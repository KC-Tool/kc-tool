package github.boxiaolanya2008.kc_tool.audio

data class AudioEffectPreset(
    val id: Int,
    val name: String,
    val description: String,
    val eqBands: IntArray = IntArray(10) { 50 },
    val bassBoost: Short = 0,
    val virtualizer: Short = 0,
    val reverbPreset: Short = 0,
    val envReverb: EnvReverbParams? = null,
    val isNetwork: Boolean = false,
    val userCount: String = ""
) {
    data class EnvReverbParams(
        val roomLevel: Short,
        val reflectionsLevel: Short,
        val reverbLevel: Short,
        val roomHfRatio: Short,
        val decayTime: Short,
        val density: Short,
        val diffusion: Short
    )

    companion object {
        val PRESETS = listOf(
            AudioEffectPreset(0, "关闭", "直通无效果"),
            AudioEffectPreset(1, "全景环绕", "空间环绕声", virtualizer = 1000, eqBands = intArrayOf(50, 50, 50, 50, 50, 50, 50, 50, 50, 50)),
            AudioEffectPreset(2, "超重低音", "低频增强", bassBoost = 800, eqBands = intArrayOf(85, 80, 60, 50, 35, 30, 55, 55, 70, 75)),
            AudioEffectPreset(3, "清澈人声", "中频人声突出", eqBands = intArrayOf(30, 35, 45, 65, 80, 80, 65, 45, 35, 30)),
            AudioEffectPreset(4, "KTV", "卡拉OK混响", reverbPreset = 4, eqBands = intArrayOf(60, 55, 50, 50, 55, 55, 50, 50, 55, 55), envReverb = EnvReverbParams(4523, 1242, 4551, 5447, 5447, 6702, 1536)),
            AudioEffectPreset(5, "演唱会", "大厅混响效果", reverbPreset = 2, eqBands = intArrayOf(55, 50, 50, 50, 55, 55, 55, 55, 50, 50), envReverb = EnvReverbParams(8192, 1352, 5120, 6564, 6564, 8192, 468)),
            AudioEffectPreset(6, "浴室", "短混响反射", reverbPreset = 3, eqBands = intArrayOf(45, 50, 55, 55, 50, 50, 55, 55, 50, 45), envReverb = EnvReverbParams(8192, 2027, 3758, 971, 971, 2829, 1858)),
            AudioEffectPreset(7, "音乐厅", "大型音乐厅", reverbPreset = 5, eqBands = intArrayOf(50, 50, 55, 55, 60, 60, 55, 55, 50, 50), envReverb = EnvReverbParams(5254, 1435, 4702, 6785, 6785, 6771, 1024)),
            AudioEffectPreset(8, "健身房", "超大空间混响", reverbPreset = 5, eqBands = intArrayOf(50, 55, 55, 55, 60, 60, 55, 55, 55, 50), envReverb = EnvReverbParams(7685, 1731, 4349, 2365, 2365, 6756, 1731)),
            AudioEffectPreset(9, "图书馆", "微混响安静环境", reverbPreset = 1, eqBands = intArrayOf(45, 50, 50, 50, 55, 55, 50, 50, 50, 45), envReverb = EnvReverbParams(7052, 1140, 5152, 3294, 3294, 8192, 704)),
            AudioEffectPreset(10, "HiFi 直通", "关闭所有处理，原始输出", eqBands = IntArray(10) { 50 }, bassBoost = 0, virtualizer = 0, reverbPreset = 0),
            AudioEffectPreset(20, "自定义", "用户自定义EQ", eqBands = intArrayOf(50, 50, 50, 50, 50, 50, 50, 50, 50, 50)),
            AudioEffectPreset(21, "流行", "流行音乐预设", eqBands = intArrayOf(80, 75, 30, 35, 75, 70, 30, 35, 80, 70)),
            AudioEffectPreset(22, "摇滚", "摇滚音乐预设", eqBands = intArrayOf(85, 80, 60, 50, 35, 30, 55, 55, 70, 75)),
            AudioEffectPreset(23, "舞曲", "电子舞曲预设", eqBands = intArrayOf(70, 65, 30, 20, 50, 50, 65, 70, 70, 75)),
            AudioEffectPreset(24, "蓝调", "蓝调音乐预设", eqBands = intArrayOf(40, 50, 60, 65, 65, 55, 40, 35, 40, 40)),
            AudioEffectPreset(25, "古典", "古典音乐预设", eqBands = intArrayOf(80, 85, 55, 60, 50, 55, 40, 35, 20, 15)),
            AudioEffectPreset(26, "电子乐", "电子音乐预设", eqBands = intArrayOf(90, 80, 60, 50, 30, 35, 50, 50, 65, 75)),
            AudioEffectPreset(27, "爵士", "爵士乐预设", eqBands = intArrayOf(75, 30, 35, 40, 65, 60, 60, 50, 60, 60)),
            AudioEffectPreset(28, "慢歌", "抒情慢歌预设", eqBands = intArrayOf(25, 30, 30, 30, 65, 60, 70, 70, 50, 50)),
            AudioEffectPreset(29, "乡村", "乡村音乐预设", eqBands = intArrayOf(30, 25, 30, 35, 65, 70, 75, 70, 35, 35)),
            AudioEffectPreset(100, "5.1全景声", "5.1声道环绕", virtualizer = 1000, isNetwork = true, userCount = "67.7万"),
            AudioEffectPreset(101, "HiFi现场", "Hi-Fi现场效果", eqBands = intArrayOf(60, 55, 50, 50, 55, 60, 60, 55, 50, 50), reverbPreset = 2, isNetwork = true, userCount = "124.7万"),
            AudioEffectPreset(102, "黑胶唱片", "复古黑胶质感", eqBands = intArrayOf(30, 40, 50, 60, 65, 60, 50, 45, 40, 35), isNetwork = true, userCount = "96.0万"),
            AudioEffectPreset(103, "超级低音", "极致低频", bassBoost = 1000, eqBands = intArrayOf(90, 85, 70, 50, 30, 30, 40, 45, 50, 55), isNetwork = true, userCount = "88.2万"),
            AudioEffectPreset(104, "清澈音质", "全频段清澈", eqBands = intArrayOf(40, 45, 55, 60, 65, 65, 60, 55, 45, 40), isNetwork = true, userCount = "285.8万"),
            AudioEffectPreset(105, "3D旋转", "3D旋转空间音效", virtualizer = 900, eqBands = intArrayOf(50, 55, 60, 55, 50, 50, 55, 60, 55, 50), isNetwork = true, userCount = "35.1万"),
            AudioEffectPreset(300, "录音棚", "专业录音棚效果", eqBands = intArrayOf(50, 50, 50, 50, 50, 50, 50, 50, 50, 50), isNetwork = true, userCount = "13.2万"),
            AudioEffectPreset(400, "民谣", "民谣吉他质感", eqBands = intArrayOf(35, 40, 50, 60, 70, 65, 55, 45, 40, 35), isNetwork = true, userCount = "1.5万"),
            AudioEffectPreset(401, "古风", "中国传统乐器优化", eqBands = intArrayOf(30, 35, 45, 55, 70, 75, 65, 55, 45, 40), isNetwork = true, userCount = "13.3万")
        )

        fun getPresetById(id: Int) = PRESETS.find { it.id == id } ?: PRESETS[0]
        fun getBuiltInPresets() = PRESETS.filter { it.id in 0..9 }
        fun getEqPresets() = PRESETS.filter { it.id in 20..29 }
        fun getNetworkPresets() = PRESETS.filter { it.isNetwork }
    }

    fun toEqMillibels(): ShortArray = ShortArray(10) { i -> ((eqBands[i] - 50) * 20).toShort() }
}
