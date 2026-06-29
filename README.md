> 一个Android系统优化软件

### 本项目没有注释，看什么看，注释都是给弱者的，强者都不需要注释。

### 以下是唯一有注释的片段
```kotlin
    private fun execCommand(command: String): String {
        val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
        // 并发读 stdout / stderr,防 pipe 缓冲区满卡死
        val (output, error, exitCode) = runBlocking {
            withTimeout(15_000) {
                coroutineScope {
                    val outDef = async(Dispatchers.IO) {
                        runCatching { process.inputStream.bufferedReader().readText() }.getOrDefault("")
                    }
                    val errDef = async(Dispatchers.IO) {
                        runCatching { process.errorStream.bufferedReader().readText() }.getOrDefault("")
                    }
                    val code = process.waitFor()
                    Triple(outDef.await(), errDef.await(), code)
                }
            }
        }
```

> 何必膜拜传奇，当你就是其中之一。