package github.boxiaolanya2008.kc_tool.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ClearDataLoopScreen(
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
    var hasNotificationPermission by remember { mutableStateOf(checkNotificationPermission(context)) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
        if (granted) NotificationHelper.sendTestNotification(context)
    }

    val filteredApps = remember(apps, searchQuery) {
        if (searchQuery.isBlank()) apps else apps.filter {
            it.appName.contains(searchQuery, ignoreCase = true) || it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.clear_data_loop_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Box(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(stringResource(R.string.clear_data_loop_subtitle), style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(stringResource(R.string.clear_data_loop_command_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

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

                if (isRunning || CrashLoopState.isRunning.collectAsState().value) {
                    ClearDataStatusCard()
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
                                    vm.startNoNotificationLoop(selectedApps.map { it.packageName }, totalMs, LoopOperationMode.CLEAR_DATA)
                                } else {
                                    val ok = CrashLoopService.startMultiple(context, selectedApps.map { it.packageName }, totalMs, stealth = stealthMode, operationMode = LoopOperationMode.CLEAR_DATA)
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
                Text(text = if (isRunning) stringResource(R.string.stop_clear_data_loop) else stringResource(R.string.start_clear_data_loop))
            }
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

@Composable
private fun ClearDataStatusCard() {
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
                    Icon(Icons.Default.DeleteSweep, null, tint = if (serviceRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("清理状态", style = MaterialTheme.typography.titleMedium)
                }
                Surface(shape = MaterialTheme.shapes.extraSmall, color = if (serviceRunning) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant) {
                    Text(if (serviceRunning) stringResource(R.string.running) else stringResource(R.string.stopped), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = if (serviceRunning) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("执行次数", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$totalCount", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            if (logs.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                logs.take(5).forEach { entry ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(entry.packageName.substringAfterLast('.'), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
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
}

private fun checkNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else true
}
