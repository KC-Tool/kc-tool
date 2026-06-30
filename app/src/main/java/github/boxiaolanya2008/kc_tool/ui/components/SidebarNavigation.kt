package github.boxiaolanya2008.kc_tool.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.automirrored.filled.PlaylistAddCheck
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class NavItem(val title: String, val icon: ImageVector) {
    Home("首页", Icons.Default.Home),
    LoopTool("循环工具", Icons.Default.BugReport),
    AudioEffect("全局音效", Icons.Default.GraphicEq),
    ProcessManager("进程管理", Icons.Default.Memory),
    AppManager("应用管理", Icons.Default.Apps),
    AppStoreOpt("应用商店优化", Icons.Default.ShoppingCart),
    GameOpt("游戏优化", Icons.Default.SportsEsports),
    Settings("设置", Icons.Default.Settings)
}

@Composable
fun AppDrawer(
    currentNav: NavItem,
    onNavClick: (NavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(modifier = modifier) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "kc-tool",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp)
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        NavItem.entries.forEach { item ->
            val selected = currentNav == item
            NavigationDrawerItem(
                label = { Text(item.title) },
                icon = { Icon(item.icon, contentDescription = null) },
                selected = selected,
                onClick = { onNavClick(item) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onMenuClick: () -> Unit,
    onWhitelistClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.AutoMirrored.Filled.MenuOpen, contentDescription = "\u83dc\u5355")
            }
        },
        actions = {
            IconButton(onClick = onWhitelistClick) {
                Icon(
                    Icons.AutoMirrored.Filled.PlaylistAddCheck,
                    contentDescription = "\u8bbe\u5907\u4f11\u7720\u767d\u540d\u5355"
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = modifier
    )
}