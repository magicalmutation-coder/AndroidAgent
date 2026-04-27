package com.androidagent.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "agent_contacts")
data class AgentContact(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contactId: String = "",
    val name: String,
    val phoneNumber: String = "",
    val email: String = "",
    val isAutoReplyEnabled: Boolean = false,
    val autoReplyChannels: String = "[]", // JSON array: ["sms","email","whatsapp"]
    val customPrompt: String = ""
)
