package com.androidagent.agents.robots

import com.androidagent.agents.AgentRobot
import com.androidagent.agents.Robot
import com.androidagent.agents.RobotDefaults
import com.androidagent.agents.RobotMood

class FelixRobot : Robot {

    override val agentRobot: AgentRobot = RobotDefaults.FELIX.copy()

    init {
        agentRobot.currentStatus = "Workspace ready 📁"
    }

    override suspend fun performTask(task: String): String {
        agentRobot.currentTask = task
        return when {
            task.startsWith("scan") -> {
                agentRobot.currentStatus = "Scanning files... 🔍"
                agentRobot.mood = RobotMood.BUSY
                "FELIX: Scanning workspace"
            }
            task.startsWith("save:") -> {
                agentRobot.currentStatus = "File saved! ✅"
                agentRobot.mood = RobotMood.HAPPY
                "FELIX: File saved!"
            }
            task.startsWith("delete:") -> {
                agentRobot.currentStatus = "File deleted 🗑️"
                "FELIX: File deleted"
            }
            else -> {
                agentRobot.currentStatus = "Working on files... 📂"
                "FELIX handling: $task"
            }
        }
    }

    override fun getStatus(): String = agentRobot.currentStatus

    fun onScanStart() {
        agentRobot.currentStatus = "Scanning files... 🔍"
        agentRobot.mood = RobotMood.BUSY
    }

    fun onScanComplete(count: Int) {
        agentRobot.currentStatus = "Found $count files 📋"
        agentRobot.mood = RobotMood.HAPPY
    }

    fun onFileSaved() {
        agentRobot.currentStatus = "File saved! ✅"
        agentRobot.mood = RobotMood.HAPPY
    }

    fun resetStatus() {
        agentRobot.currentStatus = "Workspace ready 📁"
        agentRobot.mood = RobotMood.IDLE
    }
}
