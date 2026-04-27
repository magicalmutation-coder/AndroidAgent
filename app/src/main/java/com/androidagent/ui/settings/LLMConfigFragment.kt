package com.androidagent.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.androidagent.CompanionApplication
import com.androidagent.data.database.entities.LLMConfig
import com.androidagent.databinding.FragmentLlmConfigBinding
import com.androidagent.llm.LLMManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LLMConfigFragment : Fragment() {

    private var _binding: FragmentLlmConfigBinding? = null
    private val binding get() = _binding!!
    private val db by lazy { CompanionApplication.instance.database }
    private val llmManager by lazy { LLMManager.getInstance(CompanionApplication.instance) }
    private var allConfigs = mutableListOf<LLMConfig>()
    private var selectedConfig: LLMConfig? = null

    private val providers = listOf(
        "Ollama", "LM Studio", "LLaMA.cpp Server", "Jan.ai", "GPT4All", "Custom OpenAI-Compatible"
    )
    private val providerUrls = mapOf(
        "Ollama" to "http://localhost:11434",
        "LM Studio" to "http://localhost:1234",
        "LLaMA.cpp Server" to "http://localhost:8080",
        "Jan.ai" to "http://localhost:1337",
        "GPT4All" to "http://localhost:4891",
        "Custom OpenAI-Compatible" to ""
    )
    private val providerKeys = mapOf(
        "Ollama" to "ollama",
        "LM Studio" to "openai_compatible",
        "LLaMA.cpp Server" to "openai_compatible",
        "Jan.ai" to "openai_compatible",
        "GPT4All" to "openai_compatible",
        "Custom OpenAI-Compatible" to "openai_compatible"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLlmConfigBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupProviderSpinner()
        loadConfigs()

        binding.btnFetchModels.setOnClickListener { fetchModels() }
        binding.btnSave.setOnClickListener { saveConfig() }
        binding.btnDelete.setOnClickListener { deleteConfig() }
        binding.btnSetActive.setOnClickListener { setActive() }
        binding.btnTestConnection.setOnClickListener { testConnection() }
        binding.temperatureSlider.addOnChangeListener { _, value, _ ->
            binding.tvTemperatureValue.text = String.format("%.1f", value)
        }
    }

    private fun setupProviderSpinner() {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, providers)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.providerSpinner.adapter = adapter
        binding.providerSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, v: View?, pos: Int, id: Long) {
                val providerName = providers[pos]
                val url = providerUrls[providerName] ?: ""
                if (binding.baseUrlInput.text.isNullOrBlank()) {
                    binding.baseUrlInput.setText(url)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun loadConfigs() {
        lifecycleScope.launch {
            val configs = withContext(Dispatchers.IO) { db.llmConfigDao().getAllSync() }
            allConfigs = configs.toMutableList()
            updateConfigSpinner()
        }
    }

    private fun updateConfigSpinner() {
        val names = mutableListOf("-- New Config --") + allConfigs.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.configSpinner.adapter = adapter
        binding.configSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, v: View?, pos: Int, id: Long) {
                if (pos == 0) {
                    selectedConfig = null
                    clearForm()
                } else {
                    selectedConfig = allConfigs[pos - 1]
                    populateForm(selectedConfig!!)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun clearForm() {
        binding.configNameInput.setText("")
        binding.baseUrlInput.setText("")
        binding.modelNameInput.setText("")
        binding.apiKeyInput.setText("")
        binding.maxTokensInput.setText("2048")
        binding.systemPromptInput.setText(LLMConfig.DEFAULT_SYSTEM_PROMPT)
        binding.temperatureSlider.value = 0.7f
        binding.tvTemperatureValue.text = "0.7"
    }

    private fun populateForm(config: LLMConfig) {
        binding.configNameInput.setText(config.name)
        binding.baseUrlInput.setText(config.baseUrl)
        binding.modelNameInput.setText(config.modelName)
        binding.apiKeyInput.setText(config.apiKey)
        binding.maxTokensInput.setText(config.maxTokens.toString())
        binding.systemPromptInput.setText(config.systemPrompt)
        binding.temperatureSlider.value = config.temperature.coerceIn(0f, 2f)
        binding.tvTemperatureValue.text = String.format("%.1f", config.temperature)

        val providerDisplayName = providerKeys.entries
            .firstOrNull { it.value == config.provider }?.key ?: "Ollama"
        val idx = providers.indexOf(providerDisplayName).coerceAtLeast(0)
        binding.providerSpinner.setSelection(idx)
    }

    private fun fetchModels() {
        lifecycleScope.launch {
            binding.btnFetchModels.isEnabled = false
            val models = llmManager.listModels()
            binding.btnFetchModels.isEnabled = true
            if (models.isEmpty()) {
                Toast.makeText(context, "No models found or connection failed", Toast.LENGTH_LONG).show()
            } else {
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, models)
                binding.modelNameInput.setAdapter(adapter)
                binding.modelNameInput.showDropDown()
                Toast.makeText(context, "Found ${models.size} models", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveConfig() {
        val name = binding.configNameInput.text.toString().trim()
        val baseUrl = binding.baseUrlInput.text.toString().trim()
        val model = binding.modelNameInput.text.toString().trim()

        if (name.isBlank() || baseUrl.isBlank() || model.isBlank()) {
            Toast.makeText(context, "Name, URL, and model are required", Toast.LENGTH_SHORT).show()
            return
        }

        val providerName = providers[binding.providerSpinner.selectedItemPosition]
        val providerKey = providerKeys[providerName] ?: "openai_compatible"
        val maxTokens = binding.maxTokensInput.text.toString().toIntOrNull() ?: 2048

        val config = (selectedConfig ?: LLMConfig(
            name = name, provider = providerKey, baseUrl = baseUrl, modelName = model
        )).copy(
            name = name,
            provider = providerKey,
            baseUrl = baseUrl,
            modelName = model,
            apiKey = binding.apiKeyInput.text.toString().trim(),
            temperature = binding.temperatureSlider.value,
            maxTokens = maxTokens,
            systemPrompt = binding.systemPromptInput.text.toString().trim()
        )

        lifecycleScope.launch {
            withContext(Dispatchers.IO) { db.llmConfigDao().insert(config) }
            Toast.makeText(context, "Config saved!", Toast.LENGTH_SHORT).show()
            loadConfigs()
        }
    }

    private fun deleteConfig() {
        val config = selectedConfig ?: return
        lifecycleScope.launch {
            withContext(Dispatchers.IO) { db.llmConfigDao().delete(config) }
            selectedConfig = null
            Toast.makeText(context, "Config deleted", Toast.LENGTH_SHORT).show()
            loadConfigs()
        }
    }

    private fun setActive() {
        val config = selectedConfig ?: run {
            Toast.makeText(context, "Save config first", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                db.llmConfigDao().deactivateAll()
                db.llmConfigDao().setActive(config.id)
            }
            Toast.makeText(context, "${config.name} is now active!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun testConnection() {
        lifecycleScope.launch {
            binding.btnTestConnection.isEnabled = false
            val result = llmManager.sendRawMessage("Say 'Hello, I am connected!' in exactly those words.")
            binding.btnTestConnection.isEnabled = true
            val msg = if (result.isBlank()) "Connection failed or no response" else "✅ Connected! Response: $result"
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
