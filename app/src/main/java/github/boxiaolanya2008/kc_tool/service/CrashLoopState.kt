package github.boxiaolanya2008.kc_tool.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

object CrashLoopState {
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _crashCount = MutableStateFlow(0)
    val crashCount: StateFlow<Int> = _crashCount.asStateFlow()

    private val _totalCrashCount = MutableStateFlow(0)
    val totalCrashCount: StateFlow<Int> = _totalCrashCount.asStateFlow()

    private val _targetPackages = MutableStateFlow<List<String>>(emptyList())
    val targetPackages: StateFlow<List<String>> = _targetPackages.asStateFlow()

    private val _logs = MutableStateFlow<List<CrashLogEntry>>(emptyList())
    val logs: StateFlow<List<CrashLogEntry>> = _logs.asStateFlow()

    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    fun setRunning(running: Boolean) {
        _isRunning.value = running
    }

    fun setTargets(packages: List<String>) {
        _targetPackages.value = packages
    }

    fun incrementCrash(packageName: String, output: String = "") {
        _crashCount.value++
        _totalCrashCount.value++
        val entry = CrashLogEntry(
            packageName = packageName,
            timestamp = timeFormat.format(Date()),
            success = true,
            output = output
        )
        _logs.value = listOf(entry) + _logs.value.take(99)
    }

    fun failCrash(packageName: String, output: String = "") {
        _crashCount.value++
        _totalCrashCount.value++
        val entry = CrashLogEntry(
            packageName = packageName,
            timestamp = timeFormat.format(Date()),
            success = false,
            output = output
        )
        _logs.value = listOf(entry) + _logs.value.take(99)
    }

    fun resetCount() {
        _crashCount.value = 0
    }

    fun resetAll() {
        _isRunning.value = false
        _crashCount.value = 0
        _totalCrashCount.value = 0
        _targetPackages.value = emptyList()
        _logs.value = emptyList()
    }

    // FIXME: debug only, remove later
    private val _diagnostic = MutableStateFlow("")
    val diagnostic: StateFlow<String> = _diagnostic.asStateFlow()

    fun diag(msg: String) {
        _diagnostic.value = "${timeFormat.format(Date())}  $msg\n${_diagnostic.value.take(800)}"
    }
}

data class CrashLogEntry(
    val packageName: String,
    val timestamp: String,
    val success: Boolean,
    val output: String = ""
)