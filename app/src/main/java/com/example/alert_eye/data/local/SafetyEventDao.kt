package com.example.alert_eye.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SafetyEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: SafetyEventEntity)

    @Query("SELECT * FROM safety_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<SafetyEventEntity>>

    @Query("SELECT * FROM safety_events WHERE isSynced = 0 ORDER BY timestamp ASC")
    suspend fun getUnsyncedEvents(): List<SafetyEventEntity>

    @Update
    suspend fun updateEvent(event: SafetyEventEntity)
    
    @Query("UPDATE safety_events SET isSynced = 1 WHERE id IN (:eventIds)")
    suspend fun markEventsAsSynced(eventIds: List<String>)
}
