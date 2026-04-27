package com.androidagent.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.androidagent.CompanionApplication
import com.androidagent.agents.AgentRobot
import com.androidagent.agents.RobotMood
import com.androidagent.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private val agentManager by lazy {
        (requireActivity().application as CompanionApplication).agentManager
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        agentManager.ariaStatus.observe(viewLifecycleOwner) { status ->
            binding.companionStatus.text = status
            binding.companionFace.setMood(agentManager.ariaRobot.agentRobot.mood)
        }

        agentManager.robots.observe(viewLifecycleOwner) { robots ->
            updateRobotCards(robots)
        }

        binding.fabVoice.setOnClickListener {
            agentManager.updateAriaStatus("Listening... 🎤")
        }

        agentManager.refreshRobotList()
    }

    private fun updateRobotCards(robots: List<AgentRobot>) {
        val robotsWithoutAria = robots.filter { it.name != "ARIA" }

        val cardBindings = listOf(
            binding.cardMaxie,
            binding.cardFelix,
            binding.cardNexus,
            binding.cardMemo
        )

        robotsWithoutAria.forEachIndexed { index, robot ->
            if (index < cardBindings.size) {
                val card = cardBindings[index]
                card.robotEmoji.text = robot.emoji
                card.robotName.text = robot.displayName
                card.robotStatus.text = robot.currentStatus
                val isBusy = robot.mood == RobotMood.BUSY || robot.mood == RobotMood.THINKING
                card.robotProgress.visibility = if (isBusy) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
