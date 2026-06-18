package github.boxiaolanya2008.kc_tool.shizuku

import android.os.Parcel
import android.util.Log
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

class ShizukuUserService : IShizukuUserService.Stub() {
    companion object {
        private const val TAG = "ShizukuUserService"
    }

    override fun executeCommand(command: String): String {
        return try {
            val output = execViaShizuku(command)
            Log.d(TAG, "exec: $command -> ${output.take(200)}")
            output
        } catch (e: Exception) {
            Log.e(TAG, "shizuku exec failed, trying local shell: $command", e)
            execLocal(command)
        }
    }

    override fun checkRootAccess(): Boolean {
        return try {
            val output = execViaShizuku("id")
            output.contains("uid=0")
        } catch (e: Exception) {
            Log.e(TAG, "root check fail", e)
            false
        }
    }

    private fun execViaShizuku(command: String): String {
        val binder = Shizuku.getBinder() ?: throw IllegalStateException("Shizuku binder is null")

        val data = Parcel.obtain()
        val reply = Parcel.obtain()
        try {
            data.writeInterfaceToken("rikka.shizuku.IShellService")
            data.writeStringArray(arrayOf("sh", "-c", command))
            data.writeStringArray(null)
            data.writeString(null)

            binder.transact(2, data, reply, 0)
            reply.readException()

            val output = reply.readString() ?: ""
            val error = reply.readString() ?: ""

            if (error.isNotEmpty()) Log.w(TAG, "stderr: ${error.take(200)}")
            return output.ifEmpty { error }
        } finally {
            data.recycle()
            reply.recycle()
        }
    }

    private fun execLocal(command: String): String {
        val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", command))
        val output = process.inputStream.bufferedReader().readText()
        process.waitFor()
        return output
    }
}