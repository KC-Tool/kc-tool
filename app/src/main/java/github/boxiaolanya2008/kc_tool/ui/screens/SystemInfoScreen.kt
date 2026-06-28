package github.boxiaolanya2008.kc_tool.ui.screens

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import rikka.shizuku.Shizuku
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemInfoScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memInfo = ActivityManager.MemoryInfo()
    am.getMemoryInfo(memInfo)

    val totalRam = memInfo.totalMem / (1024 * 1024)
    val availRam = memInfo.availMem / (1024 * 1024)

    val storage = StatFs(Environment.getDataDirectory().path)
    val totalStorage = storage.totalBytes / (1024 * 1024 * 1024)
    val availStorage = storage.availableBytes / (1024 * 1024 * 1024)

    var uptime by remember { mutableStateOf("") }
    var cpuInfo by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            val uptimeMs = System.currentTimeMillis() - android.os.SystemClock.elapsedRealtime()
            val days = uptimeMs / (1000 * 60 * 60 * 24)
            val hours = (uptimeMs % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
            val mins = (uptimeMs % (1000 * 60 * 60)) / (1000 * 60)
            uptime = "${days}天 ${hours}小时 ${mins}分钟"

            val cpuRaw = try {
                val p = Shizuku.newProcess(arrayOf("sh", "-c", "cat /proc/cpuinfo | head -20"), null, null)
                p.inputStream.bufferedReader().readText()
            } catch (_: Exception) { "" }
            cpuInfo = cpuRaw.lines()
                .filter { it.contains("Hardware") || it.contains("model name") || it.contains("Processor") }
                .joinToString("\n") { it.trim() }
                .ifEmpty { Build.HARDWARE }
        } catch (_: Exception) {}
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("设备信息", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                InfoRow("品牌", Build.BRAND)
                InfoRow("型号", Build.MODEL)
                InfoRow("设备", Build.DEVICE)
                InfoRow("Android 版本", Build.VERSION.RELEASE)
                InfoRow("API 级度", "${Build.VERSION.SDK_INT}")
                InfoRow("主板", Build.BOARD)
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("硬件信息", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
                InfoRow("CPU", cpuInfo.ifEmpty { Build.HARDWARE })
                InfoRow("总内存", "${totalRam} MB")
                InfoRow("可用内存", "${availRam} MB")
                InfoRow("内存使用", "${totalRam - availRam} MB (${((totalRam - availRam) * 100 / totalRam)}%)")
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("存储信息", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
                InfoRow("总存储", "${totalStorage} GB")
                InfoRow("可用存储", "${availStorage} GB")
                InfoRow("已用存储", "${totalStorage - availStorage} GB")
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("系统信息", style = MaterialTheme.typography.titleMedium)
                InfoRow("系统版本", Build.DISPLAY)
                InfoRow("内核版本", System.getProperty("os.version") ?: "N/A")
                InfoRow("运行时间", uptime)
                InfoRow("ABI", Build.SUPPORTED_ABIS.firstOrNull() ?: "N/A")
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}