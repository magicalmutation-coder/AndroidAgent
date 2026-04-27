package com.androidagent.data.repository

import com.androidagent.data.database.dao.MemoryDao
import com.androidagent.data.database.entities.Memory

class MemoryRepository(private val memoryDao: MemoryDao) {

    suspend fun storeMemory(key: String, value: String, category: String, importance: Int = 5): Long {
        val memory = Memory(
            key = key,
            value = value,
            category = category,
            importance = importance.coerceIn(1, 10)
        )
        return memoryDao.insert(memory)
    }

    suspend fun updateMemory(memory: Memory) = memoryDao.update(memory)

    suspend fun getAllMemories(): List<Memory> = memoryDao.getAll()

    suspend fun getMemoriesByCategory(category: String): List<Memory> =
        memoryDao.getByCategory(category)

    suspend fun searchMemories(key: String): List<Memory> = memoryDao.searchByKey(key)

    suspend fun deleteMemory(memory: Memory) = memoryDao.delete(memory)

    suspend fun clearAllMemories() = memoryDao.deleteAll()

    suspend fun buildContextSummary(): String {
        val memories = memoryDao.getAll().take(10)
        if (memories.isEmpty()) return ""
        return memories.joinToString("\n") { "- ${it.key}: ${it.value}" }
    }
}
