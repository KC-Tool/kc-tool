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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import github.boxiaolanya2008.kc_tool.manager.LogManager
import github.boxiaolanya2008.kc_tool.manager.SettingsManager
import github.boxiaolanya2008.kc_tool.service.CrashLoopState
import github.boxiaolanya2008.kc_tool.shizuku.ShizukuManager
import github.boxiaolanya2008.kc_tool.ui.components.ShizukuStatusCard
import github.boxiaolanya2008.kc_tool.ui.screens.CrashLoopScreen
import github.boxiaolanya2008.kc_tool.ui.screens.SettingsScreen
import github.boxiaolanya2008.kc_tool.ui.theme.KctoolTheme

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

private sealed class AppScreen {
    object Home : AppScreen()
    object LoopTool : AppScreen()
}

@Composable
private fun MainApp(
    isConnected: Boolean,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    settingsManager: SettingsManager
) {
    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Home) }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (currentScreen) {
                AppScreen.Home -> HomeScreen(
                    isConnected = isConnected,
                    hasPermission = hasPermission,
                    onRequestPermission = onRequestPermission,
                    onOpenTool = { currentScreen = AppScreen.LoopTool }
                )
                AppScreen.LoopTool -> CrashLoopScreen(
                    settingsManager = settingsManager,
                    modifier = Modifier.fillMaxSize(),
                    onBack = { currentScreen = AppScreen.Home }
                )
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
            text = stringResource(R.string.toolbox_title),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

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

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    KctoolTheme {
        HomeScreen(
            isConnected = true,
            hasPermission = true,
            onRequestPermission = {},
            onOpenTool = {}
        )
    }
}