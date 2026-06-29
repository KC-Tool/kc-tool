package github.boxiaolanya2008.kc_tool.ui.screens
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import github.boxiaolanya2008.kc_tool.R
import github.boxiaolanya2008.kc_tool.ui.anim.LottieKind
import github.boxiaolanya2008.kc_tool.ui.anim.LottieView
import github.boxiaolanya2008.kc_tool.manager.LogManager
import github.boxiaolanya2008.kc_tool.manager.SettingsManager
@Composable
fun SettingsScreen(
    settingsManager: SettingsManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val stealthMode by settingsManager.stealthMode.collectAsState()
    val autoStart by settingsManager.autoStartOnBoot.collectAsState()
    val noNotificationMode by settingsManager.noNotificationMode.collectAsState()
    var showLogDialog by remember { mutableStateOf(false) }
    var logContent by remember { mutableStateOf("") }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SettingsSection(title = stringResource(R.string.settings_general)) {
            SettingsSwitchItem(
                icon = Icons.Default.VisibilityOff,
                title = stringResource(R.string.settings_stealth_mode),
                subtitle = stringResource(R.string.settings_stealth_mode_desc),
                checked = stealthMode,
                onCheckedChange = { settingsManager.setStealthMode(it) }
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            SettingsSwitchItem(
                icon = Icons.Default.RestartAlt,
                title = stringResource(R.string.settings_auto_start),
                subtitle = stringResource(R.string.settings_auto_start_desc),
                checked = autoStart,
                onCheckedChange = { settingsManager.setAutoStartOnBoot(it) }
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            SettingsSwitchItem(
                icon = Icons.Default.NotificationsOff,
                title = "无通知模式",
                subtitle = "不弹通知直接在后台循环，锁屏或切后台即停",
                checked = noNotificationMode,
                onCheckedChange = { settingsManager.setNoNotificationMode(it) }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        SettingsSection(title = "日志") {
            SettingsClickItem(
                icon = Icons.Default.Description,
                title = "查看内部日志",
                subtitle = "存储在应用私有目录",
                onClick = {
                    logContent = LogManager.get(context).readInternal()
                    showLogDialog = true
                }
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            SettingsClickItem(
                icon = Icons.Default.SdStorage,
                title = "查看外部日志",
                subtitle = "存储在外部私有目录",
                onClick = {
                    logContent = LogManager.get(context).readExternal()
                    showLogDialog = true
                }
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            SettingsClickItem(
                icon = Icons.Default.Delete,
                title = "清除日志",
                subtitle = "同时清除内部和外部日志",
                onClick = {
                    LogManager.get(context).clear()
                    Toast.makeText(context, "日志已清除", Toast.LENGTH_SHORT).show()
                }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LottieView(kind = LottieKind.Shield, size = 40.dp)
                Text(
                    text = stringResource(R.string.settings_about),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
    if (showLogDialog) {
        AlertDialog(
            onDismissRequest = { showLogDialog = false },
            title = { Text("日志内容") },
            text = {
                Column(modifier = Modifier.heightIn(max = 400.dp)) {
                    Text(
                        logContent.ifEmpty { "(空)" },
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                }
            },
            confirmButton = { TextButton(onClick = { showLogDialog = false }) { Text("关闭") } }
        )
    }
}
@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
        )
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                content()
            }
        }
    }
}
@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
@Composable
private fun SettingsClickItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onClick) {
            Icon(Icons.Default.ChevronRight, null)
        }
    }
}