package github.boxiaolanya2008.kc_tool.audio

object Constants {
    const val ACTION_START = "com.kc_tool.ACTION_START"
    const val ACTION_STOP = "com.kc_tool.ACTION_STOP"
    const val ACTION_SET_PRESET = "com.kc_tool.ACTION_SET_PRESET"
    const val ACTION_SET_EQ = "com.kc_tool.ACTION_SET_EQ"

    const val EXTRA_RESULT_CODE = "result_code"
    const val EXTRA_DATA = "data"
    const val EXTRA_PRESET_ID = "preset_id"
    const val EXTRA_EQ_BANDS = "eq_bands"

    const val SAMPLE_RATE = 48000
    const val CHANNEL_COUNT = 2
    const val BIT_DEPTH = 16
    const val BYTES_PER_SAMPLE = BIT_DEPTH / 8

    const val NOTIFICATION_CHANNEL_ID = "global_audio_effect"
    const val NOTIFICATION_ID = 2001

    val EQ_BAND_LABELS = arrayOf("31", "62", "125", "250", "500", "1K", "2K", "4K", "8K", "16K")
    val EQ_BAND_FREQUENCIES = intArrayOf(31, 62, 125, 250, 500, 1000, 2000, 4000, 8000, 16000)
}
