package github.boxiaolanya2008.kc_tool.shizuku

import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import android.util.Log
import rikka.shizuku.Shizuku

class ShizukuManager {
    companion object {
        private const val TAG = "ShizukuManager"
        private const val REQUEST_CODE = 1001
    }

    private var userService: IShizukuUserService? = null
    private var isBound = false

    private val userServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            Log.d(TAG, "UserService connected")
            userService = IShizukuUserService.Stub.asInterface(binder)
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "UserService disconnected")
            userService = null
            isBound = false
        }
    }

    private val shizukuAliveListener = Shizuku.OnBinderReceivedListener {
        Log.d(TAG, "Shizuku binder received")
        checkPermission()
    }

    private val shizukuDeadListener = Shizuku.OnBinderDeadListener {
        Log.d(TAG, "Shizuku binder dead")
        userService = null
        isBound = false
    }

    private val permissionResultListener =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            Log.d(TAG, "Permission result: $grantResult")
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                bindUserService()
            }
        }

    fun initialize() {
        Shizuku.addBinderReceivedListener(shizukuAliveListener)
        Shizuku.addBinderDeadListener(shizukuDeadListener)
        Shizuku.addRequestPermissionResultListener(permissionResultListener)

        if (Shizuku.pingBinder()) {
            checkPermission()
        }
    }

    fun destroy() {
        Shizuku.removeBinderReceivedListener(shizukuAliveListener)
        Shizuku.removeBinderDeadListener(shizukuDeadListener)
        Shizuku.removeRequestPermissionResultListener(permissionResultListener)
        unbindUserService()
    }

    fun checkPermission() {
        if (Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            bindUserService()
        } else {
            Shizuku.requestPermission(REQUEST_CODE)
        }
    }

    private fun bindUserService() {
        if (isBound) return

        try {
            Shizuku.bindUserService(
                Shizuku.UserServiceArgs(
                    ComponentName(
                        "github.boxiaolanya2008.kc_tool",
                        ShizukuUserService::class.java.name
                    )
                ).daemon(false),
                userServiceConnection
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to bind UserService", e)
        }
    }

    private fun unbindUserService() {
        if (!isBound) return

        try {
            Shizuku.unbindUserService(
                Shizuku.UserServiceArgs(
                    ComponentName(
                        "github.boxiaolanya2008.kc_tool",
                        ShizukuUserService::class.java.name
                    )
                ),
                userServiceConnection,
                true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to unbind UserService", e)
        }
    }

    fun getService(): IShizukuUserService? = userService

    fun isConnected(): Boolean = Shizuku.pingBinder() && isBound

    fun hasPermission(): Boolean = Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
}