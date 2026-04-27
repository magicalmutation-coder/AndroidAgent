package com.androidagent.agents

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.androidagent.agents.robots.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AgentManager private constructor(private val context: Context) {

    val ariaRobot = AriaRobot()
    val maxieRobot = MaxieRobot()
    val felixRobot = FelixRobot()
    val nexusRobot = NexusRobot()
    val memoRobot = MemoRobot()

    private val _robots = MutableLiveData<List<AgentRobot>>()
    val robots: LiveData<List<AgentRobot>> = _robots

    private val _ariaStatus = MutableLiveData<String>()
    val ariaStatus: LiveData<String> = _ariaStatus

    private val _maxieStatus = MutableLiveData<String>()
    val maxieStatus: LiveData<String> = _maxieStatus

    private val _felixStatus = MutableLiveData<String>()
    val felixStatus: LiveData<String> = _felixStatus

    private val _nexusStatus = MutableLiveData<String>()
    val nexusStatus: LiveData<String> = _nexusStatus

    private val _memoStatus = MutableLiveData<String>()
    val memoStatus: LiveData<String> = _memoStatus

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun initialize() {
        refreshRobotList()
    }

    fun refreshRobotList() {
        _robots.value = listOf(
            ariaRobot.agentRobot,
            maxieRobot.agentRobot,
            felixRobot.agentRobot,
            nexusRobot.agentRobot,
            memoRobot.agentRobot
        )
        _ariaStatus.value = ariaRobot.getStatus()
        _maxieStatus.value = maxieRobot.getStatus()
        _felixStatus.value = felixRobot.getStatus()
        _nexusStatus.value = nexusRobot.getStatus()
        _memoStatus.value = memoRobot.getStatus()
    }

    fun triggerRobot(name: String, task: String) {
        scope.launch {
            val robot: Robot? = when (name.uppercase()) {
                "ARIA" -> ariaRobot
                "MAXIE" -> maxieRobot
                "FELIX" -> felixRobot
                "NEXUS" -> nexusRobot
                "MEMO" -> memoRobot
                else -> null
            }
            robot?.performTask(task)
            refreshRobotList()
        }
    }

    fun updateAriaStatus(status: String) {
        ariaRobot.agentRobot.currentStatus = status
        _ariaStatus.postValue(status)
        _robots.postValue(listOf(
            ariaRobot.agentRobot,
            maxieRobot.agentRobot,
            felixRobot.agentRobot,
            nexusRobot.agentRobot,
            memoRobot.agentRobot
        ))
    }

    fun updateMaxieStatus(status: String) {
        maxieRobot.agentRobot.currentStatus = status
        _maxieStatus.postValue(status)
    }

    fun updateFelixStatus(status: String) {
        felixRobot.agentRobot.currentStatus = status
        _felixStatus.postValue(status)
    }

    companion object {
        @Volatile
        private var INSTANCE: AgentManager? = null

        fun getInstance(context: Context): AgentManager {
            return INSTANCE ?: synchronized(this) {
                AgentManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
