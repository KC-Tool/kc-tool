package github.boxiaolanya2008.kc_tool.viewmodel
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import github.boxiaolanya2008.kc_tool.service.ProcessInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import rikka.shizuku.Shizuku
class ProcessManagerViewModel(application: Application) : AndroidViewModel(application) {
    private val pm = application.packageManager
    private val prefs = application.getSharedPreferences("frozen_prefs", Context.MODE_PRIVATE)
    private val _processes = MutableStateFlow<List<ProcessInfo>>(emptyList())
    val processes: StateFlow<List<ProcessInfo>> = _processes.asStateFlow()
    private val _selectedProcesses = MutableStateFlow<Set<String>>(emptySet())
    val selectedProcesses: StateFlow<Set<String>> = _selectedProcesses.asStateFlow()
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _frozenProcesses = MutableStateFlow<Set<String>>(emptySet())
    val frozenProcesses: StateFlow<Set<String>> = _frozenProcesses.asStateFlow()
    init {
        loadFrozenState()
        loadProcesses()
    }
    private fun loadFrozenState() {
        val saved = prefs.getStringSet("frozen", emptySet()) ?: emptySet()
        _frozenProcesses.value = saved
    }
    private fun saveFrozenState() {
        prefs.edit().putStringSet("frozen", _frozenProcesses.value).apply()
    }
    fun loadProcesses() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val psOutput = execCommand("ps -A")
                val thirdPartyPkgs = getThirdPartyPackages()
                val procs = parsePsOutput(psOutput, thirdPartyPkgs)
                val enriched = procs.map { info ->
                    val icon = try {
                        pm.getApplicationIcon(info.packageName)
                    } catch (_: Exception) {
                        null
                    }
                    val appName = try {
                        pm.getApplicationInfo(info.packageName, 0).loadLabel(pm).toString()
                    } catch (_: Exception) {
                        ""
                    }
                    info.copy(icon = icon, processName = appName.ifEmpty { info.packageName })
                }
                _processes.value = enriched
            } catch (_: Exception) {
                _processes.value = emptyList()
            } finally {
                _isLoading.value = false
                sortProcesses()
            }
        }
    }
    private fun sortProcesses() {
        val frozen = _frozenProcesses.value
        _processes.value = _processes.value.sortedWith(
            compareBy<ProcessInfo> { it.packageName !in frozen }
                .thenBy { it.isSystemApp }
                .thenBy { it.packageName }
        )
    }
    private fun getThirdPartyPackages(): Set<String> {
        return try {
            val output = execCommand("pm list packages -3")
            output.lines()
                .filter { it.startsWith("package:") }
                .map { it.removePrefix("package:").trim() }
                .toSet()
        } catch (_: Exception) {
            emptySet()
        }
    }
    fun toggleProcess(packageName: String) {
        _selectedProcesses.value = if (packageName in _selectedProcesses.value) {
            _selectedProcesses.value - packageName
        } else {
            _selectedProcesses.value + packageName
        }
    }
    fun selectAll(processes: List<ProcessInfo>) {
        _selectedProcesses.value = processes.map { it.packageName }.toSet()
    }
    fun deselectAll() {
        _selectedProcesses.value = emptySet()
    }
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
    fun freezeProcess(packageName: String, sticky: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val command = if (sticky) {
                "dumpsys activity freeze --sticky $packageName"
            } else {
                "am freeze $packageName"
            }
            try {
                execCommand(command)
                _frozenProcesses.value = _frozenProcesses.value + packageName
                saveFrozenState()
                sortProcesses()
            } catch (_: Exception) {
            }
        }
    }
    fun unfreezeProcess(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                execCommand("am unfreeze $packageName")
                _frozenProcesses.value = _frozenProcesses.value - packageName
                saveFrozenState()
                sortProcesses()
            } catch (_: Exception) {
            }
        }
    }
    fun batchFreeze(sticky: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val targets = _selectedProcesses.value.toList()
            for (pkg in targets) {
                try {
                    val cmd = if (sticky) {
                        "dumpsys activity freeze --sticky $pkg"
                    } else {
                        "am freeze $pkg"
                    }
                    execCommand(cmd)
                    _frozenProcesses.value = _frozenProcesses.value + pkg
                } catch (_: Exception) {
                }
            }
            saveFrozenState()
            sortProcesses()
        }
    }
    fun batchUnfreeze() {
        viewModelScope.launch(Dispatchers.IO) {
            val targets = _selectedProcesses.value.toList()
            for (pkg in targets) {
                try {
                    execCommand("am unfreeze $pkg")
                    _frozenProcesses.value = _frozenProcesses.value - pkg
                } catch (_: Exception) {
                }
            }
            saveFrozenState()
            sortProcesses()
        }
    }
    private fun execCommand(command: String): String {
        val (out, err, code) = runBlocking {
            withTimeout(15_000) {
                val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
                coroutineScope {
                    val outDef = async(Dispatchers.IO) {
                        runCatching { process.inputStream.bufferedReader().readText() }.getOrDefault("")
                    }
                    val errDef = async(Dispatchers.IO) {
                        runCatching { process.errorStream.bufferedReader().readText() }.getOrDefault("")
                    }
                    val c = process.waitFor()
                    Triple(outDef.await(), errDef.await(), c)
                }
            }
        }
        if (code != 0) throw RuntimeException("exit=$code err=$err")
        return out.ifEmpty { err }
    }
    private fun parsePsOutput(output: String, thirdPartyPkgs: Set<String>): List<ProcessInfo> {
        val processes = mutableListOf<ProcessInfo>()
        val seen = mutableSetOf<String>()
        for (line in output.lines()) {
            val trimmed = line.trim()
            if (trimmed.startsWith("USER") || trimmed.startsWith("PID") || trimmed.isEmpty()) continue
            val parts = trimmed.split("\\s+".toRegex())
            if (parts.size < 3) continue
            val pid = parts[1].toIntOrNull() ?: parts[0].toIntOrNull() ?: continue
            val fullName = parts.last()
            val pkgName = if (fullName.contains("/")) {
                fullName.substringBefore("/")
            } else {
                fullName
            }.trimEnd('|', ' ', '\t', '\u0000', '\n', '\r')
            if (pkgName.isEmpty() || pkgName.startsWith("[") || pkgName.startsWith("/")) continue
            if (!seen.add(pkgName)) continue
            val isThirdParty = pkgName in thirdPartyPkgs
            processes.add(
                ProcessInfo(
                    packageName = pkgName,
                    processName = pkgName,
                    pid = pid,
                    uid = 0,
                    isSystemApp = !isThirdParty
                )
            )
        }
        return processes
    }
}