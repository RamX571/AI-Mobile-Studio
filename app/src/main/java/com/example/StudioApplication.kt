package com.example

import android.app.Application
import com.example.agent.AgentRegistry
import com.example.build.AndroidBuildManager
import com.example.checkpoint.CheckpointManager
import com.example.data.local.StudioDatabase
import com.example.data.security.SecureCredentialManager
import com.example.git.GitRepositoryManager
import com.example.runtime.LinuxRuntimeManager
import com.example.workspace.WorkspaceManager

class StudioApplication : Application() {

    lateinit var database: StudioDatabase
        private set
    lateinit var secureCredentialManager: SecureCredentialManager
        private set
    lateinit var workspaceManager: WorkspaceManager
        private set
    lateinit var runtimeManager: LinuxRuntimeManager
        private set
    lateinit var agentRegistry: AgentRegistry
        private set
    lateinit var gitManager: GitRepositoryManager
        private set
    lateinit var checkpointManager: CheckpointManager
        private set
    lateinit var buildManager: AndroidBuildManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = StudioDatabase.getInstance(this)
        secureCredentialManager = SecureCredentialManager(this)
        workspaceManager = WorkspaceManager(this)
        runtimeManager = LinuxRuntimeManager(this)
        agentRegistry = AgentRegistry(database.agentDao(), runtimeManager)
        gitManager = GitRepositoryManager(runtimeManager)
        checkpointManager = CheckpointManager(workspaceManager, database.checkpointDao())
        buildManager = AndroidBuildManager(this, workspaceManager, runtimeManager, database.buildDao())
    }

    companion object {
        lateinit var instance: StudioApplication
            private set
    }
}
