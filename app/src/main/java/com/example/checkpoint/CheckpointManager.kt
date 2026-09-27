package com.example.checkpoint

import com.example.data.local.dao.CheckpointDao
import com.example.data.local.entity.CheckpointEntity
import com.example.workspace.WorkspaceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class CheckpointManager(
    private val workspaceManager: WorkspaceManager,
    private val checkpointDao: CheckpointDao
) {
    suspend fun createCheckpoint(
        projectId: String,
        title: String,
        description: String = "Manual project snapshot"
    ): CheckpointEntity = withContext(Dispatchers.IO) {
        val checkpointId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val snapshotDir = File(workspaceManager.getAiDir(projectId), "checkpoints/$checkpointId").apply { mkdirs() }

        val sourceDir = workspaceManager.getSourceDir(projectId)
        var fileCount = 0
        sourceDir.walkTopDown().forEach { file ->
            if (file.isFile) {
                fileCount++
                val rel = file.relativeTo(sourceDir).path
                val dest = File(snapshotDir, rel)
                dest.parentFile?.mkdirs()
                file.copyTo(dest, overwrite = true)
            }
        }

        val entity = CheckpointEntity(
            id = checkpointId,
            projectId = projectId,
            title = title,
            description = description,
            timestamp = timestamp,
            snapshotRef = snapshotDir.absolutePath,
            filesChangedCount = fileCount
        )
        checkpointDao.insertCheckpoint(entity)
        entity
    }

    suspend fun restoreCheckpoint(checkpoint: CheckpointEntity): Boolean = withContext(Dispatchers.IO) {
        val snapshotDir = File(checkpoint.snapshotRef)
        if (!snapshotDir.exists()) return@withContext false

        val sourceDir = workspaceManager.getSourceDir(checkpoint.projectId)
        snapshotDir.walkTopDown().forEach { file ->
            if (file.isFile) {
                val rel = file.relativeTo(snapshotDir).path
                val dest = File(sourceDir, rel)
                dest.parentFile?.mkdirs()
                file.copyTo(dest, overwrite = true)
            }
        }
        true
    }

    suspend fun deleteCheckpoint(checkpoint: CheckpointEntity) = withContext(Dispatchers.IO) {
        File(checkpoint.snapshotRef).deleteRecursively()
        checkpointDao.deleteCheckpoint(checkpoint.id)
    }
}
