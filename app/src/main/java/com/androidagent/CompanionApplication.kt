package com.androidagent

import android.app.Application
import com.androidagent.agents.AgentManager
import com.androidagent.data.database.AppDatabase

class CompanionApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val agentManager: AgentManager by lazy { AgentManager.getInstance(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
        // Initialize agent manager eagerly so robots are ready
        agentManager.initialize()
    }

    companion object {
        lateinit var instance: CompanionApplication
            private set
    }
}
