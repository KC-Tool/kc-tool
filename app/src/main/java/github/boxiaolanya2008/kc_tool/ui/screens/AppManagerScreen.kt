package github.boxiaolanya2008.kc_tool.ui.screens

import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku

data class AppEntry(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean,
    val icon: Drawable?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppManagerScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var apps by remember { mutableStateOf<List<AppEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var showSystemApps by remember { mutableStateOf(false) }
    var selectedApp by remember { mutableStateOf<AppEntry?>(null) }
    var showActionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val packages = pm.getInstalledApplications(0)
            apps = packages.map { info ->
                AppEntry(
                    packageName = info.packageName,
                    appName = info.loadLabel(pm).toString(),
                    isSystemApp = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                    icon = try { pm.getApplicationIcon(info) } catch (_: Exception) { null }
                )
            }.sortedWith(compareByDescending<AppEntry> { it.isSystemApp }.thenBy { it.appName })
            isLoading = false
        }
    }

    val filteredApps = remember(apps, searchQuery, showSystemApps) {
        apps.filter {
            (showSystemApps || !it.isSystemApp) &&
            (searchQuery.isBlank() || it.appName.contains(searchQuery, true) || it.packageName.contains(searchQuery, true))
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("搜索应用") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, null)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AssistChip(
                onClick = { showSystemApps = !showSystemApps },
                label = { Text(if (showSystemApps) "隐藏系统" else "显示系统") },
                leadingIcon = { Icon(if (showSystemApps) Icons.Default.VisibilityOff else Icons.Default.Visibility, null, modifier = Modifier.size(18.dp)) }
            )
            AssistChip(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        isLoading = true
                        val pm = context.packageManager
                        val packages = pm.getInstalledApplications(0)
                        apps = packages.map { info ->
                            AppEntry(
                                packageName = info.packageName,
                                appName = info.loadLabel(pm).toString(),
                                isSystemApp = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                                icon = try { pm.getApplicationIcon(info) } catch (_: Exception) { null }
                            )
                        }.sortedWith(compareByDescending<AppEntry> { it.isSystemApp }.thenBy { it.appName })
                        isLoading = false
                    }
                },
                label = { Text("刷新") },
                leadingIcon = { Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp)) }
            )
        }

        Text(
            text = "共 ${filteredApps.size} 个应用",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredApps, key = { it.packageName }) { app ->
                    ListItem(
                        headlineContent = { Text(app.appName) },
                        supportingContent = {
                            Text(
                                app.packageName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingContent = {
                            if (app.icon != null) {
                                Image(
                                    bitmap = app.icon.toBitmap(48, 48).asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Icon(Icons.Default.Android, null, modifier = Modifier.size(40.dp))
                            }
                        },
                        trailingContent = {
                            IconButton(onClick = {
                                selectedApp = app
                                showActionDialog = true
                            }) {
                                Icon(Icons.Default.MoreVert, null)
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
                    )
                }
            }
        }
    }

    if (showActionDialog && selectedApp != null) {
        val app = selectedApp!!
        var commandOutput by remember { mutableStateOf("") }
        var showOutput by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showActionDialog = false; showOutput = false; commandOutput = "" },
            icon = { Icon(Icons.Default.Apps, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text(app.appName, maxLines = 1) },
            text = {
                Column {
                    Text(
                        text = app.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (showOutput) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                            modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)
                        ) {
                            Text(
                                text = commandOutput.ifEmpty { "执行中..." },
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    } else {
                        Text("选择操作：", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                if (!showOutput) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(onClick = {
                            scope.launch(Dispatchers.IO) {
                                showOutput = true
                                commandOutput = "正在强制停止..."
                                try {
                                    val p = Shizuku.newProcess(arrayOf("sh", "-c", "am force-stop ${app.packageName}"), null, null)
                                    p.waitFor()
                                    commandOutput = "已强制停止 ${app.packageName}"
                                } catch (e: Exception) {
                                    commandOutput = "失败: ${e.message}"
                                }
                            }
                        }) { Text("强制停止") }

                        Button(onClick = {
                            scope.launch(Dispatchers.IO) {
                                showOutput = true
                                commandOutput = "正在清除数据..."
                                try {
                                    val p = Shizuku.newProcess(arrayOf("sh", "-c", "pm clear ${app.packageName}"), null, null)
                                    p.waitFor()
                                    commandOutput = "已清除数据 ${app.packageName}"
                                } catch (e: Exception) {
                                    commandOutput = "失败: ${e.message}"
                                }
                            }
                        }) { Text("清除数据") }
                    }
                } else {
                    TextButton(onClick = { showActionDialog = false; showOutput = false; commandOutput = "" }) {
                        Text("关闭")
                    }
                }
            },
            dismissButton = {
                if (!showOutput) {
                    TextButton(onClick = { showActionDialog = false }) { Text("取消") }
                }
            }
        )
    }
}