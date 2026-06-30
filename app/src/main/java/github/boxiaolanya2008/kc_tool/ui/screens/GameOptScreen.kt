package github.boxiaolanya2008.kc_tool.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import github.boxiaolanya2008.kc_tool.ui.anim.LottieKind
import github.boxiaolanya2008.kc_tool.ui.anim.LottieView
import github.boxiaolanya2008.kc_tool.ui.components.GameOptEntry
import github.boxiaolanya2008.kc_tool.ui.components.gameOptEntries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku

@Composable
fun GameOptScreen(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    var results by remember { mutableStateOf<Map<String, GameOptResult>>(emptyMap()) }
    var isRunning by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("游戏优化", style = MaterialTheme.typography.titleMedium)
                Text("控制 vivo 应用商店的游戏模式广播，开启/关闭游戏优化", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        gameOptEntries.forEach { entry ->
            val result = results[entry.key]
            GameOptCard(
                entry = entry,
                result = result,
                isRunning = isRunning,
                onExecute = {
                    isRunning = true
                    scope.launch {
                        val output = withContext(Dispatchers.IO) { execShell(entry.command) }
                        results = results + (entry.key to GameOptResult(output.second, output.first == 0))
                        isRunning = false
                    }
                }
            )
        }

        val hasResults = results.isNotEmpty()
        if (hasResults) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("执行结果", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    HorizontalDivider()
                    results.forEach { (key, result) ->
                        val entry = gameOptEntries.find { it.key == key } ?: return@forEach
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (result.success) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (result.success) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp).padding(top = 2.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(entry.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                if (result.output.isNotEmpty()) {
                                    Text(result.output.trim().take(200), style = MaterialTheme.typography.labelSmall, color = if (result.success) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class GameOptResult(val output: String, val success: Boolean)

@Composable
private fun GameOptCard(
    entry: GameOptEntry,
    result: GameOptResult?,
    isRunning: Boolean,
    onExecute: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = when {
            result == null -> null
            result.success -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            else -> BorderStroke(1.dp, MaterialTheme.colorScheme.error)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        entry.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(entry.label, style = MaterialTheme.typography.bodyLarge)
                Text(entry.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            FilledTonalButton(
                onClick = onExecute,
                enabled = !isRunning
            ) {
                Text("执行", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

private suspend fun execShell(command: String): Pair<Int, String> = withContext(Dispatchers.IO) {
    try {
        val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
        val outDef = async(Dispatchers.IO) {
            runCatching { process.inputStream.bufferedReader().readText() }.getOrDefault("")
        }
        val errDef = async(Dispatchers.IO) {
            runCatching { process.errorStream.bufferedReader().readText() }.getOrDefault("")
        }
        val code = process.waitFor()
        val out = outDef.await()
        val err = errDef.await()
        code to (out.ifEmpty { err })
    } catch (e: Exception) {
        -1 to e.message.orEmpty()
    }
}
