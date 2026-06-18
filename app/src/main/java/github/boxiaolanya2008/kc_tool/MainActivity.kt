package github.boxiaolanya2008.kc_tool

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import github.boxiaolanya2008.kc_tool.manager.SettingsManager
import github.boxiaolanya2008.kc_tool.shizuku.ShizukuManager
import github.boxiaolanya2008.kc_tool.ui.components.ShizukuStatusCard
import github.boxiaolanya2008.kc_tool.ui.screens.CrashLoopScreen
import github.boxiaolanya2008.kc_tool.ui.screens.SettingsScreen
import github.boxiaolanya2008.kc_tool.ui.theme.KctoolTheme

class MainActivity : ComponentActivity() {
    private lateinit var shizukuManager: ShizukuManager
    private lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        shizukuManager = ShizukuManager()
        shizukuManager.initialize()
        settingsManager = SettingsManager(applicationContext)

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

@Composable
private fun MainApp(
    isConnected: Boolean,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    settingsManager: SettingsManager
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_home)) },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.BugReport, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_crash_loop)) },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_settings)) },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                fadeIn() + slideInHorizontally { if (targetState > initialState) it else -it } togetherWith
                        fadeOut() + slideOutHorizontally { if (targetState > initialState) -it else it }
            },
            label = "tab"
        ) { tab ->
            when (tab) {
                0 -> HomeScreen(
                    isConnected = isConnected,
                    hasPermission = hasPermission,
                    onRequestPermission = onRequestPermission,
                    modifier = Modifier.padding(innerPadding)
                )
                1 -> CrashLoopScreen(
                    settingsManager = settingsManager,
                    modifier = Modifier.padding(innerPadding)
                )
                2 -> SettingsScreen(
                    settingsManager = settingsManager,
                    modifier = Modifier.padding(innerPadding)
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
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = stringResource(R.string.home_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        ShizukuStatusCard(
            isConnected = isConnected,
            hasPermission = hasPermission,
            onRequestPermission = onRequestPermission
        )

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.home_tips_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.home_tips_content),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
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
            onRequestPermission = {}
        )
    }
}