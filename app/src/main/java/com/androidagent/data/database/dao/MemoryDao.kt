package com.androidagent.data.database.dao

import androidx.room.*
import com.androidagent.data.database.entities.Memory

@Dao
interface MemoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(memory: Memory): Long

    @Update
    suspend fun update(memory: Memory)

    @Delete
    suspend fun delete(memory: Memory)

    @Query("SELECT * FROM memories ORDER BY importance DESC, lastAccessed DESC")
    suspend fun getAll(): List<Memory>

    @Query("SELECT * FROM memories WHERE category = :category ORDER BY importance DESC")
    suspend fun getByCategory(category: String): List<Memory>

    @Query("SELECT * FROM memories WHERE key LIKE '%' || :key || '%'")
    suspend fun searchByKey(key: String): List<Memory>

    @Query("DELETE FROM memories")
    suspend fun deleteAll()

    @Query("UPDATE memories SET lastAccessed = :timestamp WHERE id = :id")
    suspend fun updateLastAccessed(id: Long, timestamp: Long)
}
