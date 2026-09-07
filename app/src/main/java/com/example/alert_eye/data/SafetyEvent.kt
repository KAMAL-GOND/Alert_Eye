package com.example.alert_eye.data

enum class EventType {
    DROWSINESS,
    DISTRACTION,
    PHONE_USE,
    DRINKING,
    YAWNING
}

enum class Severity {
    LOW,
    MEDIUM,
    HIGH,
    EMERGENCY
}

data class SafetyEvent(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: EventType,
    val timestamp: Long = System.currentTimeMillis(),
    val severity: Severity
)
