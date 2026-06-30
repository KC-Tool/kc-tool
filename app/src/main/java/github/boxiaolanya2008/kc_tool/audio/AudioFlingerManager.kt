package github.boxiaolanya2008.kc_tool.audio

import android.util.Log
import rikka.shizuku.Shizuku
import java.io.BufferedReader

object AudioFlingerManager {
    private const val TAG = "AudioFlingerManager"

    fun execShell(command: String): String {
        return try {
            val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
            val output = process.inputStream.bufferedReader().readText()
            val error = process.errorStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            Log.d(TAG, "cmd=$command exit=$exitCode")
            output.ifEmpty { error }
        } catch (e: Exception) {
            Log.e(TAG, "execShell failed: $command", e)
            ""
        }
    }

    fun getAudioFlingerDump(): String = execShell("dumpsys media.audio_flinger")

    fun getAudioPolicyDump(): String = execShell("dumpsys media.audio_policy")

    fun getAudioDump(): String = execShell("dumpsys audio")

    fun getServiceCall(): String = execShell("service list | grep audio")

    fun listEffects(): String = execShell("dumpsys media.audio_flinger | grep -i -A 2 'Effect\\|effect\\|Equalizer\\|BassBoost\\|Virtualizer\\|Reverb\\|DynamicsProcessing'")

    fun listOutputThreads(): String = execShell("dumpsys media.audio_flinger | grep 'Output thread'")

    fun listSessions(): String = execShell("dumpsys media.audio_flinger | grep 'session' | head -30")

    fun getGlobalSessionEffects(): String {
        val dump = getAudioFlingerDump()
        val sb = StringBuilder()
        val lines = dump.lines()
        var inPrimaryThread = false
        var inEffectChain = false
        var depth = 0

        for (line in lines) {
            if (line.contains("Output thread") && line.contains("AudioOut_D")) {
                inPrimaryThread = true
                sb.appendLine(line.trim())
                continue
            }
            if (inPrimaryThread && line.trimStart().startsWith("Output thread") && !line.contains("AudioOut_D")) {
                break
            }
            if (inPrimaryThread && line.contains("Effect Chains")) {
                inEffectChain = true
                sb.appendLine(line.trim())
                continue
            }
            if (inEffectChain) {
                sb.appendLine(line.trim())
                if (line.trim().startsWith("Effect ID")) {
                    depth++
                }
            }
        }
        return sb.toString().ifEmpty { "未找到 session 0 上的效果链" }
    }

    fun injectEqualizer(frequency: Int, gain: Int): Boolean {
        val dump = getAudioFlingerDump()
        val threadLine = dump.lines().find { it.contains("AudioOut_D") && it.contains("Output thread") }
        if (threadLine == null) {
            Log.e(TAG, "Primary output thread not found")
            return false
        }

        val cmd = "cmd audio inject-effect equalizer $frequency $gain"
        val result = execShell(cmd)
        Log.d(TAG, "injectEqualizer result: $result")
        return result.isNotEmpty() && !result.contains("error", true)
    }

    fun setAudioParameter(key: String, value: String): String {
        return execShell("cmd audio set-parameter $key $value")
    }

    fun getAudioParameter(key: String): String {
        return execShell("cmd audio get-parameter $key")
    }

    fun setGlobalEq(bands: IntArray) {
        for (i in bands.indices) {
            val freq = Constants.EQ_BAND_FREQUENCIES[i]
            val db = ((bands[i] - 50) * 0.4).toInt()
            setAudioParameter("music_eq_band_${freq}hz", db.toString())
        }
    }

    fun setBassBoost(strength: Int) {
        setAudioParameter("music_bass_boost", strength.toString())
    }

    fun setVirtualizer(strength: Int) {
        setAudioParameter("music_virtualizer", strength.toString())
    }

    fun setReverbPreset(preset: Int) {
        setAudioParameter("music_reverb_preset", preset.toString())
    }

    fun getSupportedEffects(): String = execShell("cmd audio get-supported-effects")

    fun probeCmdAudio(): String {
        val sb = StringBuilder()
        sb.appendLine("=== cmd audio help ===")
        sb.appendLine(execShell("cmd audio help 2>&1"))
        sb.appendLine()
        sb.appendLine("=== cmd audio list-effects ===")
        sb.appendLine(execShell("cmd audio list-effects 2>&1"))
        sb.appendLine()
        sb.appendLine("=== cmd audio get-effects ===")
        sb.appendLine(execShell("cmd audio get-effects 2>&1"))
        sb.appendLine()
        sb.appendLine("=== cmd audio parameters ===")
        sb.appendLine(execShell("cmd audio parameters 2>&1"))
        sb.appendLine()
        sb.appendLine("=== getprop audio ===")
        sb.appendLine(execShell("getprop | grep audio 2>&1"))
        return sb.toString()
    }

    fun getAllOutputDeviceInfo(): String {
        val dump = getAudioFlingerDump()
        val sb = StringBuilder()
        val lines = dump.lines()
        for (i in lines.indices) {
            if (lines[i].contains("Output thread")) {
                val start = i
                val end = minOf(i + 15, lines.size)
                for (j in start until end) {
                    sb.appendLine(lines[j].trim())
                }
                sb.appendLine("---")
            }
        }
        return sb.toString().ifEmpty { "无输出线程信息" }
    }

    fun getSession0Info(): String {
        val dump = getAudioFlingerDump()
        val sb = StringBuilder()
        val lines = dump.lines()
        var found = false
        for (line in lines) {
            if (line.contains("session 0") || line.contains("Session: 0")) {
                found = true
                sb.appendLine(line.trim())
            } else if (found && (line.trim().startsWith("Effect") || line.trim().startsWith("Type") || line.trim().startsWith("UUID") || line.trim().startsWith("name"))) {
                sb.appendLine(line.trim())
            } else if (found && line.trim().isEmpty()) {
                break
            }
        }
        return sb.toString().ifEmpty { "未找到 session 0 信息" }
    }

    fun isShizukuAvailable(): Boolean = try { Shizuku.pingBinder() } catch (e: Exception) { false }

    fun hasPermission(): Boolean = try { Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED } catch (e: Exception) { false }

    fun getFullReport(): String {
        val sb = StringBuilder()
        sb.appendLine("=== Shizuku 状态 ===")
        sb.appendLine("连接: ${isShizukuAvailable()}")
        sb.appendLine("权限: ${hasPermission()}")
        sb.appendLine()
        sb.appendLine("=== 输出线程 ===")
        sb.appendLine(listOutputThreads())
        sb.appendLine()
        sb.appendLine("=== Session 0 效果 ===")
        sb.appendLine(getGlobalSessionEffects())
        sb.appendLine()
        sb.appendLine("=== 支持的音效 ===")
        sb.appendLine(getSupportedEffects())
        sb.appendLine()
        sb.appendLine("=== Audio 服务列表 ===")
        sb.appendLine(getServiceCall())
        return sb.toString()
    }
}
