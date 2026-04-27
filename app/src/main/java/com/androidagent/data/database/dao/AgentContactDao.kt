package com.androidagent.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.androidagent.data.database.entities.AgentContact

@Dao
interface AgentContactDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: AgentContact): Long

    @Update
    suspend fun update(contact: AgentContact)

    @Delete
    suspend fun delete(contact: AgentContact)

    @Query("SELECT * FROM agent_contacts ORDER BY name ASC")
    fun getAll(): LiveData<List<AgentContact>>

    @Query("SELECT * FROM agent_contacts ORDER BY name ASC")
    suspend fun getAllSync(): List<AgentContact>

    @Query("SELECT * FROM agent_contacts WHERE phoneNumber = :phone LIMIT 1")
    suspend fun getByPhoneNumber(phone: String): AgentContact?

    @Query("SELECT * FROM agent_contacts WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): AgentContact?

    @Query("SELECT * FROM agent_contacts WHERE isAutoReplyEnabled = 1")
    suspend fun getAutoReplyEnabled(): List<AgentContact>
}
