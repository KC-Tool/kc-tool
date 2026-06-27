package github.boxiaolanya2008.kc_tool

import github.boxiaolanya2008.kc_tool.service.LoopOperationMode
import github.boxiaolanya2008.kc_tool.service.buildLoopCommand
import org.junit.Assert.assertEquals
import org.junit.Test

class LoopOperationModeTest {
    @Test
    fun crash_mode_uses_am_crash_command() {
        assertEquals("am crash com.example.app", buildLoopCommand("com.example.app", LoopOperationMode.CRASH))
    }

    @Test
    fun clear_data_mode_uses_pm_clear_command() {
        assertEquals("pm clear com.example.app", buildLoopCommand("com.example.app", LoopOperationMode.CLEAR_DATA))
    }
}
