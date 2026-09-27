package com.example.agent

import com.example.data.local.entity.ProviderEntity
import kotlinx.coroutines.flow.Flow
import java.io.File

data class AgentCapabilities(
    val supportsStreaming: Boolean = true,
    val supportsToolCalling: Boolean = true,
    val supportsThinking: Boolean = false,
    val supportsFileEditing: Boolean = true,
    val supportsCommandExec: Boolean = true,
    val supportsGit: Boolean = true,
    val supportsTestRunner: Boolean = true,
    val requiresNetwork: Boolean = true,
    val executionMode: String = "CLI" // CLI, API, SUBPROCESS
)

enum class SessionStatus {
    IDLE,
    RUNNING,
    WAITING_FOR_INPUT,
    WAITING_FOR_APPROVAL,
    TERMINATED,
    ERROR
}

data class AgentContext(
    val projectId: String,
    val workspaceDir: File,
    val files: List<String>,
    val permissionMode: String, // SAFE, ASK_SENSITIVE, TRUSTED
    val provider: ProviderEntity?,
    val apiKey: String?
)

sealed class AgentEvent {
    data class StatusUpdate(val statusText: String) : AgentEvent()
    data class ThoughtChunk(val thought: String) : AgentEvent()
    data class ContentChunk(val content: String) : AgentEvent()
    data class ToolCallStarted(
        val callId: String,
        val toolName: String,
        val description: String,
        val inputJson: String,
        val isSensitive: Boolean = false
    ) : AgentEvent()
    data class ToolCallFinished(
        val callId: String,
        val toolName: String,
        val output: String,
        val isError: Boolean = false
    ) : AgentEvent()
    data class FileEdit(
        val filePath: String,
        val oldContent: String?,
        val newContent: String,
        val diffSummary: String
    ) : AgentEvent()
    data class CommandRun(
        val command: String,
        val exitCode: Int,
        val outputSummary: String
    ) : AgentEvent()
    data class ApprovalNeeded(
        val callId: String,
        val actionTitle: String,
        val actionDescription: String,
        val dangerousLevel: String = "HIGH"
    ) : AgentEvent()
    data class Completed(val summary: String, val modifiedFiles: List<String>) : AgentEvent()
    data class Error(val message: String, val canRetry: Boolean = true) : AgentEvent()
}

interface AgentAdapter {
    val id: String
    val displayName: String

    suspend fun install(): Flow<String>
    suspend fun authenticate(credentials: Map<String, String>): Boolean
    suspend fun startSession(projectId: String): String
    suspend fun resumeSession(sessionId: String): Boolean
    fun sendPrompt(sessionId: String, prompt: String, context: AgentContext): Flow<AgentEvent>
    suspend fun stopSession(sessionId: String)
    suspend fun getSessionStatus(sessionId: String): SessionStatus
    fun getCapabilities(): AgentCapabilities
    fun configureProvider(provider: ProviderEntity)
    fun configureModel(modelId: String)
}
