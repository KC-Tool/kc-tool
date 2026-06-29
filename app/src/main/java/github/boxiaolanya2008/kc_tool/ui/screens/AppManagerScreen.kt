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
import github.boxiaolanya2008.kc_tool.ui.anim.LottieKind
import github.boxiaolanya2008.kc_tool.ui.anim.LottieView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import rikka.shizuku.Shizuku

data class AppEntry(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean,
    val icon: Drawable?
)

private suspend fun runShell(command: String): Triple<String, String, Int> = withTimeout(15_000) {
    val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
    coroutineScope {
        val outDef = async(Dispatchers.IO) {
            runCatching { process.inputStream.bufferedReader().readText() }.getOrDefault("")
        }
        val errDef = async(Dispatchers.IO) {
            runCatching { process.errorStream.bufferedReader().readText() }.getOrDefault("")
        }
        val code = process.waitFor()
        Triple(outDef.await(), errDef.await(), code)
    }
}

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
                    isSystemApp = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                                  && (info.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0,
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
            label = { Text("\u641c\u7d22\u5e94\u7528") },
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
                label = { Text(if (showSystemApps) "\u9690\u85cf\u7cfb\u7edf" else "\u663e\u793a\u7cfb\u7edf") },
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
                                isSystemApp = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                                              && (info.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0,
                                icon = try { pm.getApplicationIcon(info) } catch (_: Exception) { null }
                            )
                        }.sortedWith(compareByDescending<AppEntry> { it.isSystemApp }.thenBy { it.appName })
                        isLoading = false
                    }
                },
                label = { Text("\u5237\u65b0") },
                leadingIcon = { Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp)) }
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                LottieView(kind = LottieKind.Spinner, size = 80.dp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredApps, key = { it.packageName }) { app ->
                    ListItem(
                        headlineContent = { Text(app.appName) },
                        supportingContent = {
                            Text(app.packageName, style = MaterialTheme.typography.bodySmall)
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
                                text = commandOutput.ifEmpty { "\u6267\u884c\u4e2d..." },
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    } else {
                        Text("\u9009\u62e9\u64cd\u4f5c\uff1a", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                if (!showOutput) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(onClick = {
                                scope.launch(Dispatchers.IO) {
                                    showOutput = true
                                    commandOutput = "\u6b63\u5728\u5f3a\u5236\u505c\u6b62..."
                                    val (out, err, code) = runShell("am force-stop ${app.packageName}")
                                    commandOutput = if (code == 0) "\u5df2\u5f3a\u5236\u505c\u6b62: ${app.packageName}\n" + out.trim()
                                                    else "\u5931\u8d25 (exit=$code): ${err.trim().ifEmpty { out.trim() }}"
                                }
                            }) { Text("\u5f3a\u5236\u505c\u6b62") }

                            Button(onClick = {
                                scope.launch(Dispatchers.IO) {
                                    showOutput = true
                                    commandOutput = "\u6b63\u5728\u6e05\u9664\u6570\u636e..."
                                    val (out, err, code) = runShell("pm clear ${app.packageName}")
                                    commandOutput = if (code == 0) "\u5df2\u6e05\u9664\u6570\u636e: ${app.packageName}\n" + out.trim()
                                                    else "\u5931\u8d25 (exit=$code): ${err.trim().ifEmpty { out.trim() }}"
                                }
                            }) { Text("\u6e05\u9664\u6570\u636e") }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        showOutput = true
                                        commandOutput = "\u6b63\u5728\u52a0\u5165\u767d\u540d\u5355..."
                                        val (out, err, code) = runShell("dumpsys deviceidle whitelist +${app.packageName}")
                                        commandOutput = if (code == 0) "\u5df2\u52a0\u5165\u767d\u540d\u5355: ${app.packageName}\n" + out.trim()
                                                        else "\u5931\u8d25 (exit=$code): ${err.trim().ifEmpty { out.trim() }}"
                                    }
                                },
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) { Text("\u52a0\u5165\u767d\u540d\u5355") }

                            OutlinedButton(
                                onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        showOutput = true
                                        commandOutput = "\u6b63\u5728\u79fb\u51fa\u767d\u540d\u5355..."
                                        val (out, err, code) = runShell("dumpsys deviceidle whitelist -${app.packageName}")
                                        commandOutput = if (code == 0) "\u5df2\u79fb\u51fa\u767d\u540d\u5355: ${app.packageName}\n" + out.trim()
                                                        else "\u5931\u8d25 (exit=$code): ${err.trim().ifEmpty { out.trim() }}"
                                    }
                                }
                            ) { Text("\u79fb\u51fa\u767d\u540d\u5355") }
                        }
                    }
                } else {
                    TextButton(onClick = { showActionDialog = false; showOutput = false; commandOutput = "" }) {
                        Text("\u5173\u95ed")
                    }
                }
            },
            dismissButton = {
                if (!showOutput) {
                    TextButton(onClick = { showActionDialog = false }) { Text("\u53d6\u6d88") }
                }
            }
        )
    }
}