package github.boxiaolanya2008.kc_tool.service

import android.graphics.drawable.Drawable

data class ProcessInfo(
    val packageName: String,
    val processName: String,
    val pid: Int,
    val uid: Int,
    val isSystemApp: Boolean,
    val icon: Drawable? = null
)