package github.boxiaolanya2008.kc_tool.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import github.boxiaolanya2008.kc_tool.R
import github.boxiaolanya2008.kc_tool.manager.NotificationHelper
import github.boxiaolanya2008.kc_tool.manager.SettingsManager
import github.boxiaolanya2008.kc_tool.service.CrashLoopService
import github.boxiaolanya2008.kc_tool.service.CrashLoopState
import github.boxiaolanya2008.kc_tool.service.LoopOperationMode
import github.boxiaolanya2008.kc_tool.viewmodel.CrashLoopViewModel

data class AppInfo(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CrashLoopScreen(
    settingsManager: SettingsManager,
    modifier: Modifier = Modifier,
    vm: CrashLoopViewModel = viewModel()
) {
    val context = LocalContext.current
    val apps by vm.apps.collectAsState()
    val selectedApps by vm.selectedApps.collectAsState()
    val searchQuery by vm.searchQuery.collectAsState()
    val seconds by vm.seconds.collectAsState()
    val milliseconds by vm.milliseconds.collectAsState()
    val isRunning by vm.isRunning.collectAsState()
    val stealthMode by settingsManager.stealthMode.collectAsState()
    val noNotificationMode by settingsManager.noNotificationMode.collectAsState()
    var showAppPicker by remember { mutableStateOf(false) }
    var hasNotificationPermission by remember {
        mutableStateOf(checkNotificationPermission(context))
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
        if (granted) NotificationHelper.sendTestNotification(context)
    }

    val filteredApps = remember(apps, searchQuery) {
        if (searchQuery.isBlank()) apps
        else apps.filter {
            it.appName.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    var clearMode by rememberSaveable { mutableStateOf(false) }
    var runBoth by rememberSaveable { mutableStateOf(false) }
    val currentMode = when {
        runBoth -> LoopOperationMode.BOTH
        clearMode -> LoopOperationMode.CLEAR_DATA
        else -> LoopOperationMode.CRASH
    }
    val subtitleText = when (currentMode) {
        LoopOperationMode.BOTH -> stringResource(R.string.loop_both_subtitle)
        LoopOperationMode.CLEAR_DATA -> stringResource(R.string.clear_data_loop_subtitle)
        else -> stringResource(R.string.crash_loop_subtitle)
    }
    val modeHintText = when (currentMode) {
        LoopOperationMode.BOTH -> stringResource(R.string.loop_both_mode_hint)
        LoopOperationMode.CLEAR_DATA -> stringResource(R.string.clear_data_mode_hint)
        else -> stringResource(R.string.crash_mode_hint)
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                AnimatedVisibility(
                    visible = !hasNotificationPermission,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.NotificationsOff, null, tint = MaterialTheme.colorScheme.error)
                            Text(
                                text = stringResource(R.string.notification_permission_hint),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            FilledTonalButton(onClick = { notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                                Text(stringResource(R.string.grant))
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(stringResource(R.string.loop_mode_title), style = MaterialTheme.typography.titleMedium)
                        Text(subtitleText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(modeHintText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.clear_mode_label), style = MaterialTheme.typography.bodyMedium)
                            Switch(checked = clearMode, onCheckedChange = { clearMode = it }, enabled = !runBoth)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(R.string.run_both_label), style = MaterialTheme.typography.bodyMedium)
                            Switch(checked = runBoth, onCheckedChange = { runBoth = it })
                        }
                    }
                }

                if (stealthMode) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.VisibilityOff, null, tint = MaterialTheme.colorScheme.onTertiaryContainer, modifier = Modifier.size(18.dp))
                            Text(stringResource(R.string.stealth_mode_active), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.target_app), style = MaterialTheme.typography.titleMedium)
                            if (selectedApps.isNotEmpty()) {
                                AssistChip(onClick = { showAppPicker = true }, label = { Text("${selectedApps.size}") }, leadingIcon = { Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp)) })
                            }
                        }
                        OutlinedTextField(
                            value = selectedApps.joinToString { it.appName },
                            onValueChange = {},
                            label = { Text(stringResource(R.string.selected_apps)) },
                            readOnly = true,
                            placeholder = { Text(stringResource(R.string.tap_to_select)) },
                            trailingIcon = { IconButton(onClick = { showAppPicker = true }) { Icon(Icons.Default.Search, null) } },
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (selectedApps.isNotEmpty()) {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                selectedApps.forEach { app ->
                                    InputChip(selected = true, onClick = { vm.toggleApp(app) }, label = { Text(app.appName, maxLines = 1, overflow = TextOverflow.Ellipsis) }, trailingIcon = { Icon(Icons.Default.Close, null, modifier = Modifier.size(14.dp)) })
                                }
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(stringResource(R.string.interval_settings), style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(value = seconds, onValueChange = { vm.updateSeconds(it) }, label = { Text(stringResource(R.string.seconds)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), singleLine = true)
                            OutlinedTextField(value = milliseconds, onValueChange = { vm.updateMilliseconds(it) }, label = { Text(stringResource(R.string.milliseconds)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.weight(1f), singleLine = true)
                        }
                        val totalMs = remember(seconds, milliseconds) { (seconds.toLongOrNull() ?: 0) * 1000 + (milliseconds.toLongOrNull() ?: 0) }
                        Text(stringResource(R.string.total_interval, totalMs), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                DiagnosticCard()

                if (isRunning || CrashLoopState.isRunning.collectAsState().value) {
                    CrashStatusCard()
                }
            }

            Button(
                onClick = {
                    try {
                        if (isRunning) {
                            if (noNotificationMode) {
                                vm.stopNoNotificationLoop()
                            } else {
                                CrashLoopService.stop(context)
                                vm.setRunning(false)
                            }
                        } else {
                            if (!noNotificationMode && !hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                return@Button
                            }
                            if (selectedApps.isNotEmpty()) {
                                var totalMs = vm.getTotalMs()
                                if (totalMs <= 0) totalMs = 500L
                                if (noNotificationMode) {
                                    vm.startNoNotificationLoop(selectedApps.map { it.packageName }, totalMs, currentMode)
                                } else {
                                    val ok = CrashLoopService.startMultiple(context, selectedApps.map { it.packageName }, totalMs, stealth = stealthMode, operationMode = currentMode)
                                    if (ok) vm.setRunning(true)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        CrashLoopState.diag("Button click crashed: ${e.javaClass.simpleName} ${e.message}")
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.fillMaxWidth().padding(16.dp).align(Alignment.BottomCenter),
                enabled = selectedApps.isNotEmpty()
            ) {
                Icon(imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = if (isRunning) stringResource(R.string.stop_loop) else stringResource(
                    when {
                        runBoth -> R.string.start_both_loop
                        clearMode -> R.string.start_clear_data_loop
                        else -> R.string.start_crash_loop
                    }
                ))
            }
        }

        if (showAppPicker) {
            AppPickerDialog(
                apps = filteredApps,
                selectedApps = selectedApps,
                searchQuery = searchQuery,
                onSearchQueryChange = { vm.updateSearchQuery(it) },
                onAppToggle = { vm.toggleApp(it) },
                onSelectAll = { vm.selectAll(filteredApps) },
                onDeselectAll = { vm.deselectAll() },
                onDismiss = { showAppPicker = false; vm.updateSearchQuery("") }
            )
        }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPickerDialog(
    apps: List<AppInfo>,
    selectedApps: Set<AppInfo>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onAppToggle: (AppInfo) -> Unit,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(stringResource(R.string.select_app))
                Text(stringResource(R.string.selected_count, selectedApps.size), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column {
                OutlinedTextField(value = searchQuery, onValueChange = onSearchQueryChange, label = { Text(stringResource(R.string.search_app)) }, leadingIcon = { Icon(Icons.Default.Search, null) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(onClick = onSelectAll, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.select_all), style = MaterialTheme.typography.labelMedium) }
                    FilledTonalButton(onClick = onDeselectAll, modifier = Modifier.weight(1f)) { Text(stringResource(R.string.deselect_all), style = MaterialTheme.typography.labelMedium) }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(items = apps, key = { it.packageName }) { app ->
                        ListItem(
                            headlineContent = { Text(app.appName) },
                            supportingContent = { Text(app.packageName, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            leadingContent = { Checkbox(checked = app in selectedApps, onCheckedChange = { onAppToggle(app) }) },
                            trailingContent = { Icon(if (app.isSystemApp) Icons.Default.Android else Icons.Default.Apps, null, tint = if (app.isSystemApp) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)) }
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.confirm)) } }
    )
}

@Composable
private fun CrashStatusCard() {
    val serviceRunning by CrashLoopState.isRunning.collectAsState()
    val totalCount by CrashLoopState.totalCrashCount.collectAsState()
    val logs by CrashLoopState.logs.collectAsState()
    var showLogDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Analytics, null, tint = if (serviceRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.crash_status), style = MaterialTheme.typography.titleMedium)
                }
                Surface(shape = MaterialTheme.shapes.extraSmall, color = if (serviceRunning) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant) {
                    Text(if (serviceRunning) stringResource(R.string.running) else stringResource(R.string.stopped), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = if (serviceRunning) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.total_crashes), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$totalCount", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            if (logs.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.recent_logs), style = MaterialTheme.typography.titleSmall)
                    TextButton(onClick = { showLogDialog = true }) { Text("查看输出") }
                }
                logs.take(5).forEach { entry ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(entry.packageName.substringAfterLast('.'), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        Text(entry.operationMode.name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(entry.timestamp, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(if (entry.success) Icons.Default.CheckCircle else Icons.Default.Error, null, tint = if (entry.success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                    }
                    if (entry.output.isNotEmpty()) {
                        Text(
                            entry.output.take(120).replace("\n", " "),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (entry.success) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }

    if (showLogDialog) {
        AlertDialog(
            onDismissRequest = { showLogDialog = false },
            title = { Text("命令输出") },
            text = {
                val text = logs.take(20).joinToString("\n\n") { entry ->
                    val status = if (entry.success) "OK" else "FAIL"
                    "[$status] ${entry.timestamp} ${entry.packageName}\n${entry.output.ifEmpty { "(无输�?" }}"
                }
                Column(modifier = Modifier.heightIn(max = 400.dp)) {
                    Text(
                        text,
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
private fun DiagnosticCard() {
    val diag by CrashLoopState.diagnostic.collectAsState()
    if (diag.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Terminal, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("诊断输出", style = MaterialTheme.typography.titleSmall)
            }
            Text(
                diag,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.heightIn(max = 200.dp).verticalScroll(rememberScrollState())
            )
        }
    }
}

private fun checkNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else true
}
