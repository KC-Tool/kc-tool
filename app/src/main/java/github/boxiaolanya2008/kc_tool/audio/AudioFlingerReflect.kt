package github.boxiaolanya2008.kc_tool.audio

import android.os.IBinder
import android.util.Log
import rikka.shizuku.Shizuku
import java.io.BufferedReader

object AudioFlingerReflect {
    private const val TAG = "AudioFlingerReflect"

    fun execShell(command: String): String {
        return try {
            val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
            val output = process.inputStream.bufferedReader().readText()
            val error = process.errorStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            Log.d(TAG, "cmd=$command exit=$exitCode out=${output.take(300)} err=${error.take(200)}")
            output.ifEmpty { error }
        } catch (e: Exception) {
            Log.e(TAG, "execShell failed: $command", e)
            ""
        }
    }

    fun getAudioFlingerDump(): String = execShell("dumpsys media.audio_flinger")

    fun getAudioPolicyDump(): String = execShell("dumpsys media.audio_policy")

    fun getAudioDump(): String = execShell("dumpsys audio")

    fun listAudioSessions(): String = execShell("dumpsys media.audio_flinger | grep -A 2 'Output thread'")

    fun listEffects(): String = execShell("dumpsys audio | grep -i -A 1 'effect\\|equalizer\\|bass\\|reverb\\|virtualizer'")

    fun setParameter(key: String, value: String): String = execShell("cmd audio set-parameter $key $value")

    fun setGlobalEq(bands: IntArray) {
        for (i in bands.indices) {
            val freq = Constants.EQ_BAND_FREQUENCIES[i]
            val db = ((bands[i] - 50) * 0.4).toInt()
            setParameter("music_eq_band_${freq}hz", db.toString())
        }
    }

    fun setBassBoost(strength: Int) {
        setParameter("music_bass_boost", strength.toString())
    }

    fun setVirtualizer(strength: Int) {
        setParameter("music_virtualizer", strength.toString())
    }

    fun setReverbPreset(preset: Int) {
        setParameter("music_reverb_preset", preset.toString())
    }

    fun getSupportedEffects(): String = execShell("cmd audio get-supported-effects")

    fun getServiceCall(): String = execShell("service list | grep audio")

    fun isShizukuAvailable(): Boolean = try { Shizuku.pingBinder() } catch (e: Exception) { false }

    fun hasPermission(): Boolean = try { Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED } catch (e: Exception) { false }
}
