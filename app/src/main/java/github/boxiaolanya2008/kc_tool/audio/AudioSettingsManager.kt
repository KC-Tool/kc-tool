package github.boxiaolanya2008.kc_tool.audio

import android.content.Context
import android.content.SharedPreferences

object AudioSettingsManager {
    private const val PREFS_NAME = "audio_effect_settings"
    private const val KEY_CURRENT_PRESET = "current_preset"
    private const val KEY_EQ_BANDS = "eq_bands"
    private const val KEY_AUTO_START = "auto_start"
    private const val KEY_LAST_EQ = "last_eq"
    private const val KEY_DOLBY = "dolby_enabled"
    private const val KEY_HIFI = "hifi_enabled"

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveCurrentPreset(presetId: Int) {
        prefs?.edit()?.putInt(KEY_CURRENT_PRESET, presetId)?.apply()
    }

    fun getCurrentPreset(): Int = prefs?.getInt(KEY_CURRENT_PRESET, 0) ?: 0

    fun saveEqBands(bands: IntArray) {
        prefs?.edit()?.putString(KEY_EQ_BANDS, bands.joinToString(","))?.apply()
    }

    fun getEqBands(): IntArray? {
        val str = prefs?.getString(KEY_EQ_BANDS, null) ?: return null
        return try {
            str.split(",").map { it.trim().toInt() }.toIntArray()
        } catch (e: Exception) { null }
    }

    fun saveAutoStart(enabled: Boolean) {
        prefs?.edit()?.putBoolean(KEY_AUTO_START, enabled)?.apply()
    }

    fun getAutoStart(): Boolean = prefs?.getBoolean(KEY_AUTO_START, false) ?: false

    fun saveLastEq(bands: IntArray) {
        prefs?.edit()?.putString(KEY_LAST_EQ, bands.joinToString(","))?.apply()
    }

    fun getLastEq(): IntArray? {
        val str = prefs?.getString(KEY_LAST_EQ, null) ?: return null
        return try {
            str.split(",").map { it.trim().toInt() }.toIntArray()
        } catch (e: Exception) { null }
    }

    fun saveDolby(enabled: Boolean) {
        prefs?.edit()?.putBoolean(KEY_DOLBY, enabled)?.apply()
    }

    fun getDolby(): Boolean = prefs?.getBoolean(KEY_DOLBY, false) ?: false

    fun saveHiFi(enabled: Boolean) {
        prefs?.edit()?.putBoolean(KEY_HIFI, enabled)?.apply()
    }

    fun getHiFi(): Boolean = prefs?.getBoolean(KEY_HIFI, false) ?: false
}
