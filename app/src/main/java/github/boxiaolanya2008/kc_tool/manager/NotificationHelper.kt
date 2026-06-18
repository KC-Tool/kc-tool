package github.boxiaolanya2008.kc_tool.manager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import github.boxiaolanya2008.kc_tool.R

object NotificationHelper {
    private const val TEST_CHANNEL_ID = "test_channel"
    private const val TEST_NOTIFICATION_ID = 9999

    fun sendTestNotification(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channel = NotificationChannel(
            TEST_CHANNEL_ID,
            "测试通知",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "用于测试通知权限"
        }
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, TEST_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(context.getString(R.string.notification_test_success))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        manager.notify(TEST_NOTIFICATION_ID, notification)
    }
}