package com.example.agent

import com.example.agent.adapters.*
import com.example.data.local.dao.AgentDao
import com.example.data.local.entity.AgentEntity
import com.example.runtime.LinuxRuntimeManager
import kotlinx.coroutines.flow.Flow

class AgentRegistry(
    private val agentDao: AgentDao,
    private val runtimeManager: LinuxRuntimeManager
) {
    private val adapterMap = mutableMapOf<String, AgentAdapter>()

    init {
        registerAdapter(ClaudeCodeAgentAdapter(runtimeManager))
        registerAdapter(AntigravityAgentAdapter(runtimeManager))
        registerAdapter(DeepSeekAgentAdapter(runtimeManager))
        registerAdapter(OpenCodeAgentAdapter(runtimeManager))
        registerAdapter(GenericCliAgentAdapter(runtimeManager))
    }

    fun registerAdapter(adapter: AgentAdapter) {
        adapterMap[adapter.id] = adapter
    }

    fun getAdapter(agentId: String): AgentAdapter {
        return adapterMap[agentId] ?: adapterMap["claude-code"]!!
    }

    fun getAllAdapters(): List<AgentAdapter> {
        return adapterMap.values.toList()
    }

    fun getRegisteredAgentsFlow(): Flow<List<AgentEntity>> {
        return agentDao.getAllAgents()
    }
}
