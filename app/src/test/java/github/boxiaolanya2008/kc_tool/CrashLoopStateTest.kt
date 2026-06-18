package github.boxiaolanya2008.kc_tool

import github.boxiaolanya2008.kc_tool.service.CrashLoopState
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CrashLoopStateTest {

    @Before
    fun setup() {
        CrashLoopState.resetAll()
    }

    @Test
    fun initialState_isNotRunning() {
        assertFalse(CrashLoopState.isRunning.value)
    }

    @Test
    fun setRunning_updatesState() {
        CrashLoopState.setRunning(true)
        assertTrue(CrashLoopState.isRunning.value)

        CrashLoopState.setRunning(false)
        assertFalse(CrashLoopState.isRunning.value)
    }

    @Test
    fun setTargets_updatesPackages() {
        val packages = listOf("com.example.app1", "com.example.app2")
        CrashLoopState.setTargets(packages)
        assertEquals(packages, CrashLoopState.targetPackages.value)
    }

    @Test
    fun incrementCrash_incrementsCount() {
        assertEquals(0, CrashLoopState.crashCount.value)
        assertEquals(0, CrashLoopState.totalCrashCount.value)

        CrashLoopState.incrementCrash("com.example.app")
        assertEquals(1, CrashLoopState.crashCount.value)
        assertEquals(1, CrashLoopState.totalCrashCount.value)

        CrashLoopState.incrementCrash("com.example.app")
        assertEquals(2, CrashLoopState.crashCount.value)
        assertEquals(2, CrashLoopState.totalCrashCount.value)
    }

    @Test
    fun failCrash_incrementsCount() {
        CrashLoopState.failCrash("com.example.app")
        assertEquals(1, CrashLoopState.crashCount.value)
        assertEquals(1, CrashLoopState.totalCrashCount.value)
    }

    @Test
    fun resetCount_resetsCurrentCount() {
        CrashLoopState.incrementCrash("com.example.app")
        CrashLoopState.incrementCrash("com.example.app")
        assertEquals(2, CrashLoopState.crashCount.value)

        CrashLoopState.resetCount()
        assertEquals(0, CrashLoopState.crashCount.value)
    }

    @Test
    fun logs_areAddedInOrder() {
        CrashLoopState.incrementCrash("com.example.app1")
        CrashLoopState.failCrash("com.example.app2")
        CrashLoopState.incrementCrash("com.example.app3")

        val logs = CrashLoopState.logs.value
        assertEquals(3, logs.size)
        assertEquals("com.example.app3", logs[0].packageName)
        assertEquals("com.example.app2", logs[1].packageName)
        assertEquals("com.example.app1", logs[2].packageName)
    }

    @Test
    fun logs_successAndFailureMarked() {
        CrashLoopState.incrementCrash("com.example.app")
        CrashLoopState.failCrash("com.example.app")

        val logs = CrashLoopState.logs.value
        assertFalse(logs[0].success)
        assertTrue(logs[1].success)
    }
}