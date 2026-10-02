package com.example.alert_eye.data

data class Contact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val isSynced: Boolean = false
)
