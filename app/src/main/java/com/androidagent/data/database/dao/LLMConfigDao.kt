package com.androidagent.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.androidagent.data.database.entities.LLMConfig

@Dao
interface LLMConfigDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: LLMConfig): Long

    @Update
    suspend fun update(config: LLMConfig)

    @Delete
    suspend fun delete(config: LLMConfig)

    @Query("SELECT * FROM llm_configs ORDER BY name ASC")
    fun getAll(): LiveData<List<LLMConfig>>

    @Query("SELECT * FROM llm_configs ORDER BY name ASC")
    suspend fun getAllSync(): List<LLMConfig>

    @Query("SELECT * FROM llm_configs WHERE isActive = 1 LIMIT 1")
    suspend fun getActive(): LLMConfig?

    @Query("UPDATE llm_configs SET isActive = 0")
    suspend fun deactivateAll()

    @Query("UPDATE llm_configs SET isActive = 1 WHERE id = :id")
    suspend fun setActive(id: Long)

    @Query("SELECT COUNT(*) FROM llm_configs")
    suspend fun getCount(): Int
}
