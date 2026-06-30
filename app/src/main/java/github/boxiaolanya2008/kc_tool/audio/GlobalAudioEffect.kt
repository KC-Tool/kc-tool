package github.boxiaolanya2008.kc_tool.audio

import android.os.IBinder
import android.util.Log
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.lang.reflect.Method
import java.lang.reflect.Proxy

object GlobalAudioEffect {
    private const val TAG = "GlobalAudioEffect"

    private var audioFlinger: Any? = null
    private var audioPolicyService: Any? = null
    private var isInitialized = false

    fun initialize(): Boolean {
        try {
            val serviceManagerClass = Class.forName("android.os.ServiceManager")
            val getServiceMethod = serviceManagerClass.getMethod("getService", String::class.java)

            val audioBinder = getServiceMethod.invoke(null, "audio") as? IBinder
            if (audioBinder == null) {
                Log.e(TAG, "Failed to get audio service binder")
                return false
            }

            val audioServiceStub = Class.forName("android.media.IAudioService\$Stub")
            val asInterfaceMethod = audioServiceStub.getMethod("asInterface", IBinder::class.java)
            val audioService = asInterfaceMethod.invoke(null, audioBinder)
            Log.d(TAG, "AudioService obtained: ${audioService?.javaClass?.name}")

            val audioFlingerBinder = getServiceMethod.invoke(null, "media.audio_flinger") as? IBinder
            if (audioFlingerBinder != null) {
                Log.d(TAG, "AudioFlinger binder obtained")
            }

            isInitialized = true
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Initialize failed", e)
            return false
        }
    }

    fun execShell(cmd: String): String {
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

    fun setGlobalEq(bands: IntArray) {
        for (i in bands.indices) {
            val freq = Constants.EQ_BAND_FREQUENCIES[i]
            val db = ((bands[i] - 50) * 0.4).toInt()
            execShell("cmd audio set-parameter music_eq_band_${freq}hz ${db}")
        }
    }

    fun setBassBoost(strength: Int) {
        execShell("cmd audio set-parameter music_bass_boost $strength")
    }

    fun setVirtualizer(strength: Int) {
        execShell("cmd audio set-parameter music_virtualizer $strength")
    }

    fun setReverb(preset: Int) {
        execShell("cmd audio set-parameter music_reverb_preset $preset")
    }

    fun applyPreset(preset: AudioEffectPreset) {
        if (preset.id == 0) {
            resetAll()
            return
        }
        setGlobalEq(preset.eqBands)
        setBassBoost(preset.bassBoost.toInt())
        setVirtualizer(preset.virtualizer.toInt())
        if (preset.reverbPreset > 0) {
            setReverb(preset.reverbPreset.toInt())
        }
    }

    fun resetAll() {
        setGlobalEq(IntArray(10) { 50 })
        setBassBoost(0)
        setVirtualizer(0)
        setReverb(0)
    }

    fun applyEqBand(band: Int, level: Int) {
        val freq = Constants.EQ_BAND_FREQUENCIES[band.coerceIn(0, 9)]
        val db = ((level - 50) * 0.4).toInt()
        execShell("cmd audio set-parameter music_eq_band_${freq}hz ${db}")
    }

    fun setStreamVolume(stream: Int, index: Int) {
        execShell("cmd audio set-stream-volume $stream $index")
    }

    fun isAvailable(): Boolean = try { Shizuku.pingBinder() } catch (e: Exception) { false }

    fun hasPermission(): Boolean = try { Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED } catch (e: Exception) { false }

    fun getDump(): String = execShell("dumpsys media.audio_flinger | grep -A 5 'Effect\\|session 0'")
}
