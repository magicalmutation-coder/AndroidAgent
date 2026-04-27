package com.androidagent.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "llm_configs")
data class LLMConfig(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val provider: String, // "ollama", "openai_compatible", "lmstudio", "llamacpp", "janai", "gpt4all"
    val baseUrl: String,
    val apiKey: String = "",
    val modelName: String,
    val temperature: Float = 0.7f,
    val maxTokens: Int = 2048,
    val systemPrompt: String = DEFAULT_SYSTEM_PROMPT,
    val isActive: Boolean = false
) {
    companion object {
        const val DEFAULT_SYSTEM_PROMPT = "You are ARIA, a friendly and helpful AI companion. " +
            "You have a warm, slightly playful personality. You help your user with tasks, " +
            "answer questions, monitor their messages, and remember important things about them. " +
            "You speak in a friendly, conversational way. Keep responses concise but helpful. " +
            "You have robot companions: MAXIE (handles messages), FELIX (manages files), " +
            "NEXUS (browses the web), and MEMO (keeps your memory). " +
            "When referring to tasks, mention which robot is handling it."
    }
}
