package github.boxiaolanya2008.kc_tool.audio

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import github.boxiaolanya2008.kc_tool.MainActivity
import kotlinx.coroutines.*

class AudioCaptureService : Service() {
    companion object {
        const val ACTION_START = "com.kc_tool.ACTION_START"
        const val ACTION_STOP = "com.kc_tool.ACTION_STOP"
        const val ACTION_SET_PRESET = "com.kc_tool.ACTION_SET_PRESET"
        const val ACTION_SET_EQ = "com.kc_tool.ACTION_SET_EQ"
        const val EXTRA_RESULT_CODE = "result_code"
        const val EXTRA_DATA = "data"
        const val EXTRA_PRESET_ID = "preset_id"
        const val EXTRA_EQ_BANDS = "eq_bands"
        private const val CHANNEL_ID = "global_audio_effect"
        private const val NOTIFICATION_ID = 2001

        var isRunning = false; private set
        var currentPresetId = 0; private set
        private var instance: AudioCaptureService? = null
        fun getInstance() = instance
    }

    private var mediaProjection: MediaProjection? = null
    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var dspProcessor: DspProcessor? = null
    private var volumeHelper: VolumeHelper? = null
    private var captureJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() { fun getService() = this@AudioCaptureService }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        instance = this
        volumeHelper = VolumeHelper(this)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val rc = intent.getIntExtra(EXTRA_RESULT_CODE, -1)
                @Suppress("DEPRECATION") val data = intent.getParcelableExtra<Intent>(EXTRA_DATA)
                if (rc != -1 && data != null) { startForeground(NOTIFICATION_ID, buildNotification("音效处理中...")); startCapture(rc, data) }
            }
            ACTION_STOP -> { stopCapture(); stopForeground(STOP_FOREGROUND_REMOVE); stopSelf() }
            ACTION_SET_PRESET -> setPreset(intent.getIntExtra(EXTRA_PRESET_ID, 0))
            ACTION_SET_EQ -> intent.getIntArrayExtra(EXTRA_EQ_BANDS)?.let { setCustomEq(it) }
        }
        return START_NOT_STICKY
    }

    private fun startCapture(resultCode: Int, data: Intent) {
        val pm = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = pm.getMediaProjection(resultCode, data)
        mediaProjection?.registerCallback(object : MediaProjection.Callback() {
            override fun onStop() { serviceScope.launch { stopCapture() } }
        }, null)

        val bufferSize = AudioRecord.getMinBufferSize(48000, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT) * 4
        val captureConfig = AudioPlaybackCaptureConfiguration.Builder(mediaProjection!!)
            .addMatchingUsage(AudioAttributes.USAGE_MEDIA).addMatchingUsage(AudioAttributes.USAGE_GAME)
            .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN).build()
        val recordFormat = AudioFormat.Builder().setSampleRate(48000).setChannelMask(AudioFormat.CHANNEL_IN_STEREO).setEncoding(AudioFormat.ENCODING_PCM_16BIT).build()

        try { audioRecord = AudioRecord.Builder().setAudioPlaybackCaptureConfig(captureConfig).setAudioFormat(recordFormat).setBufferSizeInBytes(bufferSize).build() }
        catch (e: SecurityException) { stopSelf(); return }

        val trackFormat = AudioFormat.Builder().setSampleRate(48000).setChannelMask(AudioFormat.CHANNEL_OUT_STEREO).setEncoding(AudioFormat.ENCODING_PCM_16BIT).build()
        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build())
            .setAudioFormat(trackFormat).setBufferSizeInBytes(bufferSize).setTransferMode(AudioTrack.MODE_STREAM).build()

        dspProcessor = DspProcessor(audioTrack!!.audioSessionId).apply { initialize() }
        currentPresetId = 0

        volumeHelper?.duckOriginalAudio()

        audioRecord!!.startRecording()
        audioTrack!!.play()

        // 关键：将 AudioTrack 音量设为最大，避免被 duck 影响
        audioTrack?.setVolume(1.0f)

        isRunning = true
        captureJob = serviceScope.launch { captureAndProcess() }
    }

    private suspend fun captureAndProcess() {
        val buffer = ShortArray(2048)
        while (captureJob?.isActive == true) {
            val readCount = audioRecord?.read(buffer, 0, buffer.size) ?: -1
            if (readCount > 0) audioTrack?.write(buffer, 0, readCount)
        }
    }

    fun setPreset(presetId: Int) {
        currentPresetId = presetId
        dspProcessor?.applyPreset(AudioEffectPreset.getPresetById(presetId))
        updateNotification("当前: ${AudioEffectPreset.getPresetById(presetId).name}")
    }

    fun setCustomEq(bands: IntArray) {
        currentPresetId = -1
        dspProcessor?.setCustomEq(bands)
        updateNotification("自定义EQ")
    }

    fun getCurrentPreset() = if (currentPresetId >= 0) AudioEffectPreset.getPresetById(currentPresetId)
        else AudioEffectPreset(-1, "自定义", "", dspProcessor?.getEqBands() ?: IntArray(10) { 50 })

    private fun stopCapture() {
        isRunning = false; captureJob?.cancel(); captureJob = null
        volumeHelper?.restoreOriginalAudio()
        try { audioRecord?.stop(); audioRecord?.release() } catch (_: Exception) {}
        try { audioTrack?.stop(); audioTrack?.release() } catch (_: Exception) {}
        dspProcessor?.release(); dspProcessor = null
        mediaProjection?.stop(); mediaProjection = null
        audioRecord = null; audioTrack = null
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, "全局音效", NotificationManager.IMPORTANCE_LOW).apply { description = "全局音效处理服务" }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(text: String): Notification {
        val pi = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val stopPi = PendingIntent.getService(this, 1, Intent(this, AudioCaptureService::class.java).apply { action = ACTION_STOP }, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("全局音效").setContentText(text).setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pi).addAction(Notification.Action.Builder(null, "停止", stopPi).build()).setOngoing(true).build()
    }

    private fun updateNotification(text: String) { getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, buildNotification(text)) }

    override fun onDestroy() { super.onDestroy(); stopCapture(); serviceScope.cancel(); instance = null }
}
