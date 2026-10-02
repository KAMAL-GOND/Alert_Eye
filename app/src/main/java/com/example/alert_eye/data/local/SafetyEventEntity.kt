package com.example.alert_eye.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.alert_eye.data.EventType
import com.example.alert_eye.data.Severity

@Entity(tableName = "safety_events")
data class SafetyEventEntity(
    @PrimaryKey
    val id: String,
    val type: EventType,
    val timestamp: Long,
    val severity: Severity,
    val isSynced: Boolean = false
)
