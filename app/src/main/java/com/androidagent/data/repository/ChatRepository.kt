package com.androidagent.data.repository

import androidx.lifecycle.LiveData
import com.androidagent.data.database.dao.MessageDao
import com.androidagent.data.database.entities.Message
import com.androidagent.llm.LLMManager

class ChatRepository(
    private val messageDao: MessageDao,
    private val llmManager: LLMManager
) {

    fun getConversationMessages(conversationId: String): LiveData<List<Message>> =
        messageDao.getByConversation(conversationId)

    suspend fun sendMessage(conversationId: String, userText: String): String {
        val userMessage = Message(
            conversationId = conversationId,
            role = "user",
            content = userText
        )
        messageDao.insert(userMessage)

        val reply = llmManager.sendMessage(conversationId, userText)

        val assistantMessage = Message(
            conversationId = conversationId,
            role = "assistant",
            content = reply,
            agentName = "ARIA"
        )
        messageDao.insert(assistantMessage)

        return reply
    }

    suspend fun clearConversation(conversationId: String) {
        messageDao.deleteByConversation(conversationId)
    }

    suspend fun getRecentMessages(limit: Int = 20): List<Message> =
        messageDao.getRecent(limit)
}
