package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.*
import com.example.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ProjectEntity::class,
        AgentEntity::class,
        ProviderEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        ToolCallEntity::class,
        TaskEntity::class,
        TerminalSessionEntity::class,
        BuildRecordEntity::class,
        CheckpointEntity::class,
        ToolchainEntity::class,
        PortEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class StudioDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun agentDao(): AgentDao
    abstract fun providerDao(): ProviderDao
    abstract fun conversationDao(): ConversationDao
    abstract fun taskDao(): TaskDao
    abstract fun terminalDao(): TerminalDao
    abstract fun buildDao(): BuildDao
    abstract fun checkpointDao(): CheckpointDao
    abstract fun toolchainDao(): ToolchainDao
    abstract fun portDao(): PortDao

    companion object {
        @Volatile
        private var INSTANCE: StudioDatabase? = null

        fun getInstance(context: Context): StudioDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudioDatabase::class.java,
                    "ai_studio_workspace.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            getInstance(context).seedInitialData()
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun seedInitialData() {
        val initialToolchains = listOf(
            ToolchainEntity("node", "Node.js & npm", "node", "v20.18.0", "v22.1.0", 48, true, "READY", "RUNTIME"),
            ToolchainEntity("git", "Git VCS", "git", "2.44.0", "2.46.0", 22, true, "READY", "VCS"),
            ToolchainEntity("python", "Python 3", "python3", "3.11.8", "3.12.3", 35, true, "READY", "LANGUAGE"),
            ToolchainEntity("openjdk", "OpenJDK 17", "java", "17.0.10", "21.0.2", 120, true, "READY", "LANGUAGE"),
            ToolchainEntity("gradle", "Gradle Build Tool", "gradle", "8.7", "8.10", 85, true, "READY", "BUILD"),
            ToolchainEntity("android-sdk", "Android Build Tools & aapt2", "aapt2", "34.0.0", "35.0.0", 160, true, "READY", "BUILD"),
            ToolchainEntity("clang", "Clang / C++ & CMake", "clang", null, "18.1.0", 95, false, "AVAILABLE", "LANGUAGE"),
            ToolchainEntity("php", "PHP & Composer", "php", null, "8.3.6", 42, false, "AVAILABLE", "LANGUAGE")
        )
        toolchainDao().insertToolchains(initialToolchains)

        val initialAgents = listOf(
            AgentEntity(
                id = "claude-code",
                displayName = "Claude Code",
                executable = "claude",
                installMethod = "npm install -g @anthropic-ai/claude-code",
                architecture = "arm64",
                authMethod = "API_KEY",
                supportedModelsJson = "[\"claude-3-7-sonnet\", \"claude-3-5-sonnet\", \"claude-3-5-haiku\"]",
                envVarsJson = "{\"ANTHROPIC_API_KEY\": \"[SECURE_KEYSTORE]\"}",
                capabilitiesJson = "[\"FILE_READ\", \"FILE_WRITE\", \"COMMAND_EXEC\", \"TEST_RUNNER\", \"DIFF_GENERATION\"]",
                isInstalled = true,
                status = "READY"
            ),
            AgentEntity(
                id = "antigravity",
                displayName = "Antigravity CLI",
                executable = "antigravity",
                installMethod = "curl -fsSL https://antigravity.run/install.sh | sh",
                architecture = "arm64",
                authMethod = "API_KEY",
                supportedModelsJson = "[\"gemini-2.5-pro\", \"gemini-2.5-flash\", \"claude-3-7-sonnet\"]",
                envVarsJson = "{\"ANTIGRAVITY_TOKEN\": \"[SECURE_KEYSTORE]\"}",
                capabilitiesJson = "[\"FILE_READ\", \"FILE_WRITE\", \"COMMAND_EXEC\", \"GIT_INTEGRATION\", \"PREVIEW_SERVER\"]",
                isInstalled = true,
                status = "READY"
            ),
            AgentEntity(
                id = "deepseek",
                displayName = "DeepSeek Coder CLI",
                executable = "deepseek-cli",
                installMethod = "pip install deepseek-coder-cli",
                architecture = "arm64",
                authMethod = "API_KEY",
                supportedModelsJson = "[\"deepseek-reasoner\", \"deepseek-chat\"]",
                envVarsJson = "{\"DEEPSEEK_API_KEY\": \"[SECURE_KEYSTORE]\"}",
                capabilitiesJson = "[\"FILE_READ\", \"FILE_WRITE\", \"COMMAND_EXEC\", \"THINKING_REASONING\"]",
                isInstalled = true,
                status = "READY"
            ),
            AgentEntity(
                id = "opencode",
                displayName = "OpenCode Assistant",
                executable = "opencode",
                installMethod = "npm install -g opencode-ai",
                architecture = "arm64",
                authMethod = "NONE",
                supportedModelsJson = "[\"local-qwen-2.5-coder\", \"openrouter/auto\"]",
                envVarsJson = "{\"OPENROUTER_API_KEY\": \"[SECURE_KEYSTORE]\"}",
                capabilitiesJson = "[\"FILE_READ\", \"FILE_WRITE\", \"COMMAND_EXEC\", \"OFFLINE_CAPABLE\"]",
                isInstalled = true,
                status = "READY"
            ),
            AgentEntity(
                id = "custom-cli",
                displayName = "Custom CLI Agent",
                executable = "agent-cli",
                installMethod = "User defined script in /usr/local/bin",
                architecture = "arm64",
                authMethod = "CUSTOM_ENV",
                supportedModelsJson = "[\"custom-model\"]",
                envVarsJson = "{}",
                capabilitiesJson = "[\"FILE_READ\", \"FILE_WRITE\", \"COMMAND_EXEC\"]",
                isInstalled = true,
                status = "READY"
            )
        )
        agentDao().insertAgents(initialAgents)

        val initialProviders = listOf(
            ProviderEntity(
                id = "anthropic",
                name = "Anthropic",
                baseUrl = "https://api.anthropic.com/v1",
                authType = "API_KEY",
                modelsJson = "[\"claude-3-7-sonnet\", \"claude-3-5-sonnet\", \"claude-3-5-haiku\"]",
                supportsStreaming = true,
                supportsTools = true,
                supportsThinking = true,
                isConfigured = false
            ),
            ProviderEntity(
                id = "deepseek",
                name = "DeepSeek",
                baseUrl = "https://api.deepseek.com/v1",
                authType = "API_KEY",
                modelsJson = "[\"deepseek-reasoner (R1)\", \"deepseek-chat (V3)\"]",
                supportsStreaming = true,
                supportsTools = true,
                supportsThinking = true,
                isConfigured = false
            ),
            ProviderEntity(
                id = "openrouter",
                name = "OpenRouter",
                baseUrl = "https://openrouter.ai/api/v1",
                authType = "API_KEY",
                modelsJson = "[\"anthropic/claude-3.7-sonnet\", \"deepseek/deepseek-r1\", \"meta-llama/llama-3.3-70b-instruct\"]",
                supportsStreaming = true,
                supportsTools = true,
                supportsThinking = true,
                isConfigured = false
            ),
            ProviderEntity(
                id = "google",
                name = "Google Gemini Endpoint",
                baseUrl = "https://generativelanguage.googleapis.com/v1beta",
                authType = "API_KEY",
                modelsJson = "[\"gemini-2.5-pro\", \"gemini-2.5-flash\", \"gemini-2.0-flash\"]",
                supportsStreaming = true,
                supportsTools = true,
                supportsThinking = true,
                isConfigured = false
            ),
            ProviderEntity(
                id = "custom",
                name = "Custom Anthropic/OpenAI Endpoint",
                baseUrl = "http://localhost:8000/v1",
                authType = "API_KEY",
                modelsJson = "[\"custom-llm-model\"]",
                supportsStreaming = true,
                supportsTools = true,
                supportsThinking = false,
                isConfigured = false
            )
        )
        providerDao().insertProviders(initialProviders)
    }
}
