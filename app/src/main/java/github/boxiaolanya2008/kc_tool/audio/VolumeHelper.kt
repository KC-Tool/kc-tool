package github.boxiaolanya2008.kc_tool.audio

import android.content.Context
import android.media.AudioManager
import android.util.Log

class VolumeHelper(context: Context) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var originalMediaVolume = -1
    private var isDucked = false

    fun duckOriginalAudio() {
        if (isDucked) return
        originalMediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (maxVolume * 0.15f).toInt().coerceAtLeast(1), 0)
        isDucked = true
    }

    fun restoreOriginalAudio() {
        if (!isDucked || originalMediaVolume < 0) return
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalMediaVolume, 0)
        isDucked = false
    }
}
