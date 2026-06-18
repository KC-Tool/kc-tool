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
import rikka.shizuku.Shizuku
import kotlinx.coroutines.*

class CrashLoopService : Service() {
    companion object {
        private const val TAG = "CrashLoopService"
        private const val CHANNEL_ID = "crash_loop_channel"
        private const val CHANNEL_ID_STEALTH = "crash_loop_stealth"
        private const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "github.boxiaolanya2008.kc_tool.STOP_CRASH_LOOP"

        private const val EXTRA_PACKAGE_NAMES = "package_names"
        private const val EXTRA_INTERVAL_MS = "interval_ms"
        private const val EXTRA_STEALTH = "stealth"

        fun start(context: Context, packageName: String, intervalMs: Long, stealth: Boolean = false) {
            val intent = Intent(context, CrashLoopService::class.java).apply {
                putExtra(EXTRA_PACKAGE_NAMES, arrayOf(packageName))
                putExtra(EXTRA_INTERVAL_MS, intervalMs)
                putExtra(EXTRA_STEALTH, stealth)
            }
            context.startForegroundService(intent)
        }

        fun startMultiple(context: Context, packageNames: List<String>, intervalMs: Long, stealth: Boolean = false) {
            val intent = Intent(context, CrashLoopService::class.java).apply {
                putExtra(EXTRA_PACKAGE_NAMES, packageNames.toTypedArray())
                putExtra(EXTRA_INTERVAL_MS, intervalMs)
                putExtra(EXTRA_STEALTH, stealth)
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, CrashLoopService::class.java))
        }
    }

    private var serviceJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentPackages = emptyList<String>()
    private var currentIntervalMs = 0L
    private var isStealth = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }

        val packages = intent?.getStringArrayExtra(EXTRA_PACKAGE_NAMES)?.toList() ?: run {
            Log.e(TAG, "No packages in intent, stopping")
            stopSelf()
            return START_NOT_STICKY
        }
        val intervalMs = intent.getLongExtra(EXTRA_INTERVAL_MS, 1000L)
        isStealth = intent.getBooleanExtra(EXTRA_STEALTH, false)

        Log.d(TAG, "onStartCommand: packages=$packages, interval=${intervalMs}ms, stealth=$isStealth")

        currentPackages = packages
        currentIntervalMs = intervalMs

        CrashLoopState.setRunning(true)
        CrashLoopState.setTargets(packages)
        CrashLoopState.resetCount()

        startForeground(NOTIFICATION_ID, buildNotification())

        if (isStealth) {
            hideNotification()
        }

        serviceJob?.cancel()
        serviceJob = scope.launch {
            loopCrash(packages, intervalMs)
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        serviceJob?.cancel()
        scope.cancel()
        CrashLoopState.setRunning(false)
        super.onDestroy()
    }

    private suspend fun loopCrash(packages: List<String>, intervalMs: Long) {
        while (true) {
            for (pkg in packages) {
                try {
                    Log.d(TAG, ">>> Crashing $pkg ...")
                    val output = execCommand("dumpsys activity crash $pkg")
                    CrashLoopState.incrementCrash(pkg)
                    Log.d(TAG, "<<< OK: $pkg output=${output.take(200)}")
                } catch (e: Exception) {
                    Log.e(TAG, "<<< FAIL: $pkg", e)
                    CrashLoopState.failCrash(pkg)
                }
            }
            updateNotification()
            delay(intervalMs)
        }
    }

    private fun execCommand(command: String): String {
        Log.d(TAG, "execCommand: $command")
        try {
            val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
            Log.d(TAG, "process created, pid=${process.hashCode()}")

            val output = process.inputStream.bufferedReader().readText()
            val error = process.errorStream.bufferedReader().readText()
            val exitCode = process.waitFor()

            Log.d(TAG, "exitCode=$exitCode, output=${output.take(200)}, error=${error.take(200)}")
            if (error.isNotEmpty()) Log.w(TAG, "stderr: ${error.take(200)}")
            return output.ifEmpty { error }
        } catch (e: Exception) {
            Log.e(TAG, "execCommand failed: $command", e)
            throw e
        }
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.crash_loop_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.crash_loop_channel_desc)
            enableLights(true)
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 100, 200, 100)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        manager.createNotificationChannel(channel)

        val stealthChannel = NotificationChannel(
            CHANNEL_ID_STEALTH,
            getString(R.string.settings_stealth_mode),
            NotificationManager.IMPORTANCE_MIN
        ).apply {
            description = getString(R.string.settings_stealth_mode_desc)
            setShowBadge(false)
            enableLights(false)
            enableVibration(false)
            setSound(null, null)
            lockscreenVisibility = Notification.VISIBILITY_SECRET
        }
        manager.createNotificationChannel(stealthChannel)
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

        val channelId = if (isStealth) CHANNEL_ID_STEALTH else CHANNEL_ID
        val count = CrashLoopState.crashCount.value

        val pkgText = if (currentPackages.size > 2) {
            "${currentPackages.take(2).joinToString()} +${currentPackages.size - 2}"
        } else {
            currentPackages.joinToString()
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.crash_loop_running))
            .setContentText(
                "${getString(R.string.target)}: $pkgText | " +
                        "${getString(R.string.crash_count)}: $count | " +
                        "${currentIntervalMs}ms"
            )
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setLargeIcon(android.graphics.BitmapFactory.decodeResource(resources, android.R.drawable.ic_dialog_alert))
            .setContentIntent(contentIntent)
            .addAction(android.R.drawable.ic_media_pause, getString(R.string.stop), stopPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(false)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVibrate(longArrayOf(0, 100, 200, 100))
            .setPriority(if (isStealth) NotificationCompat.PRIORITY_MIN else NotificationCompat.PRIORITY_HIGH)
            .setVisibility(if (isStealth) NotificationCompat.VISIBILITY_SECRET else NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "${getString(R.string.target)}: $pkgText\n" +
                        "${getString(R.string.crash_count)}: $count\n" +
                        "${getString(R.string.interval)}: ${currentIntervalMs}ms"
            ))
            .build()
    }

    private fun hideNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        val stealthChannel = manager.getNotificationChannel(CHANNEL_ID_STEALTH)
        stealthChannel?.let {
            it.importance = NotificationManager.IMPORTANCE_MIN
            manager.createNotificationChannel(it)
        }
    }

    private fun updateNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification())
    }
}