package com.androidagent.agents

import android.graphics.Color

enum class RobotMood { HAPPY, THINKING, CURIOUS, EXCITED, IDLE, BUSY, ALERT }

data class AgentRobot(
    val name: String,
    val displayName: String,
    val emoji: String,
    val description: String,
    val color: Int,
    var currentStatus: String = "Idle",
    var currentTask: String = "",
    var mood: RobotMood = RobotMood.IDLE
)

interface Robot {
    val agentRobot: AgentRobot
    suspend fun performTask(task: String): String
    fun getStatus(): String
}

object RobotDefaults {
    val ARIA = AgentRobot(
        name = "ARIA",
        displayName = "ARIA",
        emoji = "🤖",
        description = "Main AI Companion",
        color = Color.parseColor("#2196F3")
    )
    val MAXIE = AgentRobot(
        name = "MAXIE",
        displayName = "MAXIE",
        emoji = "💬",
        description = "Messaging Agent",
        color = Color.parseColor("#4CAF50")
    )
    val FELIX = AgentRobot(
        name = "FELIX",
        displayName = "FELIX",
        emoji = "📁",
        description = "File Manager",
        color = Color.parseColor("#FF9800")
    )
    val NEXUS = AgentRobot(
        name = "NEXUS",
        displayName = "NEXUS",
        emoji = "🌐",
        description = "Web Explorer",
        color = Color.parseColor("#9C27B0")
    )
    val MEMO = AgentRobot(
        name = "MEMO",
        displayName = "MEMO",
        emoji = "🧠",
        description = "Memory Keeper",
        color = Color.parseColor("#FFC107")
    )
}
