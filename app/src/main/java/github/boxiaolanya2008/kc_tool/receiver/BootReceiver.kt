package github.boxiaolanya2008.kc_tool.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import github.boxiaolanya2008.kc_tool.manager.SettingsManager
import github.boxiaolanya2008.kc_tool.service.CrashLoopService

class BootReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        Log.d(TAG, "Boot completed, checking auto-start...")

        val settings = SettingsManager(context.applicationContext)
        val autoStart = settings.autoStartOnBoot.value

        if (!autoStart) {
            Log.d(TAG, "Auto-start disabled, skipping")
            return
        }

        val packages = settings.lastSelectedPackages.value.toList()
        if (packages.isEmpty()) {
            Log.d(TAG, "No previous crash targets, skipping")
            return
        }

        val intervalMs = settings.defaultIntervalSec.value.toLongOrNull()?.times(1000)?.plus(
            settings.defaultIntervalMs.value.toLongOrNull() ?: 500
        ) ?: 1000L

        Log.d(TAG, "Auto-starting crash loop: $packages, interval=${intervalMs}ms")
        CrashLoopService.startMultiple(context, packages, intervalMs, settings.stealthMode.value)
    }
}