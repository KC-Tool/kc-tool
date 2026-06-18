package github.boxiaolanya2008.kc_tool.manager

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class LogManager private constructor(context: Context) {
    companion object {
        private const val TAG = "LogManager"
        private const val MAX_SIZE = 2 * 1024 * 1024 // 2MB, then rotate
        private var instance: LogManager? = null

        fun get(context: Context): LogManager {
            return instance ?: synchronized(this) {
                instance ?: LogManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    private val internalDir = File(context.filesDir, "logs").apply { mkdirs() }
    private val externalDir = context.getExternalFilesDir(null)?.let { File(it, "logs").apply { mkdirs() } }

    private val internalLog = File(internalDir, "crash_loop.log")
    private val internalLogOld = File(internalDir, "crash_loop.log.old")

    private val externalLog = externalDir?.let { File(it, "crash_loop.log") }
    private val externalLogOld = externalDir?.let { File(it, "crash_loop.log.old") }

    init {
        rotateIfNeeded(internalLog, internalLogOld)
        if (externalLog != null && externalLogOld != null) {
            rotateIfNeeded(externalLog, externalLogOld)
        }
    }

    private fun rotateIfNeeded(current: File, backup: File) {
        if (current.exists() && current.length() > MAX_SIZE) {
            backup.delete()
            current.renameTo(backup)
        }
    }

    fun write(tag: String, msg: String, isError: Boolean = false) {
        val line = "${timeFormat.format(Date())}  [${if (isError) "E" else "I"}]  $tag  $msg\n"
        try {
            internalLog.appendText(line)
        } catch (e: IOException) {
            Log.e(TAG, "write internal failed", e)
        }
        try {
            externalLog?.appendText(line)
        } catch (e: IOException) {
            Log.e(TAG, "write external failed", e)
        }
    }

    suspend fun writeAsync(tag: String, msg: String, isError: Boolean = false) = withContext(Dispatchers.IO) {
        write(tag, msg, isError)
    }

    fun readInternal(): String {
        return if (internalLog.exists()) internalLog.readText().take(100_000) else ""
    }

    fun readExternal(): String {
        return externalLog?.takeIf { it.exists() }?.readText()?.take(100_000) ?: ""
    }

    fun clear() {
        internalLog.delete()
        internalLogOld.delete()
        externalLog?.delete()
        externalLogOld?.delete()
    }

    fun installGlobalHandler(context: Context) {
        val old = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val sw = java.io.StringWriter()
            throwable.printStackTrace(java.io.PrintWriter(sw))
            write("FATAL", "Thread=${thread.name}\n${sw.toString()}", isError = true)
            old?.uncaughtException(thread, throwable)
        }
    }
}
