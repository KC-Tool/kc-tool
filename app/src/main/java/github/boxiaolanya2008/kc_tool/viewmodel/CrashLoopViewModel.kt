package github.boxiaolanya2008.kc_tool.viewmodel

import android.app.Application
import android.content.pm.ApplicationInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import github.boxiaolanya2008.kc_tool.manager.LogManager
import github.boxiaolanya2008.kc_tool.manager.SettingsManager
import github.boxiaolanya2008.kc_tool.service.CrashLoopState
import github.boxiaolanya2008.kc_tool.ui.screens.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import rikka.shizuku.Shizuku

class CrashLoopViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsManager = SettingsManager(getApplication())

    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps.asStateFlow()

    private val _selectedApps = MutableStateFlow<Set<AppInfo>>(emptySet())
    val selectedApps: StateFlow<Set<AppInfo>> = _selectedApps.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _seconds = MutableStateFlow("0")
    val seconds: StateFlow<String> = _seconds.asStateFlow()

    private val _milliseconds = MutableStateFlow("500")
    val milliseconds: StateFlow<String> = _milliseconds.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private var loopJob: Job? = null

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val pm = getApplication<Application>().packageManager
                val packages = pm.getInstalledPackages(android.content.pm.PackageManager.GET_META_DATA)
                val loaded = packages.mapNotNull { info ->
                    val appInfo = info.applicationInfo ?: return@mapNotNull null
                    AppInfo(
                        packageName = info.packageName,
                        appName = appInfo.loadLabel(pm).toString(),
                        isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    )
                }.sortedWith(compareByDescending<AppInfo> { it.isSystemApp }.thenBy { it.appName })
                _apps.value = loaded

                val saved = settingsManager.lastSelectedPackages.value
                if (saved.isNotEmpty()) {
                    _selectedApps.value = loaded.filter { it.packageName in saved }.toSet()
                }
            } catch (e: Exception) {
                Log.e("CrashLoopVM", "loadApps failed", e)
                CrashLoopState.diag("loadApps error: ${e.javaClass.simpleName} ${e.message}")
            }
        }
    }

    fun toggleApp(app: AppInfo) {
        _selectedApps.value = if (app in _selectedApps.value) {
            _selectedApps.value - app
        } else {
            _selectedApps.value + app
        }
        saveSelection()
    }

    fun selectAll(filteredApps: List<AppInfo>) {
        _selectedApps.value = filteredApps.toSet()
        saveSelection()
    }

    fun deselectAll() {
        _selectedApps.value = emptySet()
        saveSelection()
    }

    private fun saveSelection() {
        settingsManager.setLastSelectedPackages(_selectedApps.value.map { it.packageName }.toSet())
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSeconds(value: String) {
        if (value.all { it.isDigit() }) _seconds.value = value
    }

    fun updateMilliseconds(value: String) {
        if (value.all { it.isDigit() }) _milliseconds.value = value
    }

    fun setRunning(running: Boolean) {
        _isRunning.value = running
    }

    fun getTotalMs(): Long {
        return (_seconds.value.toLongOrNull() ?: 0) * 1000 +
                (_milliseconds.value.toLongOrNull() ?: 0)
    }

    fun startNoNotificationLoop(packages: List<String>, intervalMs: Long) {
        if (loopJob?.isActive == true) return
        _isRunning.value = true
        CrashLoopState.setRunning(true)
        CrashLoopState.setTargets(packages)
        CrashLoopState.resetCount()
        val logMgr = LogManager.get(getApplication())
        loopJob = viewModelScope.launch(Dispatchers.IO) {
            logMgr.write("CrashLoopVM", "no-notification loop started pkgs=${packages.size}")
            while (true) {
                for (pkg in packages) {
                    try {
                        val output = execCommandNoService("am crash $pkg")
                        CrashLoopState.incrementCrash(pkg, output)
                        logMgr.write("CrashLoopVM", "OK $pkg")
                    } catch (e: Exception) {
                        CrashLoopState.failCrash(pkg, e.message ?: "")
                        logMgr.write("CrashLoopVM", "FAIL $pkg: ${e.message}", isError = true)
                    }
                }
                delay(intervalMs)
            }
        }
    }

    fun stopNoNotificationLoop() {
        loopJob?.cancel()
        loopJob = null
        _isRunning.value = false
        CrashLoopState.setRunning(false)
        LogManager.get(getApplication()).write("CrashLoopVM", "no-notification loop stopped")
    }

    private fun execCommandNoService(command: String): String {
        val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
        val output = process.inputStream.bufferedReader().readText()
        val error = process.errorStream.bufferedReader().readText()
        val exitCode = process.waitFor()
        if (exitCode != 0) throw RuntimeException("exit=$exitCode err=$error")
        return output.ifEmpty { error }
    }
}