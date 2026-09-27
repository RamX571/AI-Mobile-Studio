package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY lastOpenedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getProjectById(id: String): ProjectEntity?

    @Query("SELECT * FROM projects WHERE id = :id")
    fun getProjectByIdFlow(id: String): Flow<ProjectEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("UPDATE projects SET lastOpenedAt = :timestamp WHERE id = :id")
    suspend fun updateLastOpened(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE projects SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProject(id: String)
}

@Dao
interface AgentDao {
    @Query("SELECT * FROM agents ORDER BY displayName ASC")
    fun getAllAgents(): Flow<List<AgentEntity>>

    @Query("SELECT * FROM agents WHERE id = :id")
    suspend fun getAgentById(id: String): AgentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgent(agent: AgentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgents(agents: List<AgentEntity>)

    @Update
    suspend fun updateAgent(agent: AgentEntity)
}

@Dao
interface ProviderDao {
    @Query("SELECT * FROM providers ORDER BY name ASC")
    fun getAllProviders(): Flow<List<ProviderEntity>>

    @Query("SELECT * FROM providers WHERE id = :id")
    suspend fun getProviderById(id: String): ProviderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvider(provider: ProviderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProviders(providers: List<ProviderEntity>)

    @Update
    suspend fun updateProvider(provider: ProviderEntity)
}

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations WHERE projectId = :projectId ORDER BY updatedAt DESC")
    fun getConversationsForProject(projectId: String): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE projectId = :projectId ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getLatestConversation(projectId: String): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("SELECT * FROM tool_calls WHERE messageId = :messageId ORDER BY createdAt ASC")
    fun getToolCallsForMessage(messageId: String): Flow<List<ToolCallEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToolCall(toolCall: ToolCallEntity)

    @Update
    suspend fun updateToolCall(toolCall: ToolCallEntity)

    @Query("SELECT * FROM tool_calls WHERE status = 'WAITING_APPROVAL'")
    fun getPendingApprovalToolCalls(): Flow<List<ToolCallEntity>>
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE projectId = :projectId ORDER BY startTime DESC")
    fun getTasksForProject(projectId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status IN ('QUEUED', 'STARTING', 'RUNNING', 'WAITING_FOR_APPROVAL', 'BUILDING', 'TESTING') ORDER BY startTime DESC")
    fun getActiveTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Query("UPDATE tasks SET status = :status, outputLogs = outputLogs || :logChunk WHERE id = :id")
    suspend fun appendLogs(id: String, status: String, logChunk: String)
}

@Dao
interface TerminalDao {
    @Query("SELECT * FROM terminal_sessions WHERE projectId = :projectId ORDER BY createdAt ASC")
    fun getSessionsForProject(projectId: String): Flow<List<TerminalSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TerminalSessionEntity)

    @Query("DELETE FROM terminal_sessions WHERE id = :id")
    suspend fun deleteSession(id: String)
}

@Dao
interface BuildDao {
    @Query("SELECT * FROM build_records WHERE projectId = :projectId ORDER BY startTime DESC")
    fun getBuildsForProject(projectId: String): Flow<List<BuildRecordEntity>>

    @Query("SELECT * FROM build_records WHERE projectId = :projectId ORDER BY startTime DESC LIMIT 1")
    suspend fun getLatestBuild(projectId: String): BuildRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuild(build: BuildRecordEntity)

    @Update
    suspend fun updateBuild(build: BuildRecordEntity)
}

@Dao
interface CheckpointDao {
    @Query("SELECT * FROM checkpoints WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getCheckpointsForProject(projectId: String): Flow<List<CheckpointEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckpoint(checkpoint: CheckpointEntity)

    @Query("DELETE FROM checkpoints WHERE id = :id")
    suspend fun deleteCheckpoint(id: String)
}

@Dao
interface ToolchainDao {
    @Query("SELECT * FROM toolchains ORDER BY category ASC, name ASC")
    fun getAllToolchains(): Flow<List<ToolchainEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToolchains(toolchains: List<ToolchainEntity>)

    @Update
    suspend fun updateToolchain(toolchain: ToolchainEntity)
}

@Dao
interface PortDao {
    @Query("SELECT * FROM ports WHERE isActive = 1 ORDER BY portNumber ASC")
    fun getActivePorts(): Flow<List<PortEntity>>

    @Query("SELECT * FROM ports WHERE projectId = :projectId AND isActive = 1")
    fun getActivePortsForProject(projectId: String): Flow<List<PortEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPort(port: PortEntity)

    @Query("UPDATE ports SET isActive = 0 WHERE portNumber = :portNumber")
    suspend fun deactivatePort(portNumber: Int)

    @Query("DELETE FROM ports WHERE portNumber = :portNumber")
    suspend fun deletePort(portNumber: Int)
}
