package com.example.alert_eye.data.local

import androidx.room.TypeConverter
import com.example.alert_eye.data.EventType
import com.example.alert_eye.data.Severity

class Converters {
    @TypeConverter
    fun fromEventType(value: EventType): String {
        return value.name
    }

    @TypeConverter
    fun toEventType(value: String): EventType {
        return enumValueOf<EventType>(value)
    }

    @TypeConverter
    fun fromSeverity(value: Severity): String {
        return value.name
    }

    @TypeConverter
    fun toSeverity(value: String): Severity {
        return enumValueOf<Severity>(value)
    }
}
