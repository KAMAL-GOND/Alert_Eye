package com.example.alert_eye.data.repository

import android.util.Log
import com.example.alert_eye.data.local.SafetyEventDao
import com.example.alert_eye.data.local.SafetyEventEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout

class SyncRepository(
    private val safetyEventDao: SafetyEventDao,
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository
) {
    suspend fun saveEvent(event: SafetyEventEntity) {
        // Save to Room DB first
        safetyEventDao.insertEvent(event)
        
        // Try syncing immediately
        syncPendingEvents()
    }

    suspend fun syncPendingEvents(): Boolean {
        val user = authRepository.currentUser.value
        if (user == null) {
            Log.d("SyncRepository", "User not logged in, skipping sync")
            return false
        }

        return try {
            val unsyncedEvents = safetyEventDao.getUnsyncedEvents()
            if (unsyncedEvents.isEmpty()) return true

            val batch = firestore.batch()
            val syncedIds = mutableListOf<String>()

            for (event in unsyncedEvents) {
                val docRef = firestore.collection("users").document(user.uid)
                    .collection("safety_events").document(event.id)
                
                val firestoreEvent = mapOf(
                    "type" to event.type.name,
                    "timestamp" to event.timestamp,
                    "severity" to event.severity.name
                )
                
                batch.set(docRef, firestoreEvent)
                syncedIds.add(event.id)
            }

            // Set a 6-second timeout so it never hangs in an infinite loading state
            withTimeout(6000L) {
                batch.commit().await()
            }
            safetyEventDao.markEventsAsSynced(syncedIds)
            Log.d("SyncRepository", "Successfully synced ${syncedIds.size} events")
            true
        } catch (e: Exception) {
            Log.e("SyncRepository", "Failed to sync events: ${e.message}", e)
            false
        }
    }
}
