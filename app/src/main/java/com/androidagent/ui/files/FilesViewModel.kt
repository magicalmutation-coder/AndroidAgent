package com.androidagent.ui.files

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidagent.CompanionApplication
import com.androidagent.agents.AgentManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class FilesViewModel : ViewModel() {

    private val context = CompanionApplication.instance
    private val agentManager = AgentManager.getInstance(context)

    private val workspaceDir: File by lazy {
        File(context.getExternalFilesDir(null), "workspace").also { it.mkdirs() }
    }

    private val _files = MutableLiveData<List<File>>()
    val files: LiveData<List<File>> = _files

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _fileContent = MutableLiveData<String?>()
    val fileContent: LiveData<String?> = _fileContent

    init {
        refreshFiles()
    }

    fun refreshFiles() {
        viewModelScope.launch {
            val fileList = withContext(Dispatchers.IO) {
                workspaceDir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
            }
            _files.value = fileList
            agentManager.updateFelixStatus("Found ${fileList.size} files 📋")
        }
    }

    fun createFile(name: String, content: String = "") {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val safeName = name.replace(Regex("[^a-zA-Z0-9._-]"), "_")
                val file = File(workspaceDir, safeName)
                file.writeText(content)
            }
            agentManager.updateFelixStatus("File saved! ✅")
            refreshFiles()
        }
    }

    fun deleteFile(file: File) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { file.delete() }
            agentManager.updateFelixStatus("File deleted 🗑️")
            refreshFiles()
        }
    }

    fun readFile(file: File) {
        viewModelScope.launch {
            val content = withContext(Dispatchers.IO) {
                runCatching { file.readText() }.getOrElse { "Error reading file: ${it.message}" }
            }
            _fileContent.value = content
        }
    }

    fun clearFileContent() {
        _fileContent.value = null
    }
}
