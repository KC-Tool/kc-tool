package github.boxiaolanya2008.kc_tool.shizuku

import android.util.Log

class ShizukuUserService : IShizukuUserService.Stub() {
    companion object {
        private const val TAG = "ShizukuUserService"
    }

    override fun executeCommand(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()
            Log.d(TAG, "exec: $command")
            output
        } catch (e: Exception) {
            Log.e(TAG, "fail: $command", e)
            "Error: ${e.message}"
        }
    }

    override fun checkRootAccess(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", "id"))
            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()
            output.contains("uid=0")
        } catch (e: Exception) {
            Log.e(TAG, "root check fail", e)
            false
        }
    }
}