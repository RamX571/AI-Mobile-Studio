package com.example.runtime

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

data class ProcessExecutionResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
    val durationMs: Long
)

data class RuntimeHealthInfo(
    val cpuArch: String,
    val isArm64: Boolean,
    val totalRamMb: Long,
    val freeRamMb: Long,
    val availableStorageMb: Long,
    val totalStorageMb: Long,
    val linuxStatus: String,
    val rootfsPath: String,
    val rootfsSizeBytes: Long,
    val isForegroundServiceRunning: Boolean,
    val activeProcessCount: Int,
    val activePortCount: Int
)

class LinuxRuntimeManager(private val context: Context) {

    val rootfsDir: File by lazy {
        File(context.filesDir, "linux-rootfs").apply { mkdirs() }
    }

    private val runningProcesses = ConcurrentHashMap<String, Process>()

    init {
        ensureBasicEnvironment()
    }

    private fun ensureBasicEnvironment() {
        File(rootfsDir, "bin").mkdirs()
        File(rootfsDir, "usr/bin").mkdirs()
        File(rootfsDir, "usr/local/bin").mkdirs()
        File(rootfsDir, "etc").mkdirs()
        File(rootfsDir, "tmp").mkdirs()

        // Create standard shell script wrapper
        val wrapper = File(rootfsDir, "bin/studio-env")
        if (!wrapper.exists()) {
            wrapper.writeText(
                """
                #!/system/bin/sh
                export HOME="${context.filesDir.absolutePath}"
                export TMPDIR="${File(rootfsDir, "tmp").absolutePath}"
                export TERM="xterm-256color"
                export LANG="en_US.UTF-8"
                export PATH="${File(rootfsDir, "usr/local/bin").absolutePath}:${File(rootfsDir, "usr/bin").absolutePath}:${File(rootfsDir, "bin").absolutePath}:/system/bin:/system/xbin:${'$'}PATH"
                exec "${'$'}@"
                """.trimIndent()
            )
            wrapper.setExecutable(true)
        }
    }

    fun getEnvironmentVariables(workspacePath: String): Map<String, String> {
        val path = "${File(rootfsDir, "usr/local/bin").absolutePath}:${File(rootfsDir, "usr/bin").absolutePath}:${File(rootfsDir, "bin").absolutePath}:/system/bin:/system/xbin"
        return mapOf(
            "HOME" to context.filesDir.absolutePath,
            "WORKSPACE" to workspacePath,
            "TERM" to "xterm-256color",
            "LANG" to "en_US.UTF-8",
            "TMPDIR" to File(rootfsDir, "tmp").absolutePath,
            "PATH" to path,
            "PR_ARCH" to NativeProcessBridge.getSystemArchitecture(),
            "AI_STUDIO_RUNTIME" to "PROOT_ROOTLESS_1.0"
        )
    }

    /**
     * Executes a command inside the given working directory and streams output line-by-line.
     */
    fun executeCommandStream(
        taskId: String,
        command: String,
        workingDir: File,
        extraEnv: Map<String, String> = emptyMap()
    ): Flow<String> = flow {
        val startTime = System.currentTimeMillis()
        emit("🚀 [Runtime] Executing: $command in ${workingDir.name}\n")

        val env = getEnvironmentVariables(workingDir.absolutePath).toMutableMap()
        env.putAll(extraEnv)

        // Try direct shell invocation
        val shellCmd = listOf("/system/bin/sh", "-c", command)

        var process: Process? = null
        try {
            process = ProcessBuilder(shellCmd)
                .directory(workingDir)
                .redirectErrorStream(true)
                .apply {
                    val pEnv = environment()
                    pEnv.putAll(env)
                }
                .start()

            runningProcesses[taskId] = process

            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String? = reader.readLine()
            while (line != null) {
                emit(line + "\n")
                line = reader.readLine()
            }

            val exitCode = process.waitFor()
            val duration = System.currentTimeMillis() - startTime
            if (exitCode == 0) {
                emit("\n✨ [Runtime] Process completed successfully in ${duration}ms (exit code 0)\n")
            } else {
                emit("\n⚠️ [Runtime] Process exited with error code $exitCode in ${duration}ms\n")
            }
        } catch (e: Exception) {
            emit("❌ [Runtime Exception] ${e.localizedMessage ?: "Unknown execution error"}\n")
        } finally {
            runningProcesses.remove(taskId)
            process?.destroy()
        }
    }.flowOn(Dispatchers.IO)

    suspend fun executeCommandSync(
        command: String,
        workingDir: File,
        extraEnv: Map<String, String> = emptyMap()
    ): ProcessExecutionResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val env = getEnvironmentVariables(workingDir.absolutePath).toMutableMap().apply {
            putAll(extraEnv)
        }

        try {
            val process = ProcessBuilder(listOf("/system/bin/sh", "-c", command))
                .directory(workingDir)
                .apply { environment().putAll(env) }
                .start()

            val stdoutBuilder = StringBuilder()
            val stderrBuilder = StringBuilder()

            val stdoutReader = BufferedReader(InputStreamReader(process.inputStream))
            val stderrReader = BufferedReader(InputStreamReader(process.errorStream))

            stdoutReader.forEachLine { stdoutBuilder.appendLine(it) }
            stderrReader.forEachLine { stderrBuilder.appendLine(it) }

            val exitCode = process.waitFor()
            ProcessExecutionResult(
                exitCode = exitCode,
                stdout = stdoutBuilder.toString().trim(),
                stderr = stderrBuilder.toString().trim(),
                durationMs = System.currentTimeMillis() - startTime
            )
        } catch (e: Exception) {
            ProcessExecutionResult(
                exitCode = -1,
                stdout = "",
                stderr = e.localizedMessage ?: "Execution failed",
                durationMs = System.currentTimeMillis() - startTime
            )
        }
    }

    fun killProcess(taskId: String): Boolean {
        val process = runningProcesses.remove(taskId) ?: return false
        process.destroyForcibly()
        return true
    }

    fun getRunningProcessCount(): Int = runningProcesses.size

    /**
     * Checks if a local TCP port is open and actively accepting connections.
     */
    suspend fun checkPortActive(port: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress("127.0.0.1", port), 250)
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Scans standard dev ports: 3000, 4173, 5173, 8000, 8080.
     */
    suspend fun scanDevPorts(): List<Int> = withContext(Dispatchers.IO) {
        val targetPorts = listOf(3000, 4173, 5173, 8000, 8080)
        targetPorts.filter { checkPortActive(it) }
    }

    fun verifyPackageSha256(file: File, expectedHash: String): Boolean {
        if (!file.exists()) return false
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        val calculated = digest.digest().joinToString("") { "%02x".format(it) }
        return calculated.equals(expectedHash, ignoreCase = true)
    }

    fun getRuntimeHealth(): RuntimeHealthInfo {
        val runtime = Runtime.getRuntime()
        val totalRam = runtime.totalMemory() / (1024 * 1024)
        val freeRam = runtime.freeMemory() / (1024 * 1024)

        val stat = StatFs(context.filesDir.path)
        val availableStorage = (stat.availableBlocksLong * stat.blockSizeLong) / (1024 * 1024)
        val totalStorage = (stat.blockCountLong * stat.blockSizeLong) / (1024 * 1024)

        val rootfsSize = rootfsDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()

        return RuntimeHealthInfo(
            cpuArch = NativeProcessBridge.getSystemArchitecture(),
            isArm64 = NativeProcessBridge.isArm64Compatible(),
            totalRamMb = totalRam,
            freeRamMb = freeRam,
            availableStorageMb = availableStorage,
            totalStorageMb = totalStorage,
            linuxStatus = "Active (PRoot userland)",
            rootfsPath = rootfsDir.absolutePath,
            rootfsSizeBytes = rootfsSize,
            isForegroundServiceRunning = RuntimeForegroundService.isRunning,
            activeProcessCount = getRunningProcessCount(),
            activePortCount = 0
        )
    }
}
