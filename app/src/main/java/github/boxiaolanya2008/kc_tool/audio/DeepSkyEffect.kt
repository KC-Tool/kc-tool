package github.boxiaolanya2008.kc_tool.audio

import android.util.Log
import rikka.shizuku.Shizuku

object DeepSkyEffect {
    private const val TAG = "DeepSkyEffect"

    fun init(context: android.content.Context) {}

    fun isSupported(): Boolean = true

    fun setPreset(type: Int) {
        execShell("cmd audio set-parameter stereo_effect_type $type")
        execShell("cmd audio set-parameter vafx-settings $type")
    }

    fun setEqBands(bands: IntArray) {
        val converted = IntArray(10) { i -> (bands[i] - 50) / 5 }
        val eqStr = converted.joinToString(",")
        execShell("cmd audio set-parameter vafx-eq $eqStr")
    }

    fun setBassBoost(strength: Int) {
        execShell("cmd audio set-parameter vafx-bass $strength")
    }

    fun setVirtualizer(strength: Int) {
        execShell("cmd audio set-parameter vafx-virtualizer $strength")
    }

    fun setReverb(delay: Int, roomLevel: Int, reflections: Int, reverbLevel: Int, density: Int, diffusion: Int) {
        val params = "$delay,$roomLevel,$reflections,$reverbLevel,$density,$diffusion,"
        execShell("cmd audio set-parameter vafx-reverb $params")
    }

    fun openEffect() {
        execShell("cmd audio set-parameter stereoeffect_exist 1")
    }

    fun closeEffect() {
        execShell("cmd audio set-parameter stereoeffect_exist 0")
        execShell("cmd audio set-parameter vafx-settings NONE")
    }

    fun applyPreset(preset: AudioEffectPreset) {
        if (preset.id == 0) {
            closeEffect()
            return
        }
        openEffect()
        setPreset(preset.id)
        setEqBands(preset.eqBands)
        if (preset.bassBoost > 0) setBassBoost(preset.bassBoost.toInt())
        if (preset.virtualizer > 0) setVirtualizer(preset.virtualizer.toInt())
        if (preset.envReverb != null) {
            val rv = preset.envReverb!!
            setReverb(rv.roomHfRatio.toInt(), rv.roomLevel.toInt(), rv.reflectionsLevel.toInt(), rv.reverbLevel.toInt(), rv.density.toInt(), rv.diffusion.toInt())
        }
    }

    fun resetAll() {
        closeEffect()
    }

    private fun execShell(cmd: String): String {
        return try {
            val process = Shizuku.newProcess(arrayOf("sh", "-c", cmd), null, null)
            val output = process.inputStream.bufferedReader().readText()
            val error = process.errorStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            Log.d(TAG, "shell=$cmd exit=$exitCode")
            output.ifEmpty { error }
        } catch (e: Exception) {
            Log.e(TAG, "execShell failed: $cmd", e)
            ""
        }
    }

    fun isAvailable(): Boolean = try { Shizuku.pingBinder() } catch (e: Exception) { false }
    fun hasPermission(): Boolean = try { Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED } catch (e: Exception) { false }

    fun getStatus(): String {
        val sb = StringBuilder()
        sb.appendLine("Shizuku: ${isAvailable()} | 权限: ${hasPermission()}")
        return sb.toString()
    }
}
