package github.boxiaolanya2008.kc_tool.viewmodel

import android.app.Application
import android.content.pm.ApplicationInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import github.boxiaolanya2008.kc_tool.ui.screens.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CrashLoopViewModel(application: Application) : AndroidViewModel(application) {

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

    init {
        loadApps()
    }

    private fun loadApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val pm = getApplication<Application>().packageManager
            val packages = pm.getInstalledPackages(android.content.pm.PackageManager.GET_META_DATA)
            _apps.value = packages.mapNotNull { info ->
                val appInfo = info.applicationInfo ?: return@mapNotNull null
                AppInfo(
                    packageName = info.packageName,
                    appName = appInfo.loadLabel(pm).toString(),
                    isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                )
            }.sortedWith(compareByDescending<AppInfo> { it.isSystemApp }.thenBy { it.appName })
        }
    }

    fun toggleApp(app: AppInfo) {
        _selectedApps.value = if (app in _selectedApps.value) {
            _selectedApps.value - app
        } else {
            _selectedApps.value + app
        }
    }

    fun selectAll(filteredApps: List<AppInfo>) {
        _selectedApps.value = filteredApps.toSet()
    }

    fun deselectAll() {
        _selectedApps.value = emptySet()
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
}