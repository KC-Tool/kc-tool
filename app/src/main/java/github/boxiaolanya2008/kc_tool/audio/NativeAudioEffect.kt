package github.boxiaolanya2008.kc_tool.audio

import android.media.AudioManager
import android.os.IBinder
import android.util.Log
import java.lang.reflect.Method

object NativeAudioEffect {
    private const val TAG = "NativeAudioEffect"

    private var audioManager: AudioManager? = null
    private var audioService: Any? = null
    private var setParametersMethod: Method? = null
    private var getParametersMethod: Method? = null

    fun init(context: android.content.Context) {
        audioManager = context.getSystemService(android.content.Context.AUDIO_SERVICE) as? AudioManager
        try {
            val serviceManagerClass = Class.forName("android.os.ServiceManager")
            val getServiceMethod = serviceManagerClass.getMethod("getService", String::class.java)
            val audioBinder = getServiceMethod.invoke(null, "audio") as? IBinder

            if (audioBinder != null) {
                val audioServiceStub = Class.forName("android.media.IAudioService\$Stub")
                val asInterfaceMethod = audioServiceStub.getMethod("asInterface", IBinder::class.java)
                audioService = asInterfaceMethod.invoke(null, audioBinder)
                Log.d(TAG, "AudioService obtained via reflection")
            }

            val audioManagerClass = AudioManager::class.java
            setParametersMethod = audioManagerClass.getMethod("setParameters", String::class.java)
            getParametersMethod = audioManagerClass.getMethod("getParameters", String::class.java)
            Log.d(TAG, "Methods obtained via reflection")
        } catch (e: Exception) {
            Log.e(TAG, "Reflection init failed", e)
        }
    }

    fun setParameters(params: String): Boolean {
        try {
            setParametersMethod?.invoke(audioManager, params)
            Log.d(TAG, "setParameters: $params")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "setParameters failed: $params", e)
            return false
        }
    }

    fun getParameters(key: String): String {
        return try {
            val result = getParametersMethod?.invoke(audioManager, key) as? String ?: ""
            Log.d(TAG, "getParameters($key) = $result")
            result
        } catch (e: Exception) {
            Log.e(TAG, "getParameters failed: $key", e)
            ""
        }
    }

    fun setPreset(type: Int) {
        setParameters("stereoeffect_exist=1")
        setParameters("vafx-settings=$type")
    }

    fun setEqBands(bands: IntArray) {
        val converted = IntArray(10) { i -> (bands[i] - 50) / 5 }
        val eqStr = converted.joinToString(",")
        setParameters("vafx-eq=$eqStr")
    }

    fun setBassBoost(strength: Int) {
        setParameters("vafx-bass=$strength")
    }

    fun setVirtualizer(strength: Int) {
        setParameters("vafx-virtualizer=$strength")
    }

    fun setReverb(delay: Int, roomLevel: Int, reflections: Int, reverbLevel: Int, density: Int, diffusion: Int) {
        val params = "$delay,$roomLevel,$reflections,$reverbLevel,$density,$diffusion,"
        setParameters("vafx-reverb=$params")
    }

    fun openEffect() {
        setParameters("stereoeffect_exist=1")
    }

    fun closeEffect() {
        setParameters("stereoeffect_exist=0")
        setParameters("vafx-settings=NONE")
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

    fun isAvailable(): Boolean = audioService != null
    fun hasPermission(): Boolean = true

    fun getStatus(): String {
        val sb = StringBuilder()
        sb.appendLine("AudioService: ${if (audioService != null) "OK" else "未获取"}")
        sb.appendLine("setParameters: ${if (setParametersMethod != null) "OK" else "未获取"}")
        sb.appendLine("getParameters: ${if (getParametersMethod != null) "OK" else "未获取"}")
        return sb.toString()
    }
}
