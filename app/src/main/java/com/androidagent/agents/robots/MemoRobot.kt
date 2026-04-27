package com.androidagent.agents.robots

import com.androidagent.agents.AgentRobot
import com.androidagent.agents.Robot
import com.androidagent.agents.RobotDefaults
import com.androidagent.agents.RobotMood

class MemoRobot : Robot {

    override val agentRobot: AgentRobot = RobotDefaults.MEMO.copy()

    init {
        agentRobot.currentStatus = "Memory banks online 🧠"
    }

    override suspend fun performTask(task: String): String {
        agentRobot.currentTask = task
        return when {
            task.startsWith("store:") -> {
                agentRobot.currentStatus = "Storing memory... 💾"
                agentRobot.mood = RobotMood.BUSY
                "MEMO: Memory stored!"
            }
            task.startsWith("recall:") -> {
                agentRobot.currentStatus = "Recalling... 🔍"
                agentRobot.mood = RobotMood.THINKING
                "MEMO: Searching memory banks..."
            }
            task.startsWith("learn:") -> {
                agentRobot.currentStatus = "Learning from conversation... 📚"
                agentRobot.mood = RobotMood.CURIOUS
                "MEMO: Knowledge updated!"
            }
            else -> "MEMO: Memory banks ready!"
        }
    }

    override fun getStatus(): String = agentRobot.currentStatus

    fun onStoringMemory() {
        agentRobot.currentStatus = "Updating memory... 💾"
        agentRobot.mood = RobotMood.BUSY
    }

    fun onMemoryStored() {
        agentRobot.currentStatus = "Memory stored! ✅"
        agentRobot.mood = RobotMood.HAPPY
    }

    fun onLearning() {
        agentRobot.currentStatus = "Learning from conversation... 📚"
        agentRobot.mood = RobotMood.CURIOUS
    }

    fun resetStatus() {
        agentRobot.currentStatus = "Memory banks online 🧠"
        agentRobot.mood = RobotMood.IDLE
    }
}
