package com.example.alert_eye.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [SafetyEventEntity::class, ContactEntity::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AlertEyeDatabase : RoomDatabase() {
    abstract fun safetyEventDao(): SafetyEventDao
    abstract fun contactDao(): ContactDao
}
