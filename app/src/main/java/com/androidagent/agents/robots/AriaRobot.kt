package com.androidagent.agents.robots

import com.androidagent.agents.AgentRobot
import com.androidagent.agents.Robot
import com.androidagent.agents.RobotDefaults
import com.androidagent.agents.RobotMood

enum class AriaMood { HAPPY, THINKING, CURIOUS, EXCITED, IDLE }

class AriaRobot : Robot {

    override val agentRobot: AgentRobot = RobotDefaults.ARIA.copy()
    var mood: AriaMood = AriaMood.IDLE
        private set

    init {
        agentRobot.currentStatus = "Ready to chat! ✨"
    }

    override suspend fun performTask(task: String): String {
        setMood(AriaMood.THINKING)
        agentRobot.currentStatus = "Thinking... 🤔"
        agentRobot.currentTask = task
        // ARIA routes to LLM via AgentManager
        val result = "I'm on it! Let me help you with: $task"
        setMood(AriaMood.HAPPY)
        agentRobot.currentStatus = "Ready to chat! ✨"
        return result
    }

    override fun getStatus(): String = agentRobot.currentStatus

    fun setMood(newMood: AriaMood) {
        mood = newMood
        agentRobot.mood = when (newMood) {
            AriaMood.HAPPY -> RobotMood.HAPPY
            AriaMood.THINKING -> RobotMood.THINKING
            AriaMood.CURIOUS -> RobotMood.CURIOUS
            AriaMood.EXCITED -> RobotMood.EXCITED
            AriaMood.IDLE -> RobotMood.IDLE
        }
        agentRobot.currentStatus = when (newMood) {
            AriaMood.HAPPY -> "Happy to help! 😊"
            AriaMood.THINKING -> "Thinking... 🤔"
            AriaMood.CURIOUS -> "That's interesting! 🧐"
            AriaMood.EXCITED -> "I found something cool! 🎉"
            AriaMood.IDLE -> "Ready to chat! ✨"
        }
    }
}
