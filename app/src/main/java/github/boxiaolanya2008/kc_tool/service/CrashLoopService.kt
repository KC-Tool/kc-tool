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
import github.boxiaolanya2008.kc_tool.manager.LogManager
import rikka.shizuku.Shizuku
import kotlinx.coroutines.*

class CrashLoopService : Service() {
    companion object {
        private const val TAG = "CrashLoopService"
        private const val CHANNEL_ID = "crash_loop_v2"
        private const val CHANNEL_ID_STEALTH = "crash_loop_stealth_v2"
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

        fun startMultiple(context: Context, packageNames: List<String>, intervalMs: Long, stealth: Boolean = false): Boolean {
            val intent = Intent(context, CrashLoopService::class.java).apply {
                putExtra(EXTRA_PACKAGE_NAMES, packageNames.toTypedArray())
                putExtra(EXTRA_INTERVAL_MS, intervalMs)
                putExtra(EXTRA_STEALTH, stealth)
            }
            return try {
                context.startForegroundService(intent)
                true
            } catch (e: Exception) {
                CrashLoopState.diag("startForegroundService FAILED: ${e.javaClass.simpleName} ${e.message}")
                false
            }
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

    private lateinit var logManager: LogManager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logManager = LogManager.get(this)
        try {
            CrashLoopState.diag("Service onStartCommand action=${intent?.action}")

            if (intent?.action == ACTION_STOP) {
                CrashLoopState.diag("Service stop action received")
                logManager.write(TAG, "stop action")
                stopSelf()
                return START_NOT_STICKY
            }

            val packages = intent?.getStringArrayExtra(EXTRA_PACKAGE_NAMES)?.toList() ?: run {
                Log.e(TAG, "No packages in intent, stopping")
                CrashLoopState.diag("ERROR: no packages in intent")
                logManager.write(TAG, "ERROR: no packages", isError = true)
                stopSelf()
                return START_NOT_STICKY
            }
            val intervalMs = intent.getLongExtra(EXTRA_INTERVAL_MS, 1000L)
            isStealth = intent.getBooleanExtra(EXTRA_STEALTH, false)

            Log.d(TAG, "onStartCommand: packages=$packages, interval=${intervalMs}ms, stealth=$isStealth")
            CrashLoopState.diag("Start pkg=$packages interval=${intervalMs}ms")
            logManager.write(TAG, "start pkgs=$packages interval=${intervalMs}ms")

            currentPackages = packages
            currentIntervalMs = intervalMs

            CrashLoopState.setRunning(true)
            CrashLoopState.setTargets(packages)
            CrashLoopState.resetCount()

            val notification = try {
                buildNotification()
            } catch (e: Exception) {
                Log.e(TAG, "buildNotification failed", e)
                CrashLoopState.diag("buildNotification FAILED: ${e.message}")
                logManager.write(TAG, "buildNotification FAILED: ${e.message}", isError = true)
                fallbackNotification()
            }

            try {
                startForeground(NOTIFICATION_ID, notification)
                CrashLoopState.diag("startForeground OK")
            } catch (e: Exception) {
                CrashLoopState.diag("startForeground FAILED: ${e.javaClass.simpleName} ${e.message}")
                Log.e(TAG, "startForeground failed", e)
                logManager.write(TAG, "startForeground FAILED: ${e.javaClass.simpleName} ${e.message}", isError = true)
                CrashLoopState.setRunning(false)
                stopSelf()
                return START_NOT_STICKY
            }

            serviceJob?.cancel()
            serviceJob = scope.launch {
                loopCrash(packages, intervalMs)
            }
        } catch (e: Exception) {
            CrashLoopState.diag("onStartCommand FATAL: ${e.javaClass.simpleName} ${e.message}")
            Log.e(TAG, "onStartCommand fatal", e)
            logManager.write(TAG, "onStartCommand FATAL: ${e.javaClass.simpleName} ${e.message}", isError = true)
            stopSelf()
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
        CrashLoopState.diag("loopCrash started, pkgs=${packages.size}")
        logManager.write(TAG, "loopCrash started pkgs=${packages.size}")
        while (true) {
            for (pkg in packages) {
                try {
                    Log.d(TAG, ">>> Crashing $pkg ...")
                    CrashLoopState.diag("run → $pkg")
                    val output = execCommand("am crash $pkg")
                    CrashLoopState.incrementCrash(pkg, output)
                    CrashLoopState.diag("OK $pkg")
                    logManager.write(TAG, "OK $pkg")
                    Log.d(TAG, "<<< OK: $pkg output=${output.take(200)}")
                } catch (e: Exception) {
                    Log.e(TAG, "<<< FAIL: $pkg", e)
                    CrashLoopState.failCrash(pkg, e.message ?: "")
                    CrashLoopState.diag("FAIL $pkg: ${e.message}")
                    logManager.write(TAG, "FAIL $pkg: ${e.message}", isError = true)
                }
            }
            delay(intervalMs)
        }
    }

    private suspend fun execCommand(command: String): String {
        Log.d(TAG, "execCommand: $command")
        try {
            val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
            Log.d(TAG, "process created")
            CrashLoopState.diag("process created")

            val (output, error, exitCode) = withTimeout(15_000) {
                coroutineScope {
                    val outDeferred = async(Dispatchers.IO) {
                        runCatching { process.inputStream.bufferedReader().readText() }.getOrDefault("")
                    }
                    val errDeferred = async(Dispatchers.IO) {
                        runCatching { process.errorStream.bufferedReader().readText() }.getOrDefault("")
                    }
                    val code = process.waitFor()
                    Triple(outDeferred.await(), errDeferred.await(), code)
                }
            }

            Log.d(TAG, "exitCode=$exitCode, out=${output.take(200)}, err=${error.take(200)}")
            CrashLoopState.diag("exit=$exitCode out=${output.take(80)} err=${error.take(80)}")
            logManager.write(TAG, "exit=$exitCode out=${output.take(120)} err=${error.take(120)}")
            if (exitCode != 0)
                throw RuntimeException("exitCode=$exitCode, stderr=$error")
            val e = error.lowercase()
            if (e.contains("unable") || e.contains("permission denied") || e.contains("not found") || e.contains("error"))
                throw RuntimeException("cmd error: $error")
            return output.ifEmpty { error }
        } catch (ex: Exception) {
            Log.e(TAG, "execCommand failed: $command", ex)
            CrashLoopState.diag("execCommand exception: ${ex.message}")
            logManager.write(TAG, "execCommand exception: ${ex.message}", isError = true)
            throw ex
        }
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        // nuke old channels so their vibration/sound settings die with them
        listOf("crash_loop_channel", "crash_loop_stealth").forEach {
            runCatching { manager.deleteNotificationChannel(it) }
        }

        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.crash_loop_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.crash_loop_channel_desc)
            enableLights(false)
            enableVibration(false)
            setSound(null, null)
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

        val channelId = if (isStealth) CHANNEL_ID_STEALTH else CHANNEL_ID

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("kc-tool")
            .setContentText("正在运行...")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .addAction(android.R.drawable.ic_media_pause, getString(R.string.stop), stopPendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(if (isStealth) NotificationCompat.PRIORITY_MIN else NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(if (isStealth) NotificationCompat.VISIBILITY_SECRET else NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    private fun fallbackNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("kc-tool")
            .setContentText("正在运行...")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

}