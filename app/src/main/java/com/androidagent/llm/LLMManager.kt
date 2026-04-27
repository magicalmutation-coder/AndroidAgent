package com.androidagent.llm

import android.content.Context
import com.androidagent.data.database.AppDatabase
import com.androidagent.data.database.entities.LLMConfig
import com.androidagent.data.repository.MemoryRepository
import com.androidagent.llm.providers.OllamaProvider
import com.androidagent.llm.providers.OpenAICompatibleProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LLMManager private constructor(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val memoryRepository = MemoryRepository(db.memoryDao())

    private fun getProvider(config: LLMConfig): LLMProvider {
        return when (config.provider) {
            "ollama" -> OllamaProvider()
            else -> OpenAICompatibleProvider()
        }
    }

    suspend fun sendMessage(conversationId: String, userMessage: String): String =
        withContext(Dispatchers.IO) {
            val config = db.llmConfigDao().getActive() ?: createDefaultConfig()

            val history = db.messageDao().getByConversationSync(conversationId)
                .takeLast(20)

            val memorySummary = memoryRepository.buildContextSummary()
            val systemContent = buildString {
                append(config.systemPrompt)
                if (memorySummary.isNotBlank()) {
                    append("\n\nThings I remember about the user:\n")
                    append(memorySummary)
                }
            }

            val messages = mutableListOf<LLMMessage>()
            messages.add(LLMMessage("system", systemContent))
            history.forEach { msg ->
                if (msg.role != "system") {
                    messages.add(LLMMessage(msg.role, msg.content))
                }
            }
            messages.add(LLMMessage("user", userMessage))

            try {
                val provider = getProvider(config)
                provider.chat(messages, config)
            } catch (e: Exception) {
                "Hmm, I'm having trouble connecting to my brain right now! Check your LLM server. (${e.message})"
            }
        }

    suspend fun sendRawMessage(prompt: String): String = withContext(Dispatchers.IO) {
        val config = db.llmConfigDao().getActive() ?: createDefaultConfig()
        try {
            val provider = getProvider(config)
            provider.chat(listOf(LLMMessage("user", prompt)), config)
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun listModels(): List<String> = withContext(Dispatchers.IO) {
        val config = db.llmConfigDao().getActive() ?: return@withContext emptyList()
        try {
            getProvider(config).listModels(config)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun createDefaultConfig(): LLMConfig {
        val config = LLMConfig(
            name = "Default Ollama",
            provider = "ollama",
            baseUrl = "http://10.0.2.2:11434",
            modelName = "llama2",
            isActive = true
        )
        val id = db.llmConfigDao().insert(config)
        return config.copy(id = id)
    }

    companion object {
        @Volatile
        private var INSTANCE: LLMManager? = null

        fun getInstance(context: Context): LLMManager {
            return INSTANCE ?: synchronized(this) {
                LLMManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
