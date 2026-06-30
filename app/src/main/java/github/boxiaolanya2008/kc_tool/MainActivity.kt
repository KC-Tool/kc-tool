package github.boxiaolanya2008.kc_tool

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import github.boxiaolanya2008.kc_tool.manager.LogManager
import github.boxiaolanya2008.kc_tool.manager.SettingsManager
import github.boxiaolanya2008.kc_tool.shizuku.ShizukuManager
import github.boxiaolanya2008.kc_tool.ui.components.AppDrawer
import github.boxiaolanya2008.kc_tool.ui.components.AppTopBar
import github.boxiaolanya2008.kc_tool.ui.components.NavItem
import github.boxiaolanya2008.kc_tool.ui.components.ShizukuStatusCard
import github.boxiaolanya2008.kc_tool.ui.screens.CrashLoopScreen
import github.boxiaolanya2008.kc_tool.ui.screens.AudioEffectScreen
import github.boxiaolanya2008.kc_tool.ui.screens.ProcessManagerScreen
import github.boxiaolanya2008.kc_tool.ui.screens.SettingsScreen
import github.boxiaolanya2008.kc_tool.ui.screens.AppManagerScreen
import github.boxiaolanya2008.kc_tool.ui.screens.AppStoreOptScreen
import github.boxiaolanya2008.kc_tool.ui.screens.GameOptScreen
import github.boxiaolanya2008.kc_tool.ui.screens.WhitelistScreen
import github.boxiaolanya2008.kc_tool.ui.theme.KctoolTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var shizukuManager: ShizukuManager
    private lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        LogManager.get(this).installGlobalHandler(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        shizukuManager = ShizukuManager()
        shizukuManager.initialize()
        settingsManager = SettingsManager(applicationContext)
        LogManager.get(this).write("MainActivity", "onCreate")

        setContent {
            KctoolTheme {
                var refreshVersion by remember { mutableIntStateOf(0) }

                LaunchedEffect(Unit) {
                    shizukuManager.onStateChanged = { refreshVersion++ }
                }

                DisposableEffect(Unit) {
                    onDispose { shizukuManager.onStateChanged = null }
                }

                val isConnected by remember(refreshVersion) {
                    mutableStateOf(shizukuManager.isConnected())
                }
                val hasPermission by remember(refreshVersion) {
                    mutableStateOf(shizukuManager.hasPermission())
                }

                MainApp(
                    isConnected = isConnected,
                    hasPermission = hasPermission,
                    onRequestPermission = { shizukuManager.checkPermission() },
                    settingsManager = settingsManager
                )
            }
        }
    }

    override fun onDestroy() {
        shizukuManager.destroy()
        super.onDestroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainApp(
    isConnected: Boolean,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    settingsManager: SettingsManager
) {
    var currentNav by remember { mutableStateOf(NavItem.Home) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showWhitelist by remember { mutableStateOf(false) }
    val screenTitle = when (currentNav) {
        NavItem.Home -> stringResource(R.string.toolbox_title)
        NavItem.LoopTool -> stringResource(R.string.crash_loop_title)
        NavItem.AudioEffect -> "全局音效"
        NavItem.ProcessManager -> "进程管理"
        NavItem.AppManager -> "应用管理"
        NavItem.AppStoreOpt -> "应用商店优化"
        NavItem.GameOpt -> "游戏优化"
        NavItem.Settings -> stringResource(R.string.settings_title)
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                currentNav = currentNav,
                onNavClick = { item ->
                    currentNav = item
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                AppTopBar(
                    title = screenTitle,
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    },
                    onWhitelistClick = { showWhitelist = true }
                )
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (currentNav) {
                    NavItem.Home -> HomeScreen(
                        isConnected = isConnected,
                        hasPermission = hasPermission,
                        onRequestPermission = onRequestPermission,
                        onOpenTool = { currentNav = NavItem.LoopTool }
                    )
                    NavItem.LoopTool -> CrashLoopScreen(
                        settingsManager = settingsManager,
                        modifier = Modifier.fillMaxSize()
                    )
                    NavItem.AudioEffect -> AudioEffectScreen(
                        modifier = Modifier.fillMaxSize()
                    )
                    NavItem.ProcessManager -> ProcessManagerScreen(
                        modifier = Modifier.fillMaxSize()
                    )
                    NavItem.AppManager -> AppManagerScreen(
                        modifier = Modifier.fillMaxSize()
                    )
                    NavItem.AppStoreOpt -> AppStoreOptScreen(
                        modifier = Modifier.fillMaxSize()
                    )
                    NavItem.GameOpt -> GameOptScreen(
                        modifier = Modifier.fillMaxSize()
                    )
                    NavItem.Settings -> SettingsScreen(
                        settingsManager = settingsManager,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                if (showWhitelist) {
                    WhitelistScreen(
                        onBack = { showWhitelist = false },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(
    isConnected: Boolean,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    onOpenTool: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.toolbox_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ShizukuStatusCard(
            isConnected = isConnected,
            hasPermission = hasPermission,
            onRequestPermission = onRequestPermission
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.home_tips_title), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                Text(stringResource(R.string.home_tips_content), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.toolbox_panel_title), style = MaterialTheme.typography.titleMedium)
                Text(stringResource(R.string.toolbox_panel_desc), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ToolTile(
                        title = stringResource(R.string.tool_loop_tool),
                        description = stringResource(R.string.tool_loop_tool_desc),
                        icon = Icons.Default.BugReport,
                        onClick = { onOpenTool(1) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolTile(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}