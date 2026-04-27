package com.androidagent.agents.robots

import com.androidagent.agents.AgentRobot
import com.androidagent.agents.Robot
import com.androidagent.agents.RobotDefaults
import com.androidagent.agents.RobotMood

class MaxieRobot : Robot {

    override val agentRobot: AgentRobot = RobotDefaults.MAXIE.copy()

    init {
        agentRobot.currentStatus = "Monitoring messages 📡"
    }

    override suspend fun performTask(task: String): String {
        agentRobot.currentTask = task
        return when {
            task.startsWith("sms:") -> handleSms(task.removePrefix("sms:"))
            task.startsWith("email:") -> handleEmail(task.removePrefix("email:"))
            task.startsWith("reply:") -> handleReply(task.removePrefix("reply:"))
            else -> {
                agentRobot.currentStatus = "Processing message... 💬"
                "MAXIE is handling: $task"
            }
        }
    }

    private fun handleSms(content: String): String {
        agentRobot.currentStatus = "New SMS received! 📱"
        agentRobot.mood = RobotMood.ALERT
        return "New SMS from $content"
    }

    private fun handleEmail(content: String): String {
        agentRobot.currentStatus = "Email received! 📧"
        agentRobot.mood = RobotMood.ALERT
        return "Email from $content"
    }

    private fun handleReply(content: String): String {
        agentRobot.currentStatus = "Replying... ✍️"
        agentRobot.mood = RobotMood.BUSY
        return "Replying to $content"
    }

    override fun getStatus(): String = agentRobot.currentStatus

    fun onSmsReceived(sender: String, message: String) {
        agentRobot.currentStatus = "New SMS from $sender! 📱"
        agentRobot.mood = RobotMood.ALERT
    }

    fun onEmailReceived(sender: String) {
        agentRobot.currentStatus = "Email from $sender received 📧"
        agentRobot.mood = RobotMood.ALERT
    }

    fun onReplying(name: String) {
        agentRobot.currentStatus = "Replying to $name... ✍️"
        agentRobot.mood = RobotMood.BUSY
    }

    fun resetStatus() {
        agentRobot.currentStatus = "Monitoring messages 📡"
        agentRobot.mood = RobotMood.IDLE
    }
}
