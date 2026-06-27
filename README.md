> 一个Android系统优化软件

### 本项目没有注释，看什么看，注释都是给弱者的，强者都不需要注释。

### 以下是唯一有注释的片段

```diff
+ private suspend fun execCommand(command: String): String {
+        Log.d(TAG, "execCommand: $command")
+        try {
+            val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
+            Log.d(TAG, "process created, pid=${process.hashCode()}")
+
+            // 并发读取 stdout 和 stderr，防止缓冲区满导致死锁
+            val (output, error) = coroutineScope {
+                val outputDeferred = async(Dispatchers.IO) {
+                    process.inputStream.bufferedReader().readText()
+                }
+                val errorDeferred = async(Dispatchers.IO) {
+                    process.errorStream.bufferedReader().readText()
+                }
+                val exitCode = process.waitFor()
+                Log.d(TAG, "exitCode=$exitCode")
+                Pair(outputDeferred.await(), errorDeferred.await())
+            }
+
+            Log.d(TAG, "output=${output.take(200)}, error=${error.take(200)}")
+            if (error.isNotEmpty()) Log.w(TAG, "stderr: ${error.take(200)}")
+            return output.ifEmpty { error }
+        } catch (e: Exception) {
+            Log.e(TAG, "execCommand failed: $command", e)
+            throw e
+        }
+    }