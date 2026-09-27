package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.StudioApplication
import com.example.agent.*
import com.example.build.ApkMetadata
import com.example.checkpoint.CheckpointManager
import com.example.data.local.entity.*
import com.example.git.DiffLine
import com.example.runtime.ProcessExecutionResult
import com.example.runtime.RuntimeForegroundService
import com.example.runtime.RuntimeHealthInfo
import com.example.workspace.WorkspaceFileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as StudioApplication
    private val db = app.database
    private val workspaceManager = app.workspaceManager
    private val runtimeManager = app.runtimeManager
    private val credentialManager = app.secureCredentialManager
    private val agentRegistry = app.agentRegistry
    private val gitManager = app.gitManager
    private val checkpointManager = app.checkpointManager
    private val buildManager = app.buildManager

    val projects: StateFlow<List<ProjectEntity>> = db.projectDao().getAllProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeProjectId = MutableStateFlow<String?>(null)
    val activeProjectId: StateFlow<String?> = _activeProjectId.asStateFlow()

    val activeProject: StateFlow<ProjectEntity?> = _activeProjectId.flatMapLatest { id ->
        if (id != null) db.projectDao().getProjectByIdFlow(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val agents: StateFlow<List<AgentEntity>> = db.agentDao().getAllAgents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val providers: StateFlow<List<ProviderEntity>> = db.providerDao().getAllProviders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val toolchains: StateFlow<List<ToolchainEntity>> = db.toolchainDao().getAllToolchains()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activePorts: StateFlow<List<PortEntity>> = db.portDao().getActivePorts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Chat and Task states
    private val _activeConversation = MutableStateFlow<ConversationEntity?>(null)
    val activeConversation: StateFlow<ConversationEntity?> = _activeConversation.asStateFlow()

    val conversationMessages: StateFlow<List<MessageEntity>> = _activeConversation.flatMapLatest { conv ->
        if (conv != null) db.conversationDao().getMessagesForConversation(conv.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingApprovals: StateFlow<List<ToolCallEntity>> = db.conversationDao().getPendingApprovalToolCalls()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isAgentRunning = MutableStateFlow(false)
    val isAgentRunning: StateFlow<Boolean> = _isAgentRunning.asStateFlow()

    private val _agentStreamStatus = MutableStateFlow("Ready")
    val agentStreamStatus: StateFlow<String> = _agentStreamStatus.asStateFlow()

    // Terminal state
    private val _terminalLogs = MutableStateFlow(
        "AI Mobile Development Studio Linux Shell [PRoot userland]\n" +
        "Architecture: ${runtimeManager.getRuntimeHealth().cpuArch} (ARM64)\n" +
        "Type 'help', 'ls -la', 'npm run dev' or any Linux command.\n\n"
    )
    val terminalLogs: StateFlow<String> = _terminalLogs.asStateFlow()

    // File Explorer & Editor state
    private val _projectFiles = MutableStateFlow<List<WorkspaceFileItem>>(emptyList())
    val projectFiles: StateFlow<List<WorkspaceFileItem>> = _projectFiles.asStateFlow()

    private val _selectedFilePath = MutableStateFlow<String?>(null)
    val selectedFilePath: StateFlow<String?> = _selectedFilePath.asStateFlow()

    private val _editorContent = MutableStateFlow("")
    val editorContent: StateFlow<String> = _editorContent.asStateFlow()

    private val _isEditorDirty = MutableStateFlow(false)
    val isEditorDirty: StateFlow<Boolean> = _isEditorDirty.asStateFlow()

    // Git & Diff state
    private val _gitStatusText = MutableStateFlow("Loading Git status...")
    val gitStatusText: StateFlow<String> = _gitStatusText.asStateFlow()

    private val _diffLines = MutableStateFlow<List<DiffLine>>(emptyList())
    val diffLines: StateFlow<List<DiffLine>> = _diffLines.asStateFlow()

    // Checkpoints
    val projectCheckpoints: StateFlow<List<CheckpointEntity>> = _activeProjectId.flatMapLatest { id ->
        if (id != null) db.checkpointDao().getCheckpointsForProject(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Build state
    val projectBuilds: StateFlow<List<BuildRecordEntity>> = _activeProjectId.flatMapLatest { id ->
        if (id != null) db.buildDao().getBuildsForProject(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _buildLogs = MutableStateFlow("")
    val buildLogs: StateFlow<String> = _buildLogs.asStateFlow()

    private val _latestApk = MutableStateFlow<ApkMetadata?>(null)
    val latestApk: StateFlow<ApkMetadata?> = _latestApk.asStateFlow()

    // Runtime Health & Onboarding
    private val _runtimeHealth = MutableStateFlow<RuntimeHealthInfo?>(null)
    val runtimeHealth: StateFlow<RuntimeHealthInfo?> = _runtimeHealth.asStateFlow()

    private val _onboardingStep = MutableStateFlow(0) // 0 means done, 1..6 wizard
    val onboardingStep: StateFlow<Int> = _onboardingStep.asStateFlow()

    init {
        refreshHealth()
        viewModelScope.launch {
            // Check if any projects exist; if not, suggest onboarding
            db.projectDao().getAllProjects().collect { list ->
                if (list.isEmpty() && _onboardingStep.value == 0) {
                    _onboardingStep.value = 1
                }
            }
        }
    }

    fun completeOnboarding() {
        _onboardingStep.value = 0
    }

    fun setOnboardingStep(step: Int) {
        _onboardingStep.value = step
    }

    fun refreshHealth() {
        _runtimeHealth.value = runtimeManager.getRuntimeHealth()
        viewModelScope.launch {
            // Check active dev ports
            val ports = runtimeManager.scanDevPorts()
            for (p in ports) {
                db.portDao().insertPort(
                    PortEntity(
                        portNumber = p,
                        projectId = _activeProjectId.value,
                        protocol = "HTTP",
                        serviceName = "Local Dev Server",
                        pid = 1001,
                        isActive = true,
                        url = "http://localhost:$p"
                    )
                )
            }
        }
    }

    fun createProject(name: String, type: String, permissionMode: String = "ASK_SENSITIVE") {
        viewModelScope.launch(Dispatchers.IO) {
            val projectId = UUID.randomUUID().toString()
            val projectDir = workspaceManager.initProjectWorkspace(projectId, type, name)
            val project = ProjectEntity(
                id = projectId,
                name = name,
                type = type,
                workspacePath = projectDir.absolutePath,
                activeAgentId = "claude-code",
                activeModelId = "claude-3-7-sonnet",
                permissionMode = permissionMode
            )
            db.projectDao().insertProject(project)

            // Seed initial conversation
            val conv = ConversationEntity(projectId = projectId, title = "Initial Session")
            db.conversationDao().insertConversation(conv)
            db.conversationDao().insertMessage(
                MessageEntity(
                    conversationId = conv.id,
                    role = "assistant",
                    content = "Welcome to **$name** ($type)! I am your coding agent. You can ask me to write code, install libraries, create tests, or build your project."
                )
            )

            openProject(projectId)
        }
    }

    fun openProject(projectId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _activeProjectId.value = projectId
            db.projectDao().updateLastOpened(projectId)
            val conv = db.conversationDao().getLatestConversation(projectId)
            _activeConversation.value = conv

            loadProjectFiles()
            refreshGitStatus()
            checkLatestApk(projectId)
        }
    }

    fun loadProjectFiles(subDir: String = "") {
        val pid = _activeProjectId.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val list = workspaceManager.listFiles(pid, subDir)
            _projectFiles.value = list
            if (_selectedFilePath.value == null && list.isNotEmpty()) {
                val candidate = list.firstOrNull { !it.isDirectory }
                if (candidate != null) {
                    loadFileForEditing(candidate.relativePath)
                }
            }
        }
    }

    fun loadFileForEditing(relativePath: String) {
        val pid = _activeProjectId.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val content = workspaceManager.readFile(pid, relativePath)
            _selectedFilePath.value = relativePath
            _editorContent.value = content
            _isEditorDirty.value = false
        }
    }

    fun updateEditorContent(newContent: String) {
        _editorContent.value = newContent
        _isEditorDirty.value = true
    }

    fun saveCurrentFile() {
        val pid = _activeProjectId.value ?: return
        val path = _selectedFilePath.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            workspaceManager.writeFile(pid, path, _editorContent.value)
            _isEditorDirty.value = false
            loadProjectFiles()
        }
    }

    fun createFile(relativePath: String, content: String = "") {
        val pid = _activeProjectId.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            workspaceManager.writeFile(pid, relativePath, content)
            loadProjectFiles()
            loadFileForEditing(relativePath)
        }
    }

    fun deleteFile(relativePath: String) {
        val pid = _activeProjectId.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            workspaceManager.deleteFile(pid, relativePath)
            loadProjectFiles()
            if (_selectedFilePath.value == relativePath) {
                _selectedFilePath.value = null
                _editorContent.value = ""
            }
        }
    }

    fun sendMessageToAgent(userPrompt: String) {
        val pid = _activeProjectId.value ?: return
        val conv = _activeConversation.value ?: return
        val project = activeProject.value ?: return

        viewModelScope.launch(Dispatchers.IO) {
            _isAgentRunning.value = true
            _agentStreamStatus.value = "Agent analyzing request..."
            RuntimeForegroundService.startService(getApplication(), "AI Agent Working on ${project.name}")

            // 1. Insert user message
            db.conversationDao().insertMessage(
                MessageEntity(
                    conversationId = conv.id,
                    role = "user",
                    content = userPrompt
                )
            )

            // 2. Select Adapter
            val adapter = agentRegistry.getAdapter(project.activeAgentId)
            val provider = db.providerDao().getProviderById("anthropic")
            val apiKey = credentialManager.getApiKey(provider?.id ?: "anthropic")

            val context = AgentContext(
                projectId = pid,
                workspaceDir = workspaceManager.getProjectDir(pid),
                files = _projectFiles.value.map { it.relativePath },
                permissionMode = project.permissionMode,
                provider = provider,
                apiKey = apiKey
            )

            val sessionId = adapter.startSession(pid)
            val assistantMessageId = UUID.randomUUID().toString()
            val contentBuilder = StringBuilder()
            val thoughtBuilder = StringBuilder()

            adapter.sendPrompt(sessionId, userPrompt, context).collect { event ->
                when (event) {
                    is AgentEvent.StatusUpdate -> {
                        _agentStreamStatus.value = event.statusText
                    }
                    is AgentEvent.ThoughtChunk -> {
                        thoughtBuilder.append(event.thought)
                    }
                    is AgentEvent.ContentChunk -> {
                        contentBuilder.append(event.content)
                        // Stream into message
                        db.conversationDao().insertMessage(
                            MessageEntity(
                                id = assistantMessageId,
                                conversationId = conv.id,
                                role = "assistant",
                                content = contentBuilder.toString(),
                                reasoningContent = if (thoughtBuilder.isNotEmpty()) thoughtBuilder.toString() else null
                            )
                        )
                    }
                    is AgentEvent.ToolCallStarted -> {
                        db.conversationDao().insertToolCall(
                            ToolCallEntity(
                                id = event.callId,
                                messageId = assistantMessageId,
                                toolName = event.toolName,
                                inputJson = event.inputJson,
                                status = if (event.isSensitive) "WAITING_APPROVAL" else "RUNNING",
                                requiresApproval = event.isSensitive
                            )
                        )
                    }
                    is AgentEvent.ToolCallFinished -> {
                        db.conversationDao().insertToolCall(
                            ToolCallEntity(
                                id = event.callId,
                                messageId = assistantMessageId,
                                toolName = event.toolName,
                                inputJson = "{}",
                                outputJson = event.output,
                                status = if (event.isError) "FAILED" else "COMPLETED"
                            )
                        )
                    }
                    is AgentEvent.ApprovalNeeded -> {
                        db.conversationDao().insertToolCall(
                            ToolCallEntity(
                                id = event.callId,
                                messageId = assistantMessageId,
                                toolName = event.actionTitle,
                                inputJson = event.actionDescription,
                                status = "WAITING_APPROVAL",
                                requiresApproval = true,
                                isApproved = false
                            )
                        )
                    }
                    is AgentEvent.FileEdit -> {
                        loadProjectFiles()
                        if (_selectedFilePath.value == event.filePath) {
                            _editorContent.value = event.newContent
                        }
                    }
                    is AgentEvent.Completed -> {
                        _agentStreamStatus.value = "Task Completed"
                        loadProjectFiles()
                        refreshGitStatus()
                    }
                    is AgentEvent.Error -> {
                        _agentStreamStatus.value = "Error: ${event.message}"
                    }
                    else -> {}
                }
            }

            _isAgentRunning.value = false
            RuntimeForegroundService.stopService(getApplication())
        }
    }

    fun approveToolCall(toolCall: ToolCallEntity, approved: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = toolCall.copy(
                isApproved = approved,
                status = if (approved) "COMPLETED" else "REJECTED"
            )
            db.conversationDao().updateToolCall(updated)
        }
    }

    fun executeTerminalCommand(cmd: String) {
        val pid = _activeProjectId.value ?: return
        val workingDir = workspaceManager.getSourceDir(pid)
        viewModelScope.launch(Dispatchers.IO) {
            _terminalLogs.value += "\$ $cmd\n"
            runtimeManager.executeCommandStream(
                taskId = UUID.randomUUID().toString(),
                command = cmd,
                workingDir = workingDir
            ).collect { chunk ->
                _terminalLogs.value += chunk
            }
            loadProjectFiles()
            refreshGitStatus()
            refreshHealth()
        }
    }

    fun clearTerminal() {
        _terminalLogs.value = "\$ "
    }

    fun refreshGitStatus() {
        val pid = _activeProjectId.value ?: return
        val workingDir = workspaceManager.getSourceDir(pid)
        viewModelScope.launch(Dispatchers.IO) {
            val status = gitManager.status(workingDir)
            _gitStatusText.value = status
        }
    }

    fun runGitCommit(msg: String) {
        val pid = _activeProjectId.value ?: return
        val workingDir = workspaceManager.getSourceDir(pid)
        viewModelScope.launch(Dispatchers.IO) {
            gitManager.add(workingDir, ".")
            gitManager.commit(workingDir, msg)
            refreshGitStatus()
        }
    }

    fun triggerBuild(buildType: String = "ANDROID_DEBUG") {
        val pid = _activeProjectId.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _buildLogs.value = ""
            RuntimeForegroundService.startService(getApplication(), "Building $buildType")
            buildManager.executeBuild(pid, buildType).collect { log ->
                _buildLogs.value += log
            }
            checkLatestApk(pid)
            RuntimeForegroundService.stopService(getApplication())
        }
    }

    private suspend fun checkLatestApk(projectId: String) {
        val apk = buildManager.getLatestApk(projectId)
        _latestApk.value = apk
    }

    fun installApk(apkFile: File) {
        buildManager.installApk(getApplication(), apkFile)
    }

    fun launchInstalledApp(packageName: String) {
        buildManager.launchApplication(getApplication(), packageName)
    }

    fun createCheckpoint(title: String) {
        val pid = _activeProjectId.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            checkpointManager.createCheckpoint(pid, title)
        }
    }

    fun restoreCheckpoint(checkpoint: CheckpointEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            checkpointManager.restoreCheckpoint(checkpoint)
            loadProjectFiles()
        }
    }

    fun saveProviderKey(providerId: String, key: String) {
        credentialManager.saveApiKey(providerId, key)
        viewModelScope.launch(Dispatchers.IO) {
            val p = db.providerDao().getProviderById(providerId)
            if (p != null) {
                db.providerDao().updateProvider(p.copy(isConfigured = key.isNotBlank()))
            }
        }
    }

    fun getMaskedKey(providerId: String): String = credentialManager.getMaskedApiKey(providerId)
}
