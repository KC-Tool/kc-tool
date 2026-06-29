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
    Home("\u9996\u9875", Icons.Default.Home),
    LoopTool("\u5faa\u73af\u5de5\u5177", Icons.Default.BugReport),
    ProcessManager("\u8fdb\u7a0b\u7ba1\u7406", Icons.Default.Memory),
    AppManager("\u5e94\u7528\u7ba1\u7406", Icons.Default.Apps),
    Settings("\u8bbe\u7f6e", Icons.Default.Settings)
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