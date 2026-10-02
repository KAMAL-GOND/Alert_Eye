package com.example.alert_eye.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM emergency_contacts")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM emergency_contacts WHERE isSynced = 0")
    suspend fun getUnsyncedContacts(): List<ContactEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    @Delete
    suspend fun deleteContact(contact: ContactEntity)

    @Update
    suspend fun updateContact(contact: ContactEntity)
    
    @Query("UPDATE emergency_contacts SET isSynced = 1 WHERE id IN (:contactIds)")
    suspend fun markAsSynced(contactIds: List<String>)
}
