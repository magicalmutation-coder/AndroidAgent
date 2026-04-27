package com.androidagent.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidagent.CompanionApplication
import com.androidagent.data.database.entities.Message
import com.androidagent.data.repository.ChatRepository
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val db by lazy { CompanionApplication.instance.database }
    private val llmManager by lazy {
        com.androidagent.llm.LLMManager.getInstance(CompanionApplication.instance)
    }
    private val chatRepository by lazy {
        ChatRepository(db.messageDao(), llmManager)
    }
    private val agentManager by lazy { CompanionApplication.instance.agentManager }

    private var currentConversationId = "default"

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadConversation(conversationId: String = "default") {
        currentConversationId = conversationId
        db.messageDao().getByConversation(conversationId).observeForever { msgs ->
            _messages.postValue(msgs)
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        _isLoading.value = true
        agentManager.updateAriaStatus("Thinking... 🤔")

        viewModelScope.launch {
            try {
                chatRepository.sendMessage(currentConversationId, text)
                agentManager.updateAriaStatus("Ready to chat! ✨")
            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}")
                agentManager.updateAriaStatus("Something went wrong 😞")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun clearConversation() {
        viewModelScope.launch {
            chatRepository.clearConversation(currentConversationId)
        }
    }

    init {
        loadConversation()
    }
}
