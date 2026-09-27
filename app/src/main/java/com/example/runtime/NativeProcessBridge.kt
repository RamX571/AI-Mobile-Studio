package com.example.runtime

import android.os.Build
import android.util.Log

/**
 * JNI & Native process execution bridge for rootless terminal execution.
 * Provides fallback to ProcessBuilder with environment injection when native .so
 * is built into standard or standalone packages.
 */
object NativeProcessBridge {
    private const val TAG = "NativeProcessBridge"
    private var isNativeLoaded = false

    init {
        try {
            System.loadLibrary("studio_process_bridge")
            isNativeLoaded = true
        } catch (e: UnsatisfiedLinkError) {
            Log.d(TAG, "Native library studio_process_bridge not yet bundled; using fallback JVM execution pipe.")
            isNativeLoaded = false
        }
    }

    fun isNativeBridgeActive(): Boolean = isNativeLoaded

    fun getSystemArchitecture(): String {
        val abis = Build.SUPPORTED_ABIS
        return if (abis.isNotEmpty()) abis[0] else "unknown"
    }

    fun isArm64Compatible(): Boolean {
        val arch = getSystemArchitecture().lowercase()
        return arch.contains("arm64") || arch.contains("aarch64")
    }

    /**
     * Executes process with piped standard I/O and custom environment.
     */
    fun createProcess(
        command: List<String>,
        workingDir: String,
        envVars: Map<String, String>
    ): Process {
        val builder = ProcessBuilder(command)
        builder.directory(java.io.File(workingDir))
        val env = builder.environment()
        env.putAll(envVars)
        builder.redirectErrorStream(true)
        return builder.start()
    }
}
