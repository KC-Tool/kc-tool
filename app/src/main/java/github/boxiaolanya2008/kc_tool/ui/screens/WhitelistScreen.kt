package github.boxiaolanya2008.kc_tool.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.viewmodel.compose.viewModel
import github.boxiaolanya2008.kc_tool.ui.anim.LottieKind
import github.boxiaolanya2008.kc_tool.ui.anim.LottieView
import github.boxiaolanya2008.kc_tool.viewmodel.WhitelistViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhitelistScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    vm: WhitelistViewModel = viewModel()
) {
    val apps by vm.whitelist.collectAsState()
    val loading by vm.loading.collectAsState()
    val raw by vm.rawOutput.collectAsState()
    var search by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { vm.refresh() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("白名单应用") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { vm.refresh() }, enabled = !loading) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { inner ->
        Column(modifier = modifier.fillMaxSize().padding(inner)) {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("搜索包名或应用名", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (search.isNotEmpty()) {
                        IconButton(onClick = { search = "" }) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                    disabledBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )

            if (loading) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                    LottieView(kind = LottieKind.Spinner, size = 64.dp)
                }
            }

            val filtered = apps.filter {
                search.isBlank() ||
                it.appName.contains(search, true) ||
                it.packageName.contains(search, true)
            }

            if (filtered.isEmpty() && !loading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        LottieView(kind = LottieKind.Empty, size = 80.dp)
                        Text("白名单为空,或解析失败")
                        if (raw.isNotEmpty()) {
                            Text(
                                "原始输出(前 400 字符):\n" + raw.take(400),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filtered, key = { it.packageName }) { app ->
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
                                Icon(
                                    Icons.Default.CheckCircle,
                                    null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }
            }
        }
    }
}
