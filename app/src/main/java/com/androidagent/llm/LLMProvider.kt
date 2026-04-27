package com.androidagent.llm

import com.androidagent.data.database.entities.LLMConfig

data class LLMMessage(val role: String, val content: String)

interface LLMProvider {
    suspend fun chat(messages: List<LLMMessage>, config: LLMConfig): String
    suspend fun listModels(config: LLMConfig): List<String>
}
