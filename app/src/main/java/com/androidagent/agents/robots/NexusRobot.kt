package com.androidagent.agents.robots

import com.androidagent.agents.AgentRobot
import com.androidagent.agents.Robot
import com.androidagent.agents.RobotDefaults
import com.androidagent.agents.RobotMood
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class NexusRobot : Robot {

    override val agentRobot: AgentRobot = RobotDefaults.NEXUS.copy()

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    init {
        agentRobot.currentStatus = "Connected to the web 🌐"
    }

    override suspend fun performTask(task: String): String {
        agentRobot.currentTask = task
        return when {
            task.startsWith("search:") -> search(task.removePrefix("search:").trim())
            task.startsWith("fetch:") -> fetchUrl(task.removePrefix("fetch:").trim())
            else -> "NEXUS: Ready to browse the web for you!"
        }
    }

    private suspend fun search(query: String): String = withContext(Dispatchers.IO) {
        agentRobot.currentStatus = "Searching the web... 🔍"
        agentRobot.mood = RobotMood.BUSY
        try {
            val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
            val url = "https://duckduckgo.com/html/?q=$encodedQuery"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            agentRobot.currentStatus = "Found results! ✅"
            agentRobot.mood = RobotMood.HAPPY
            val snippet = body.take(500).replace(Regex("<[^>]*>"), "")
            "NEXUS found: ${snippet.trim()}"
        } catch (e: Exception) {
            agentRobot.currentStatus = "Search failed 😞"
            agentRobot.mood = RobotMood.IDLE
            "NEXUS: Couldn't reach the web. ${e.message}"
        }
    }

    private suspend fun fetchUrl(url: String): String = withContext(Dispatchers.IO) {
        agentRobot.currentStatus = "Downloading... ⬇️"
        agentRobot.mood = RobotMood.BUSY
        try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            agentRobot.currentStatus = "Done! ✅"
            agentRobot.mood = RobotMood.HAPPY
            body.take(2000).replace(Regex("<[^>]*>"), "")
        } catch (e: Exception) {
            agentRobot.currentStatus = "Fetch failed 😞"
            "NEXUS: Couldn't fetch URL. ${e.message}"
        }
    }

    override fun getStatus(): String = agentRobot.currentStatus

    fun resetStatus() {
        agentRobot.currentStatus = "Connected to the web 🌐"
        agentRobot.mood = RobotMood.IDLE
    }
}
