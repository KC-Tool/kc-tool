package github.boxiaolanya2008.kc_tool.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import github.boxiaolanya2008.kc_tool.audio.AudioEffectPreset
import github.boxiaolanya2008.kc_tool.audio.AudioSettingsManager
import github.boxiaolanya2008.kc_tool.audio.Constants
import github.boxiaolanya2008.kc_tool.audio.NativeAudioEngine
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AudioEffectScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    var currentPresetId by remember { mutableIntStateOf(0) }
    var eqBands by remember { mutableStateOf(IntArray(10) { 50 }) }
    var debugOutput by remember { mutableStateOf("") }
    var statusText by remember { mutableStateOf("就绪") }
    var isInit by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableIntStateOf(0) }
    var dolbyEnabled by remember { mutableStateOf(false) }
    var hifiEnabled by remember { mutableStateOf(false) }
    var selectedDevice by remember { mutableStateOf("speaker") }

    val bgColor = if (isDark) Color(0xFF0D1117) else Color(0xFFF6F8FA)
    val cardColor = if (isDark) Color(0xFF161B22) else Color(0xFFFFFFFF)
    val textColor = if (isDark) Color(0xFFC9D1D9) else Color(0xFF24292F)
    val subTextColor = if (isDark) Color(0xFF8B949E) else Color(0xFF656D76)
    val accentColor = if (isDark) Color(0xFF58A6FF) else Color(0xFF0969DA)
    val greenColor = Color(0xFF3FB950)
    val borderColor = if (isDark) Color(0xFF30363D) else Color(0xFFD0D7DE)

    LaunchedEffect(Unit) {
        AudioSettingsManager.init(context)
        isInit = NativeAudioEngine.initialize(context)
        statusText = if (isInit) "已绑定 session 0" else "初始化失败"
        val savedDolby = AudioSettingsManager.getDolby()
        dolbyEnabled = savedDolby
        if (savedDolby) NativeAudioEngine.setDolby(true)
        val savedHifi = AudioSettingsManager.getHiFi()
        hifiEnabled = savedHifi
        if (savedHifi) NativeAudioEngine.setHiFi(true)
        val savedPreset = AudioSettingsManager.getCurrentPreset()
        val savedEq = AudioSettingsManager.getEqBands()
        if (savedPreset > 0) {
            val preset = AudioEffectPreset.getPresetById(savedPreset)
            NativeAudioEngine.applyPreset(preset)
            currentPresetId = savedPreset
            eqBands = savedEq ?: preset.eqBands.copyOf()
            statusText = "已恢复: ${preset.name}"
        } else if (savedEq != null) {
            eqBands = savedEq
            NativeAudioEngine.setEqBands(eqBands)
            statusText = "已恢复自定义 EQ"
        }
    }

    DisposableEffect(Unit) {
        onDispose { NativeAudioEngine.release() }
    }

    Box(modifier = modifier.fillMaxSize().background(bgColor)) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(shape = RoundedCornerShape(16.dp), color = cardColor, shadowElevation = if (isDark) 0.dp else 1.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(accentColor.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.GraphicEq, null, tint = accentColor, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("深空音效", color = textColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("Android AudioEffect · session 0", color = subTextColor, fontSize = 12.sp)
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(if (isInit) greenColor else Color.Red))
                        Spacer(Modifier.width(8.dp))
                        Text(statusText, color = subTextColor, fontSize = 13.sp)
                    }
                }
            }

            if (isInit) {
                Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(cardColor).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf("音效", "EQ", "自定义").forEachIndexed { index, name ->
                        Box(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                                .background(if (selectedCategory == index) accentColor.copy(alpha = 0.15f) else Color.Transparent)
                                .padding(vertical = 10.dp)
                                .clickable { selectedCategory = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(name, color = if (selectedCategory == index) accentColor else subTextColor, fontSize = 13.sp, fontWeight = if (selectedCategory == index) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }

                when (selectedCategory) {
                    0 -> EffectCategory(currentPresetId, cardColor, textColor, subTextColor, accentColor, borderColor) { id, name ->
                        currentPresetId = id
                        val preset = AudioEffectPreset.getPresetById(id)
                        eqBands = preset.eqBands.copyOf()
                        NativeAudioEngine.applyPreset(preset)
                        AudioSettingsManager.saveCurrentPreset(id)
                        AudioSettingsManager.saveEqBands(eqBands)
                        statusText = name
                    }
                    1 -> EqCategory(currentPresetId, cardColor, textColor, subTextColor, accentColor, borderColor) { id, name ->
                        currentPresetId = id
                        val preset = AudioEffectPreset.getPresetById(id)
                        eqBands = preset.eqBands.copyOf()
                        NativeAudioEngine.applyPreset(preset)
                        AudioSettingsManager.saveCurrentPreset(id)
                        AudioSettingsManager.saveEqBands(eqBands)
                        statusText = name
                    }
                    2 -> CustomCategory(eqBands, cardColor, textColor, subTextColor, accentColor, borderColor) { band, level ->
                        eqBands = eqBands.copyOf().also { it[band] = level }
                        NativeAudioEngine.setEqBands(eqBands)
                        AudioSettingsManager.saveEqBands(eqBands)
                    }
                }

                Surface(shape = RoundedCornerShape(16.dp), color = cardColor, shadowElevation = if (isDark) 0.dp else 1.dp, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SurroundSound, null, tint = accentColor, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Dolby Atmos", color = textColor, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        }
                        Text("开启 Dolby Atmos 音效增强", color = subTextColor, fontSize = 12.sp)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("启用", color = textColor, fontSize = 14.sp)
                            Switch(checked = dolbyEnabled, onCheckedChange = {
                                dolbyEnabled = it
                                NativeAudioEngine.setDolby(it)
                                AudioSettingsManager.saveDolby(it)
                                statusText = if (it) "Dolby Atmos 已开启" else "Dolby Atmos 已关闭"
                            })
                        }
                    }
                }

                Surface(shape = RoundedCornerShape(16.dp), color = cardColor, shadowElevation = if (isDark) 0.dp else 1.dp, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HighQuality, null, tint = accentColor, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("HiFi 模式", color = textColor, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        }
                        Text("vivo HiFi 直通，绕过系统音效处理", color = subTextColor, fontSize = 12.sp)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("启用", color = textColor, fontSize = 14.sp)
                            Switch(checked = hifiEnabled, onCheckedChange = {
                                hifiEnabled = it
                                NativeAudioEngine.setHiFi(it)
                                AudioSettingsManager.saveHiFi(it)
                                statusText = if (it) "HiFi 模式已开启" else "HiFi 模式已关闭"
                            })
                        }
                    }
                }

                Surface(shape = RoundedCornerShape(16.dp), color = cardColor, shadowElevation = if (isDark) 0.dp else 1.dp, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speaker, null, tint = accentColor, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("输出设备", color = textColor, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            val devices = listOf("speaker" to "扬声器", "headset" to "耳机", "bluetooth" to "蓝牙", "usb" to "USB")
                            devices.forEach { (key, name) ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selectedDevice == key) accentColor.copy(alpha = 0.15f) else cardColor,
                                    border = if (selectedDevice == key) ButtonDefaults.outlinedButtonBorder(true) else null,
                                    modifier = Modifier.weight(1f).clickable {
                                        selectedDevice = key
                                        NativeAudioEngine.setOutputDevice(key)
                                    }
                                ) {
                                    Text(name, modifier = Modifier.padding(vertical = 8.dp), color = if (selectedDevice == key) accentColor else textColor, fontSize = 12.sp, textAlign = TextAlign.Center)
                                }
                            }
                        }
                    }
                }

                Surface(shape = RoundedCornerShape(16.dp), color = cardColor, shadowElevation = if (isDark) 0.dp else 1.dp, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Refresh, null, tint = Color.Red, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("重置", color = textColor, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        }
                        Button(onClick = {
                            NativeAudioEngine.disableAll()
                            NativeAudioEngine.setDolby(false)
                            NativeAudioEngine.setHiFi(false)
                            currentPresetId = 0
                            eqBands = IntArray(10) { 50 }
                            dolbyEnabled = false
                            hifiEnabled = false
                            AudioSettingsManager.saveCurrentPreset(0)
                            AudioSettingsManager.saveEqBands(eqBands)
                            AudioSettingsManager.saveDolby(false)
                            AudioSettingsManager.saveHiFi(false)
                            statusText = "已重置"
                        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))) {
                            Text("重置所有效果", color = Color.White)
                        }
                    }
                }

                Surface(shape = RoundedCornerShape(16.dp), color = cardColor, shadowElevation = if (isDark) 0.dp else 1.dp, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("调试", color = textColor, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            FilledTonalButton(onClick = { debugOutput = NativeAudioEngine.getStatus() }, modifier = Modifier.weight(1f)) { Text("状态") }
                            FilledTonalButton(onClick = { copyToClipboard(context, debugOutput) }, modifier = Modifier.weight(1f)) { Text("复制") }
                            FilledTonalButton(onClick = { exportToFile(context, debugOutput) }, modifier = Modifier.weight(1f)) { Text("导出") }
                        }
                        if (debugOutput.isNotEmpty()) {
                            Surface(shape = RoundedCornerShape(8.dp), color = bgColor, modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp)) {
                                Text(debugOutput, color = subTextColor, fontSize = 11.sp, modifier = Modifier.padding(8.dp).verticalScroll(rememberScrollState()))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EffectCategory(currentPresetId: Int, cardColor: Color, textColor: Color, subTextColor: Color, accentColor: Color, borderColor: Color, onPresetSelected: (Int, String) -> Unit) {
    AudioEffectPreset.getBuiltInPresets().chunked(2).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            row.forEach { preset ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (currentPresetId == preset.id) accentColor.copy(alpha = 0.12f) else cardColor,
                    border = if (currentPresetId == preset.id) ButtonDefaults.outlinedButtonBorder(true) else null,
                    modifier = Modifier.weight(1f).clickable { onPresetSelected(preset.id, preset.name) }
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(preset.name, color = if (currentPresetId == preset.id) accentColor else textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(preset.description, color = subTextColor, fontSize = 11.sp)
                    }
                }
            }
            if (row.size < 2) Spacer(modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun EqCategory(currentPresetId: Int, cardColor: Color, textColor: Color, subTextColor: Color, accentColor: Color, borderColor: Color, onPresetSelected: (Int, String) -> Unit) {
    AudioEffectPreset.getEqPresets().chunked(2).forEach { row ->
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            row.forEach { preset ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (currentPresetId == preset.id) accentColor.copy(alpha = 0.12f) else cardColor,
                    border = if (currentPresetId == preset.id) ButtonDefaults.outlinedButtonBorder(true) else null,
                    modifier = Modifier.weight(1f).clickable { onPresetSelected(preset.id, preset.name) }
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(preset.name, color = if (currentPresetId == preset.id) accentColor else textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text(preset.description, color = subTextColor, fontSize = 11.sp)
                    }
                }
            }
            if (row.size < 2) Spacer(modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun CustomCategory(eqBands: IntArray, cardColor: Color, textColor: Color, subTextColor: Color, accentColor: Color, borderColor: Color, onBandChanged: (Int, Int) -> Unit) {
    val isDark = isSystemInDarkTheme()
    Surface(shape = RoundedCornerShape(16.dp), color = cardColor, shadowElevation = if (isDark) 0.dp else 1.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("均衡器", color = textColor, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Constants.EQ_BAND_LABELS.forEach { label ->
                    Text(label, color = subTextColor, fontSize = 10.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                }
            }
            for (i in 0 until 10) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${((eqBands[i] - 50) * 0.4).toInt()}", color = subTextColor, fontSize = 10.sp, modifier = Modifier.width(28.dp), textAlign = TextAlign.End)
                    Slider(
                        value = eqBands[i].toFloat(),
                        onValueChange = { value -> onBandChanged(i, value.toInt()) },
                        valueRange = 0f..100f,
                        modifier = Modifier.weight(1f).height(24.dp),
                        colors = SliderDefaults.colors(thumbColor = accentColor, activeTrackColor = accentColor, inactiveTrackColor = borderColor)
                    )
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("audio_debug", text))
    Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT).show()
}

private fun exportToFile(context: Context, content: String) {
    try {
        val dir = context.getExternalFilesDir(null)
        val file = File(dir, "audio_debug_" + System.currentTimeMillis() + ".txt")
        file.writeText(content)
        Toast.makeText(context, "已导出到: " + file.absolutePath, Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "导出失败: " + e.message, Toast.LENGTH_SHORT).show()
    }
}
