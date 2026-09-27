package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.UUID

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: String, // React, Next.js, Vite, Node.js, static HTML, Python, Android/Kotlin, C/C++, PHP, Generic
    val workspacePath: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastOpenedAt: Long = System.currentTimeMillis(),
    val activeAgentId: String = "claude-code",
    val activeModelId: String = "claude-3-7-sonnet",
    val envProfile: String = "Default",
    val gitState: String = "clean",
    val status: String = "IDLE", // IDLE, RUNNING_TASK, BUILDING, SERVING
    val permissionMode: String = "ASK_SENSITIVE" // SAFE, ASK_SENSITIVE, TRUSTED
)

@Entity(tableName = "agents")
data class AgentEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val executable: String,
    val installMethod: String,
    val architecture: String,
    val authMethod: String,
    val supportedModelsJson: String,
    val envVarsJson: String,
    val capabilitiesJson: String,
    val isInstalled: Boolean = true,
    val status: String = "READY"
)

@Entity(tableName = "providers")
data class ProviderEntity(
    @PrimaryKey val id: String,
    val name: String,
    val baseUrl: String,
    val authType: String, // API_KEY, OAUTH, NONE
    val modelsJson: String,
    val supportsStreaming: Boolean = true,
    val supportsTools: Boolean = true,
    val supportsThinking: Boolean = false,
    val isConfigured: Boolean = false
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val title: String = "Coding Session",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val conversationId: String,
    val role: String, // user, assistant, system, tool
    val content: String,
    val reasoningContent: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val tokenCount: Int = 0
)

@Entity(tableName = "tool_calls")
data class ToolCallEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val messageId: String,
    val toolName: String,
    val inputJson: String,
    val outputJson: String? = null,
    val status: String = "COMPLETED", // PENDING, RUNNING, COMPLETED, FAILED, WAITING_APPROVAL, REJECTED
    val durationMs: Long = 0,
    val requiresApproval: Boolean = false,
    val isApproved: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val agentId: String,
    val sessionId: String,
    val status: String = "QUEUED", // QUEUED, STARTING, RUNNING, WAITING_FOR_APPROVAL, BUILDING, TESTING, COMPLETED, FAILED, CANCELLED
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val outputLogs: String = "",
    val cancellationState: Boolean = false,
    val errorState: String? = null,
    val completionSummary: String? = null
)

@Entity(tableName = "terminal_sessions")
data class TerminalSessionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val title: String,
    val cwd: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastActiveAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "build_records")
data class BuildRecordEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val buildType: String, // ANDROID_DEBUG, ANDROID_RELEASE, WEB_VITE, NODE_BUILD
    val status: String = "RUNNING", // RUNNING, SUCCESS, FAILED
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long? = null,
    val logSummary: String = "",
    val apkPath: String? = null,
    val apkPackageName: String? = null,
    val apkVersion: String? = null,
    val apkSizeBytes: Long = 0
)

@Entity(tableName = "checkpoints")
data class CheckpointEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val projectId: String,
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val snapshotRef: String,
    val commitHash: String? = null,
    val filesChangedCount: Int = 0
)

@Entity(tableName = "toolchains")
data class ToolchainEntity(
    @PrimaryKey val id: String,
    val name: String,
    val executableName: String,
    val installedVersion: String? = null,
    val availableVersion: String,
    val sizeMb: Int,
    val isInstalled: Boolean,
    val status: String = "READY", // READY, DOWNLOADING, EXTRACTING, ERROR
    val category: String = "RUNTIME" // RUNTIME, BUILD, LANGUAGE, VCS
)

@Entity(tableName = "ports")
data class PortEntity(
    @PrimaryKey val portNumber: Int,
    val projectId: String?,
    val protocol: String = "HTTP",
    val serviceName: String,
    val pid: Int,
    val isActive: Boolean = true,
    val url: String = "http://localhost:$portNumber"
)
