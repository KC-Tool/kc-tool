package github.boxiaolanya2008.kc_tool

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import github.boxiaolanya2008.kc_tool.shizuku.ShizukuManager
import github.boxiaolanya2008.kc_tool.service.CrashLoopService
import github.boxiaolanya2008.kc_tool.ui.components.ShizukuStatusCard
import github.boxiaolanya2008.kc_tool.ui.screens.CrashLoopScreen
import github.boxiaolanya2008.kc_tool.ui.theme.KctoolTheme

class MainActivity : ComponentActivity() {
    private lateinit var shizukuManager: ShizukuManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        shizukuManager = ShizukuManager()
        shizukuManager.initialize()

        setContent {
            KctoolTheme {
                val context = LocalContext.current

                DisposableEffect(Unit) {
                    onDispose {
                        CrashLoopService.stop(context)
                        shizukuManager.destroy()
                    }
                }

                MainApp(shizukuManager = shizukuManager)
            }
        }
    }
}

@Composable
private fun MainApp(shizukuManager: ShizukuManager) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var refreshVersion by remember { mutableIntStateOf(0) }

    val isConnected = remember(refreshVersion) { shizukuManager.isConnected() }
    val hasPermission = remember(refreshVersion) { shizukuManager.hasPermission() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_home)) },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Warning, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_crash_loop)) },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> HomeScreen(
                isConnected = isConnected,
                hasPermission = hasPermission,
                onRequestPermission = {
                    shizukuManager.checkPermission()
                    refreshVersion++
                },
                modifier = Modifier.padding(innerPadding)
            )
            1 -> CrashLoopScreen(modifier = Modifier.padding(innerPadding))
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
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium
        )

        ShizukuStatusCard(
            isConnected = isConnected,
            hasPermission = hasPermission,
            onRequestPermission = onRequestPermission
        )
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