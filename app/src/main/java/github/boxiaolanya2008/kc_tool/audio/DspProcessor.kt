package github.boxiaolanya2008.kc_tool.audio

import android.media.audiofx.BassBoost
import android.media.audiofx.EnvironmentalReverb
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import android.util.Log

class DspProcessor(private val sessionId: Int) {
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var presetReverb: PresetReverb? = null
    private var envReverb: EnvironmentalReverb? = null
    private var currentPresetId = 0
    private var customEqBands = IntArray(10) { 50 }
    private var isInitialized = false

    fun initialize() {
        try {
            equalizer = Equalizer(0, sessionId).apply { enabled = true }
            bassBoost = BassBoost(0, sessionId).apply { enabled = false }
            virtualizer = Virtualizer(0, sessionId).apply { enabled = false }
            presetReverb = PresetReverb(0, sessionId).apply { enabled = false }
            envReverb = EnvironmentalReverb(0, sessionId).apply { enabled = false }
            isInitialized = true
        } catch (e: Exception) { Log.e("DspProcessor", "Init failed", e) }
    }

    fun applyPreset(preset: AudioEffectPreset) {
        if (!isInitialized) return
        currentPresetId = preset.id
        if (preset.id == 0) { disableAll(); return }
        applyEq(preset.toEqMillibels())
        applyBassBoost(preset.bassBoost)
        applyVirtualizer(preset.virtualizer)
        if (preset.envReverb != null) applyEnvironmentalReverb(preset.envReverb)
        else applyPresetReverb(preset.reverbPreset)
    }

    fun setCustomEq(bands: IntArray) {
        if (!isInitialized || bands.size != 10) return
        customEqBands = bands.copyOf()
        currentPresetId = -1
        applyEq(AudioEffectPreset(id = -1, name = "", description = "", eqBands = bands).toEqMillibels())
    }

    fun getEqBands(): IntArray = customEqBands.copyOf()

    private fun applyEq(millibels: ShortArray) {
        equalizer?.let { eq ->
            try {
                val range = eq.bandLevelRange
                val minLevel = range[0].toInt()
                val maxLevel = range[1].toInt()
                for (i in millibels.indices) {
                    eq.setBandLevel(i.toShort(), millibels[i].toInt().coerceIn(minLevel, maxLevel).toShort())
                }
                eq.enabled = true
            } catch (e: Exception) { Log.e("DspProcessor", "EQ failed", e) }
        }
    }

    private fun applyBassBoost(strength: Short) {
        bassBoost?.let {
            try { if (strength > 0) { it.setStrength(strength); it.enabled = true } else it.enabled = false }
            catch (e: Exception) { Log.e("DspProcessor", "BassBoost failed", e) }
        }
    }

    private fun applyVirtualizer(strength: Short) {
        virtualizer?.let {
            try { if (strength > 0) { it.setStrength(strength); it.enabled = true } else it.enabled = false }
            catch (e: Exception) { Log.e("DspProcessor", "Virtualizer failed", e) }
        }
    }

    private fun applyPresetReverb(preset: Short) {
        envReverb?.enabled = false
        presetReverb?.let {
            try { if (preset > 0) { it.preset = preset; it.enabled = true } else it.enabled = false }
            catch (e: Exception) { Log.e("DspProcessor", "PresetReverb failed", e) }
        }
    }

    private fun applyEnvironmentalReverb(params: AudioEffectPreset.EnvReverbParams) {
        presetReverb?.enabled = false
        envReverb?.let {
            try {
                it.roomLevel = params.roomLevel
                it.reflectionsLevel = params.reflectionsLevel
                it.reverbLevel = params.reverbLevel
                it.decayTime = params.decayTime.toInt()
                it.density = params.density
                it.diffusion = params.diffusion
                it.enabled = true
            } catch (e: Exception) { applyPresetReverb(2) }
        }
    }

    private fun disableAll() {
        equalizer?.enabled = false; bassBoost?.enabled = false
        virtualizer?.enabled = false; presetReverb?.enabled = false; envReverb?.enabled = false
    }

    fun release() {
        equalizer?.release(); bassBoost?.release(); virtualizer?.release()
        presetReverb?.release(); envReverb?.release(); isInitialized = false
    }

    fun getCurrentPresetId() = currentPresetId
    fun isEffectEnabled() = currentPresetId != 0
}
