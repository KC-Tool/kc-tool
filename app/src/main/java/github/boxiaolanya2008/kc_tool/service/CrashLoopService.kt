package github.boxiaolanya2008.kc_tool.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import github.boxiaolanya2008.kc_tool.MainActivity
import github.boxiaolanya2008.kc_tool.R
import github.boxiaolanya2008.kc_tool.shizuku.IShizukuUserService
import github.boxiaolanya2008.kc_tool.shizuku.ShizukuManager
import kotlinx.coroutines.*

class CrashLoopService : Service() {
    companion object {
        private const val TAG = "CrashLoopService"
        private const val CHANNEL_ID = "crash_loop_channel"
        private const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "github.boxiaolanya2008.kc_tool.STOP_CRASH_LOOP"

        private const val EXTRA_PACKAGE_NAME = "package_name"
        private const val EXTRA_INTERVAL_MS = "interval_ms"

        fun start(context: Context, packageName: String, intervalMs: Long) {
            val intent = Intent(context, CrashLoopService::class.java).apply {
                putExtra(EXTRA_PACKAGE_NAME, packageName)
                putExtra(EXTRA_INTERVAL_MS, intervalMs)
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, CrashLoopService::class.java))
        }
    }

    private var serviceJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var shizukuManager: ShizukuManager? = null
    private var crashCount = 0
    private var currentPackage = ""
    private var currentIntervalMs = 0L

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        shizukuManager = ShizukuManager().also { it.initialize() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        val packageName = intent?.getStringExtra(EXTRA_PACKAGE_NAME) ?: run {
            stopSelf()
            return START_NOT_STICKY
        }
        val intervalMs = intent.getLongExtra(EXTRA_INTERVAL_MS, 1000L)

        currentPackage = packageName
        currentIntervalMs = intervalMs
        crashCount = 0

        startForeground(NOTIFICATION_ID, buildNotification())

        serviceJob?.cancel()
        serviceJob = scope.launch {
            loopCrash(packageName, intervalMs)
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        serviceJob?.cancel()
        scope.cancel()
        shizukuManager?.destroy()
        super.onDestroy()
    }

    private suspend fun loopCrash(packageName: String, intervalMs: Long) {
        while (true) {
            try {
                val service = shizukuManager?.getService()
                if (service == null) {
                    Log.w(TAG, "Shizuku service not available")
                    delay(intervalMs)
                    continue
                }

                val pid = getPid(service, packageName)
                if (pid != null) {
                    crashProcess(service, pid)
                    crashCount++
                    updateNotification()
                    Log.d(TAG, "Crashed $packageName (pid=$pid) #$crashCount")
                } else {
                    Log.w(TAG, "Process not found for $packageName")
                }

                delay(intervalMs)
            } catch (e: Exception) {
                Log.e(TAG, "Error in crash loop", e)
                delay(intervalMs)
            }
        }
    }

    private fun getPid(service: IShizukuUserService, packageName: String): String? {
        val output = service.executeCommand("ps -A | grep -i $packageName")
        return output.lineSequence()
            .firstOrNull()
            ?.trim()
            ?.split("\\s+".toRegex())
            ?.getOrNull(1)
    }

    private fun crashProcess(service: IShizukuUserService, pid: String) {
        service.executeCommand("dumpsys activity crash $pid")
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.crash_loop_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.crash_loop_channel_desc)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val stopIntent = Intent(this, CrashLoopService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.crash_loop_running))
            .setContentText(
                "${getString(R.string.target)}: $currentPackage | " +
                        "${getString(R.string.crash_count)}: $crashCount | " +
                        "${currentIntervalMs}ms"
            )
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentIntent(contentIntent)
            .addAction(android.R.drawable.ic_media_pause, getString(R.string.stop), stopPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun updateNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification())
    }
}