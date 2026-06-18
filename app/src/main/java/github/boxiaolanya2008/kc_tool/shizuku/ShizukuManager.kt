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
    var onStateChanged: (() -> Unit)? = null

    private val userServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            Log.d(TAG, "UserService connected")
            userService = IShizukuUserService.Stub.asInterface(binder)
            isBound = true
            onStateChanged?.invoke()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "UserService disconnected")
            userService = null
            isBound = false
            onStateChanged?.invoke()
        }
    }

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        Log.d(TAG, "Shizuku binder received")
        checkPermission()
        onStateChanged?.invoke()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        Log.d(TAG, "Shizuku binder dead")
        userService = null
        isBound = false
        onStateChanged?.invoke()
    }

    private val permissionResultListener =
        Shizuku.OnRequestPermissionResultListener { _, grantResult ->
            Log.d(TAG, "Permission result: $grantResult")
            onStateChanged?.invoke()
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                bindUserService()
            }
        }

    fun initialize() {
        Shizuku.addBinderReceivedListener(binderReceivedListener)
        Shizuku.addBinderDeadListener(binderDeadListener)
        Shizuku.addRequestPermissionResultListener(permissionResultListener)

        if (Shizuku.pingBinder()) {
            checkPermission()
        }
    }

    fun destroy() {
        Shizuku.removeBinderReceivedListener(binderReceivedListener)
        Shizuku.removeBinderDeadListener(binderDeadListener)
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

    private fun buildUserServiceArgs() = Shizuku.UserServiceArgs(
        ComponentName(
            "github.boxiaolanya2008.kc_tool",
            ShizukuUserService::class.java.name
        )
    ).processNameSuffix("shizuku")
        .daemon(false)

    private fun bindUserService() {
        if (isBound) return

        try {
            Shizuku.bindUserService(buildUserServiceArgs(), userServiceConnection)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to bind UserService", e)
        }
    }

    private fun unbindUserService() {
        if (!isBound) return

        try {
            Shizuku.unbindUserService(
                buildUserServiceArgs(),
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