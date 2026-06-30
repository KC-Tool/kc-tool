package github.boxiaolanya2008.kc_tool.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HighlightOff
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.ui.graphics.vector.ImageVector

data class GameOptEntry(
    val key: String,
    val label: String,
    val description: String,
    val command: String,
    val icon: ImageVector
)

val gameOptEntries = listOf(
    GameOptEntry(
        key = "game_mode_on",
        label = "开启游戏模式",
        description = "向应用商店发送游戏模式开启广播",
        command = "dumpsys activity broadcast -a com.vivo.abe.gamemode.status -p com.bbk.appstore --ei status 1",
        icon = Icons.Filled.SportsEsports
    ),
    GameOptEntry(
        key = "game_mode_off",
        label = "关闭游戏模式",
        description = "向应用商店发送游戏模式关闭广播",
        command = "dumpsys activity broadcast -a com.vivo.abe.gamemode.status -p com.bbk.appstore --ei status 0",
        icon = Icons.Filled.HighlightOff
    )
)
