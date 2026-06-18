package github.boxiaolanya2008.kc_tool.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import github.boxiaolanya2008.kc_tool.R
import github.boxiaolanya2008.kc_tool.service.CrashLoopService
import github.boxiaolanya2008.kc_tool.ui.theme.KctoolTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AppInfo(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrashLoopScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var selectedApps by remember { mutableStateOf<Set<AppInfo>>(emptySet()) }
    var searchQuery by remember { mutableStateOf("") }
    var seconds by remember { mutableStateOf("0") }
    var milliseconds by remember { mutableStateOf("500") }
    var isRunning by remember { mutableStateOf(false) }
    var showAppPicker by remember { mutableStateOf(false) }
    var hasNotificationPermission by remember {
        mutableStateOf(checkNotificationPermission(context))
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            apps = getInstalledApps(context)
        }
    }

    val filteredApps = remember(apps, searchQuery) {
        if (searchQuery.isBlank()) apps
        else apps.filter {
            it.appName.contains(searchQuery, ignoreCase = true) ||
                    it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.crash_loop_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AnimatedVisibility(
                visible = !hasNotificationPermission,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.NotificationsOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = stringResource(R.string.notification_permission_hint),
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        FilledTonalButton(
                            onClick = {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        ) {
                            Text(stringResource(R.string.grant))
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.target_app),
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (selectedApps.isNotEmpty()) {
                            AssistChip(
                                onClick = { showAppPicker = true },
                                label = { Text("${selectedApps.size}") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = selectedApps.joinToString { it.appName },
                        onValueChange = {},
                        label = { Text(stringResource(R.string.selected_apps)) },
                        readOnly = true,
                        placeholder = { Text(stringResource(R.string.tap_to_select)) },
                        trailingIcon = {
                            IconButton(onClick = { showAppPicker = true }) {
                                Icon(Icons.Default.Search, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (selectedApps.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            selectedApps.forEach { app ->
                                InputChip(
                                    selected = true,
                                    onClick = {
                                        selectedApps = selectedApps - app
                                    },
                                    label = {
                                        Text(
                                            app.appName,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.interval_settings),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = seconds,
                            onValueChange = { if (it.all { c -> c.isDigit() }) seconds = it },
                            label = { Text(stringResource(R.string.seconds)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = milliseconds,
                            onValueChange = { if (it.all { c -> c.isDigit() }) milliseconds = it },
                            label = { Text(stringResource(R.string.milliseconds)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    val totalMs = remember(seconds, milliseconds) {
                        (seconds.toLongOrNull() ?: 0) * 1000 + (milliseconds.toLongOrNull() ?: 0)
                    }
                    Text(
                        text = stringResource(R.string.total_interval, totalMs),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (isRunning) {
                        CrashLoopService.stop(context)
                        isRunning = false
                    } else {
                        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            return@Button
                        }
                        if (selectedApps.isNotEmpty()) {
                            val totalMs = (seconds.toLongOrNull() ?: 0) * 1000 +
                                    (milliseconds.toLongOrNull() ?: 0)
                            if (totalMs > 0) {
                                CrashLoopService.startMultiple(
                                    context,
                                    selectedApps.map { it.packageName },
                                    totalMs
                                )
                                isRunning = true
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                enabled = selectedApps.isNotEmpty()
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRunning) {
                        stringResource(R.string.stop_crash_loop)
                    } else {
                        stringResource(R.string.start_crash_loop)
                    }
                )
            }
        }
    }

    if (showAppPicker) {
        AppPickerDialog(
            apps = filteredApps,
            selectedApps = selectedApps,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onAppToggle = { app ->
                selectedApps = if (app in selectedApps) {
                    selectedApps - app
                } else {
                    selectedApps + app
                }
            },
            onSelectAll = { selectedApps = filteredApps.toSet() },
            onDeselectAll = { selectedApps = emptySet() },
            onDismiss = {
                showAppPicker = false
                searchQuery = ""
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppPickerDialog(
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
                Text(
                    text = stringResource(R.string.selected_count, selectedApps.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    label = { Text(stringResource(R.string.search_app)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = onSelectAll,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.select_all), style = MaterialTheme.typography.labelMedium)
                    }
                    FilledTonalButton(
                        onClick = onDeselectAll,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.deselect_all), style = MaterialTheme.typography.labelMedium)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(
                        items = apps,
                        key = { it.packageName }
                    ) { app ->
                        val isSelected = app in selectedApps
                        ListItem(
                            headlineContent = { Text(app.appName) },
                            supportingContent = {
                                Text(
                                    app.packageName,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            leadingContent = {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { onAppToggle(app) }
                                )
                            },
                            trailingContent = {
                                Icon(
                                    imageVector = if (app.isSystemApp) {
                                        Icons.Default.Android
                                    } else {
                                        Icons.Default.Apps
                                    },
                                    contentDescription = null,
                                    tint = if (app.isSystemApp) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.confirm))
            }
        }
    )
}

private fun checkNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else true
}

private fun getInstalledApps(context: Context): List<AppInfo> {
    val pm = context.packageManager
    val packages = pm.getInstalledPackages(PackageManager.GET_META_DATA)

    return packages.mapNotNull { info ->
        val appInfo = info.applicationInfo ?: return@mapNotNull null
        AppInfo(
            packageName = info.packageName,
            appName = appInfo.loadLabel(pm).toString(),
            isSystemApp = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
        )
    }.sortedWith(compareByDescending<AppInfo> { it.isSystemApp }.thenBy { it.appName })
}

@Preview(showBackground = true)
@Composable
private fun CrashLoopScreenPreview() {
    KctoolTheme {
        CrashLoopScreen()
    }
}