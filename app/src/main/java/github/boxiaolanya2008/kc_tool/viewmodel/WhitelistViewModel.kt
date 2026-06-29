package github.boxiaolanya2008.kc_tool.viewmodel

import android.app.Application
import android.content.pm.ApplicationInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import github.boxiaolanya2008.kc_tool.ui.screens.AppEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import rikka.shizuku.Shizuku

class WhitelistViewModel(application: Application) : AndroidViewModel(application) {

    private val pm = application.packageManager

    private val _whitelist = MutableStateFlow<List<AppEntry>>(emptyList())
    val whitelist: StateFlow<List<AppEntry>> = _whitelist.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _rawOutput = MutableStateFlow("")
    val rawOutput: StateFlow<String> = _rawOutput.asStateFlow()

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.value = true
            try {
                val (out, _, _) = execCommand("dumpsys deviceidle whitelist")
                _rawOutput.value = out
                val pkgs = parseWhitelist(out)
                _whitelist.value = pkgs.mapNotNull { pkg ->
                    try {
                        val info = pm.getApplicationInfo(pkg, 0)
                        AppEntry(
                            packageName = pkg,
                            appName = info.loadLabel(pm).toString(),
                            isSystemApp = (info.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                                          && (info.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0,
                            icon = try { pm.getApplicationIcon(info) } catch (_: Exception) { null }
                        )
                    } catch (_: Exception) { null }
                }
            } catch (e: Exception) {
                _rawOutput.value = "刷新失败: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    private suspend fun execCommand(command: String): Triple<String, String, Int> = withTimeout(15_000) {
        val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
        coroutineScope {
            val outDef = async(Dispatchers.IO) {
                runCatching { process.inputStream.bufferedReader().readText() }.getOrDefault("")
            }
            val errDef = async(Dispatchers.IO) {
                runCatching { process.errorStream.bufferedReader().readText() }.getOrDefault("")
            }
            val code = process.waitFor()
            Triple(outDef.await(), errDef.await(), code)
        }
    }

    private fun parseWhitelist(output: String): List<String> {
        val pkgs = mutableListOf<String>()
        for (raw in output.lines()) {
            val line = raw.trim()
            if (line.isEmpty()) continue
            if (line.startsWith("Whitelist")) continue
            if (line.endsWith(":")) continue
            if (line.contains(' ')) continue
            if (!line.contains('.')) continue
            pkgs.add(line)
        }
        return pkgs
    }
}
