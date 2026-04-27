package com.androidagent.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.androidagent.CompanionApplication
import com.androidagent.R
import com.androidagent.data.repository.MemoryRepository
import com.androidagent.databinding.FragmentSettingsBinding
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val memoryRepository by lazy {
        MemoryRepository(CompanionApplication.instance.database.memoryDao())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("agent_prefs", android.content.Context.MODE_PRIVATE)

        binding.btnLlmConfig.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_llmConfigFragment)
        }

        binding.btnManageContacts.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_contactsAutoReplyFragment)
        }

        binding.switchAutoReplyMaster.isChecked = prefs.getBoolean("auto_reply_master", false)
        binding.switchAutoReplyMaster.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("auto_reply_master", checked).apply()
        }

        binding.switchAutoReplySms.isChecked = prefs.getBoolean("auto_reply_sms", false)
        binding.switchAutoReplySms.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("auto_reply_sms", checked).apply()
        }

        binding.switchAutoReplyEmail.isChecked = prefs.getBoolean("auto_reply_email", false)
        binding.switchAutoReplyEmail.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("auto_reply_email", checked).apply()
        }

        binding.switchVoiceInput.isChecked = prefs.getBoolean("voice_input", true)
        binding.switchVoiceInput.setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("voice_input", checked).apply()
        }

        binding.btnClearMemories.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Clear All Memories")
                .setMessage("Are you sure? This cannot be undone.")
                .setPositiveButton("Clear") { _, _ ->
                    kotlinx.coroutines.GlobalScope.launch {
                        memoryRepository.clearAllMemories()
                    }
                    Toast.makeText(context, "Memories cleared", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        binding.tvAppVersion.text = "AndroidAgent v1.0\nRobots: ARIA 🤖 · MAXIE 💬 · FELIX 📁 · NEXUS 🌐 · MEMO 🧠"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
