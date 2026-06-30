package github.boxiaolanya2008.kc_tool.audio

import android.media.audiofx.AudioEffect
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import android.util.Log

object NativeAudioEngine {
    private const val TAG = "NativeAudioEngine"
    private const val SESSION_OUTPUT_MIX = 0

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var presetReverb: PresetReverb? = null

    private var isInitialized = false
    private var currentPresetId = 0
    var dolbyEnabled = false
        private set
    var hifiEnabled = false
        private set

    private var appContext: android.content.Context? = null

    fun initialize(context: android.content.Context? = null): Boolean {
        appContext = context
        try {
            equalizer = Equalizer(0, SESSION_OUTPUT_MIX).apply { enabled = true }
            bassBoost = BassBoost(0, SESSION_OUTPUT_MIX).apply { enabled = false }
            virtualizer = Virtualizer(0, SESSION_OUTPUT_MIX).apply { enabled = false }
            presetReverb = PresetReverb(0, SESSION_OUTPUT_MIX).apply { enabled = false }
            isInitialized = true
            Log.i(TAG, "Initialized on session 0 (OUTPUT_MIX)")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Init failed", e)
            isInitialized = false
            return false
        }
    }

    fun setEqBand(band: Int, level: Int) {
        if (!isInitialized) return
        try {
            val eq = equalizer ?: return
            val bandCount = eq.numberOfBands.toInt()
            if (band >= bandCount) return
            val range = eq.bandLevelRange
            val minLevel = range[0].toInt()
            val maxLevel = range[1].toInt()
            val millibels = ((level - 50) * 20).toShort()
            val clamped = millibels.toInt().coerceIn(minLevel, maxLevel).toShort()
            eq.setBandLevel(band.toShort(), clamped)
            Log.d(TAG, "EQ band $band = $level (${clamped}mB)")
        } catch (e: Exception) {
            Log.e(TAG, "setEqBand failed", e)
        }
    }

    fun setEqBands(bands: IntArray) {
        for (i in bands.indices) {
            setEqBand(i, bands[i])
        }
    }

    fun setBassBoost(strength: Int) {
        if (!isInitialized) return
        try {
            val bb = bassBoost ?: return
            if (strength > 0) {
                bb.setStrength(strength.toShort())
                bb.enabled = true
            } else {
                bb.enabled = false
            }
            Log.d(TAG, "BassBoost = $strength")
        } catch (e: Exception) {
            Log.e(TAG, "setBassBoost failed", e)
        }
    }

    fun setVirtualizer(strength: Int) {
        if (!isInitialized) return
        try {
            val v = virtualizer ?: return
            if (strength > 0) {
                v.setStrength(strength.toShort())
                v.enabled = true
            } else {
                v.enabled = false
            }
            Log.d(TAG, "Virtualizer = $strength")
        } catch (e: Exception) {
            Log.e(TAG, "setVirtualizer failed", e)
        }
    }

    fun setReverbPreset(preset: Short) {
        if (!isInitialized) return
        try {
            val r = presetReverb ?: return
            if (preset > 0) {
                r.preset = preset
                r.enabled = true
            } else {
                r.enabled = false
            }
            Log.d(TAG, "Reverb preset = $preset")
        } catch (e: Exception) {
            Log.e(TAG, "setReverbPreset failed", e)
        }
    }

    fun setDolby(enabled: Boolean) {
        dolbyEnabled = enabled
        val param = if (enabled) "1" else "0"
        try {
            val audioManagerClass = Class.forName("android.media.AudioManager")
            val setParamMethod = audioManagerClass.getMethod("setParameters", String::class.java)
            setParamMethod.invoke(null, "dolby_speaker_switch=$param")
            setParamMethod.invoke(null, "dolby_head_switch=$param")
            setParamMethod.invoke(null, "dolby_a2sd_switch=$param")
            Log.i(TAG, "Dolby set to: $enabled")
        } catch (e: Exception) {
            Log.e(TAG, "setDolby failed", e)
        }
    }

    fun setOutputDevice(device: String) {
        try {
            val audioManagerClass = Class.forName("android.media.AudioManager")
            val setParamMethod = audioManagerClass.getMethod("setParameters", String::class.java)
            when (device) {
                "speaker" -> setParamMethod.invoke(null, "routing=2")
                "headset" -> setParamMethod.invoke(null, "routing=4")
                "bluetooth" -> setParamMethod.invoke(null, "routing=8")
                "usb" -> setParamMethod.invoke(null, "routing=2048")
            }
            Log.i(TAG, "Output device set to: $device")
        } catch (e: Exception) {
            Log.e(TAG, "setOutputDevice failed", e)
        }
    }

    fun setHiFi(enabled: Boolean) {
        hifiEnabled = enabled
        try {
            val ctx = appContext ?: return
            val resolver = ctx.contentResolver
            val settingsClass = Class.forName("android.provider.Settings\$System")
            val putIntMethod = settingsClass.getMethod("putInt",
                android.content.ContentResolver::class.java,
                String::class.java,
                Int::class.javaPrimitiveType!!
            )
            putIntMethod.invoke(null, resolver, "hifi_settings_music", if (enabled) 1 else 0)

            val intent = android.content.Intent("com.vivo.action.HIFI_STATE_CHANGED")
            intent.setPackage("com.vivo.audiofx")
            ctx.sendBroadcast(intent)
            Log.i(TAG, "HiFi set to: $enabled")
        } catch (e: Exception) {
            Log.e(TAG, "setHiFi failed", e)
        }
    }

    fun applyPreset(preset: AudioEffectPreset) {
        if (!isInitialized) return
        if (preset.id == 0) {
            disableAll()
            return
        }
        setEqBands(preset.eqBands)
        setBassBoost(preset.bassBoost.toInt())
        setVirtualizer(preset.virtualizer.toInt())
        setReverbPreset(preset.reverbPreset)
        currentPresetId = preset.id
        Log.i(TAG, "Applied preset: ${preset.name}")
    }

    fun disableAll() {
        equalizer?.enabled = false
        bassBoost?.enabled = false
        virtualizer?.enabled = false
        presetReverb?.enabled = false
        currentPresetId = 0
        Log.i(TAG, "All effects disabled")
    }

    fun release() {
        equalizer?.release()
        bassBoost?.release()
        virtualizer?.release()
        presetReverb?.release()
        isInitialized = false
    }

    fun isInitialized() = isInitialized
    fun getCurrentPresetId() = currentPresetId

    fun getStatus(): String {
        val sb = StringBuilder()
        sb.appendLine("初始化: $isInitialized")
        sb.appendLine("当前预设: $currentPresetId")
        sb.appendLine("Dolby: $dolbyEnabled")
        if (isInitialized) {
            sb.appendLine("EQ 频段数: ${equalizer?.numberOfBands ?: 0}")
            sb.appendLine("EQ 范围: ${equalizer?.bandLevelRange?.let { "${it[0]}~${it[1]}mB" } ?: "N/A"}")
            sb.appendLine("BassBoost: ${bassBoost?.enabled ?: false}")
            sb.appendLine("Virtualizer: ${virtualizer?.enabled ?: false}")
            sb.appendLine("Reverb: ${presetReverb?.enabled ?: false}")
        }
        return sb.toString()
    }
}
