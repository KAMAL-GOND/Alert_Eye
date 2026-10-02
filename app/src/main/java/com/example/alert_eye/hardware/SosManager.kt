package com.example.alert_eye.hardware

import android.content.Context
import android.telephony.SmsManager
import android.util.Log
import com.example.alert_eye.data.repository.ContactRepository
import kotlinx.coroutines.flow.firstOrNull

class SosManager(
    private val context: Context,
    private val contactRepository: ContactRepository,
    private val locationTracker: LocationTracker
) {
    private val TAG = "SosManager"

    suspend fun sendSosMessage() {
        val contacts = contactRepository.contacts.firstOrNull() ?: emptyList()
        if (contacts.isEmpty()) {
            Log.w(TAG, "No emergency contacts found")
            return
        }

        val location = locationTracker.getCurrentLocation()
        val locationString = if (location != null) {
            "https://maps.google.com/?q=${location.latitude},${location.longitude}"
        } else {
            "Location unavailable"
        }

        val message = "Emergency! A potential accident was detected. Location: $locationString"

        val smsManager: SmsManager = context.getSystemService(SmsManager::class.java)

        contacts.forEach { contact ->
            try {
                smsManager.sendTextMessage(contact.phoneNumber, null, message, null, null)
                Log.d(TAG, "SOS sent to ${contact.phoneNumber}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send SOS to ${contact.phoneNumber}: ${e.message}")
            }
        }
    }
}
