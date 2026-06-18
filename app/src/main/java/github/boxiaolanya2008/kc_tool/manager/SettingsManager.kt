package github.boxiaolanya2008.kc_tool.manager

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsManager(context: Context) {
    companion object {
        private const val PREFS_NAME = "kc_tool_settings"
        private const val KEY_STEALTH_MODE = "stealth_mode"
        private const val KEY_DEFAULT_INTERVAL_SEC = "default_interval_sec"
        private const val KEY_DEFAULT_INTERVAL_MS = "default_interval_ms"
        private const val KEY_AUTO_START_ON_BOOT = "auto_start_on_boot"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _stealthMode = MutableStateFlow(prefs.getBoolean(KEY_STEALTH_MODE, false))
    val stealthMode: StateFlow<Boolean> = _stealthMode.asStateFlow()

    private val _defaultIntervalSec = MutableStateFlow(prefs.getString(KEY_DEFAULT_INTERVAL_SEC, "0") ?: "0")
    val defaultIntervalSec: StateFlow<String> = _defaultIntervalSec.asStateFlow()

    private val _defaultIntervalMs = MutableStateFlow(prefs.getString(KEY_DEFAULT_INTERVAL_MS, "500") ?: "500")
    val defaultIntervalMs: StateFlow<String> = _defaultIntervalMs.asStateFlow()

    private val _autoStartOnBoot = MutableStateFlow(prefs.getBoolean(KEY_AUTO_START_ON_BOOT, false))
    val autoStartOnBoot: StateFlow<Boolean> = _autoStartOnBoot.asStateFlow()

    fun setStealthMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_STEALTH_MODE, enabled).apply()
        _stealthMode.value = enabled
    }

    fun setDefaultIntervalSec(value: String) {
        prefs.edit().putString(KEY_DEFAULT_INTERVAL_SEC, value).apply()
        _defaultIntervalSec.value = value
    }

    fun setDefaultIntervalMs(value: String) {
        prefs.edit().putString(KEY_DEFAULT_INTERVAL_MS, value).apply()
        _defaultIntervalMs.value = value
    }

    fun setAutoStartOnBoot(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_START_ON_BOOT, enabled).apply()
        _autoStartOnBoot.value = enabled
    }
}