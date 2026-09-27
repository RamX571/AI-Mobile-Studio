package com.example.git

import com.example.runtime.LinuxRuntimeManager
import com.example.runtime.ProcessExecutionResult
import java.io.File

data class DiffLine(
    val type: DiffType,
    val text: String,
    val oldLineNum: Int? = null,
    val newLineNum: Int? = null
)

enum class DiffType {
    SAME,
    ADDED,
    REMOVED,
    HEADER
}

class GitRepositoryManager(
    private val runtimeManager: LinuxRuntimeManager
) {
    suspend fun init(workingDir: File): ProcessExecutionResult {
        return runtimeManager.executeCommandSync("git init", workingDir)
    }

    suspend fun status(workingDir: File): String {
        val res = runtimeManager.executeCommandSync("git status --short", workingDir)
        return if (res.exitCode == 0) {
            if (res.stdout.isBlank()) "Working directory clean" else res.stdout
        } else {
            "Not a git repository (or git uninitialized)"
        }
    }

    suspend fun add(workingDir: File, filePattern: String = "."): ProcessExecutionResult {
        return runtimeManager.executeCommandSync("git add $filePattern", workingDir)
    }

    suspend fun commit(workingDir: File, message: String): ProcessExecutionResult {
        val safeMsg = message.replace("\"", "\\\"")
        return runtimeManager.executeCommandSync("git commit -m \"$safeMsg\"", workingDir)
    }

    suspend fun log(workingDir: File, maxCount: Int = 10): List<String> {
        val res = runtimeManager.executeCommandSync("git log --oneline -n $maxCount", workingDir)
        return if (res.exitCode == 0 && res.stdout.isNotBlank()) {
            res.stdout.lines()
        } else {
            emptyList()
        }
    }

    suspend fun branch(workingDir: File): String {
        val res = runtimeManager.executeCommandSync("git branch --show-current", workingDir)
        return if (res.exitCode == 0 && res.stdout.isNotBlank()) res.stdout else "main"
    }

    /**
     * Computes visual line-by-line diff between two strings.
     */
    fun computeDiff(oldText: String, newText: String): List<DiffLine> {
        val oldLines = oldText.lines()
        val newLines = newText.lines()
        val result = mutableListOf<DiffLine>()

        var i = 0
        var j = 0
        var oldLineNum = 1
        var newLineNum = 1

        while (i < oldLines.size || j < newLines.size) {
            if (i < oldLines.size && j < newLines.size && oldLines[i] == newLines[j]) {
                result.add(DiffLine(DiffType.SAME, oldLines[i], oldLineNum, newLineNum))
                i++
                j++
                oldLineNum++
                newLineNum++
            } else if (j < newLines.size && (i >= oldLines.size || !oldLines.contains(newLines[j]))) {
                result.add(DiffLine(DiffType.ADDED, "+ ${newLines[j]}", null, newLineNum))
                j++
                newLineNum++
            } else if (i < oldLines.size) {
                result.add(DiffLine(DiffType.REMOVED, "- ${oldLines[i]}", oldLineNum, null))
                i++
                oldLineNum++
            }
        }
        return result
    }
}
