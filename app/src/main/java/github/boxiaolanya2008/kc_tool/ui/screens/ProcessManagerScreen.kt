package github.boxiaolanya2008.kc_tool.ui.screens

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import github.boxiaolanya2008.kc_tool.ui.anim.LottieKind
import github.boxiaolanya2008.kc_tool.ui.anim.LottieView
import github.boxiaolanya2008.kc_tool.viewmodel.ProcessManagerViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProcessManagerScreen(
    modifier: Modifier = Modifier,
    vm: ProcessManagerViewModel = viewModel()
) {
    val processes by vm.processes.collectAsState()
    val selectedProcesses by vm.selectedProcesses.collectAsState()
    val searchQuery by vm.searchQuery.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val frozenProcesses by vm.frozenProcesses.collectAsState()
    var showModeDialog by remember { mutableStateOf(false) }
    var selectedPackage by remember { mutableStateOf("") }
    var showBatchDialog by remember { mutableStateOf(false) }
    var batchMode by remember { mutableStateOf(false) }
    var showSystemProcesses by remember { mutableStateOf(false) }

    val filteredProcesses = remember(processes, searchQuery, showSystemProcesses) {
        processes.filter {
            (showSystemProcesses || !it.isSystemApp) &&
            (searchQuery.isBlank() || it.packageName.contains(searchQuery, true) || it.processName.contains(searchQuery, true))
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { vm.updateSearchQuery(it) },
                label = { Text("搜索进程") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { vm.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = showSystemProcesses,
                    onClick = { showSystemProcesses = true },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    icon = { Icon(Icons.Default.Visibility, null, modifier = Modifier.size(18.dp)) }
                ) { Text("显示系统") }

                SegmentedButton(
                    selected = !showSystemProcesses,
                    onClick = { showSystemProcesses = false },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    icon = { Icon(Icons.Default.VisibilityOff, null, modifier = Modifier.size(18.dp)) }
                ) { Text("隐藏系统") }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AssistChip(
                    onClick = { vm.selectAll(filteredProcesses) },
                    label = { Text("全选") },
                    leadingIcon = { Icon(Icons.Default.SelectAll, null, modifier = Modifier.size(18.dp)) }
                )
                AssistChip(
                    onClick = { vm.deselectAll() },
                    label = { Text("取消全选") },
                    leadingIcon = { Icon(Icons.Default.Deselect, null, modifier = Modifier.size(18.dp)) }
                )
                AssistChip(
                    onClick = { vm.loadProcesses() },
                    label = { Text("刷新") },
                    leadingIcon = { Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp)) }
                )
            }

            if (selectedProcesses.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { batchMode = true; showBatchDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(Icons.Default.Lock, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("冻结 (${selectedProcesses.size})")
                    }
                    Button(
                        onClick = { batchMode = false; showBatchDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    ) {
                        Icon(Icons.Default.LockOpen, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("解冻 (${selectedProcesses.size})")
                    }
                }
            }

            Text(
                text = "共 ${filteredProcesses.size} 个进程",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                LottieView(kind = LottieKind.Spinner, size = 80.dp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredProcesses, key = { "${it.packageName}_${it.pid}" }) { process ->
                    val isSelected = process.packageName in selectedProcesses
                    val isFrozen = process.packageName in frozenProcesses

                    ListItem(
                        headlineContent = {
                            Text(
                                text = process.processName,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        supportingContent = {
                            Column {
                                Text(
                                    text = process.packageName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "PID: ${process.pid}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        },
                        leadingContent = {
                            if (process.icon != null) {
                                Image(
                                    bitmap = process.icon.toBitmap(48, 48).asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Icon(
                                    if (isFrozen) Icons.Default.AcUnit else Icons.Default.Android,
                                    null,
                                    modifier = Modifier.size(40.dp),
                                    tint = if (isFrozen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { vm.toggleProcess(process.packageName) }
                                )
                                IconButton(onClick = {
                                    selectedPackage = process.packageName
                                    showModeDialog = true
                                }) {
                                    Icon(Icons.Default.MoreVert, null, modifier = Modifier.size(20.dp))
                                }
                            }
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = if (isFrozen) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        }
    }

    if (showModeDialog) {
        AlertDialog(
            onDismissRequest = { showModeDialog = false },
            icon = { Icon(Icons.Default.AcUnit, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("选择冻结模式") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("普通模式：进程会自动恢复", style = MaterialTheme.typography.bodyMedium)
                    Text("严肃模式：进程不会自动恢复", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                FilledTonalButton(
                    onClick = {
                        vm.freezeProcess(selectedPackage, sticky = false)
                        showModeDialog = false
                    }
                ) { Text("普通模式") }
            },
            dismissButton = {
                Button(
                    onClick = {
                        vm.freezeProcess(selectedPackage, sticky = true)
                        showModeDialog = false
                    }
                ) { Text("严肃模式") }
            }
        )
    }

    if (showBatchDialog) {
        AlertDialog(
            onDismissRequest = { showBatchDialog = false },
            icon = {
                Icon(
                    if (batchMode) Icons.Default.Lock else Icons.Default.LockOpen,
                    null,
                    tint = if (batchMode) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary
                )
            },
            title = { Text(if (batchMode) "批量冻结" else "批量解冻") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("已选 ${selectedProcesses.size} 个进程", style = MaterialTheme.typography.bodyMedium)
                    if (batchMode) {
                        Text("普通模式：进程会自动恢复", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("严肃模式：进程不会自动恢复", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            confirmButton = {
                if (batchMode) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilledTonalButton(onClick = { vm.batchFreeze(sticky = false); showBatchDialog = false }) { Text("普通冻结") }
                        Button(onClick = { vm.batchFreeze(sticky = true); showBatchDialog = false }) { Text("严肃冻结") }
                    }
                } else {
                    Button(onClick = { vm.batchUnfreeze(); showBatchDialog = false }) { Text("确认解冻") }
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchDialog = false }) { Text("取消") }
            }
        )
    }
}