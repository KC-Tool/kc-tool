package github.boxiaolanya2008.kc_tool.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.ui.graphics.vector.ImageVector

data class AppStoreOptEntry(
    val key: String,
    val label: String,
    val description: String,
    val serviceName: String,
    val command: String,
    val icon: ImageVector
)

val appStoreOptEntries = listOf(
    AppStoreOptEntry(
        key = "push",
        label = "停止推送",
        description = "停止 vivo 推送服务",
        serviceName = "com.vivo.push.sdk.service.CommandService",
        command = "dumpsys activity stop-service --user 1000 com.bbk.appstore/com.vivo.push.sdk.service.CommandService",
        icon = Icons.Filled.NotificationsOff
    ),
    AppStoreOptEntry(
        key = "report",
        label = "停止上报",
        description = "停止数据上报服务",
        serviceName = "com.bbk.appstore.report.independent.IndependentReporterService",
        command = "dumpsys activity stop-service --user 1000 com.bbk.appstore/com.bbk.appstore.report.independent.IndependentReporterService",
        icon = Icons.Filled.DataUsage
    ),
    AppStoreOptEntry(
        key = "daemon",
        label = "停止守护",
        description = "停止应用商店守护进程",
        serviceName = "com.bbk.appstore.patch.StoreDeamonService",
        command = "dumpsys activity stop-service --user 1000 com.bbk.appstore/com.bbk.appstore.patch.StoreDeamonService",
        icon = Icons.Filled.Shield
    ),
    AppStoreOptEntry(
        key = "silent_update",
        label = "停止静默升级",
        description = "阻止后台静默下载安装更新",
        serviceName = "com.bbk.appstore.update.SilentUpdateService",
        command = "dumpsys activity stop-service --user 1000 com.bbk.appstore/com.bbk.appstore.update.SilentUpdateService",
        icon = Icons.Filled.SystemUpdate
    ),
    AppStoreOptEntry(
        key = "job",
        label = "停止升级 Job",
        description = "停止升级调度任务",
        serviceName = "com.bbk.appstore.update.AppStoreJobService",
        command = "dumpsys activity stop-service --user 1000 com.bbk.appstore/com.bbk.appstore.update.AppStoreJobService",
        icon = Icons.Filled.Schedule
    )
)
