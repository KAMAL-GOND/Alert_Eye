package com.example.alert_eye.data.repository

import android.util.Log
import com.example.alert_eye.data.Contact
import com.example.alert_eye.data.local.ContactDao
import com.example.alert_eye.data.local.ContactEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ContactRepository(
    private val contactDao: ContactDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val TAG = "ContactRepository"

    val contacts: Flow<List<Contact>> = contactDao.getAllContacts().map { entities ->
        entities.map { Contact(it.id, it.name, it.phoneNumber, it.isSynced) }
    }

    suspend fun addContact(name: String, phoneNumber: String) {
        val id = java.util.UUID.randomUUID().toString()
        val entity = ContactEntity(id, name, phoneNumber, isSynced = false)
        contactDao.insertContact(entity)
        syncContacts()
    }

    suspend fun deleteContact(contact: Contact) {
        val entity = ContactEntity(contact.id, contact.name, contact.phoneNumber, contact.isSynced)
        contactDao.deleteContact(entity)
        val user = auth.currentUser
        if (user != null) {
            try {
                firestore.collection("users").document(user.uid)
                    .collection("contacts").document(contact.id).delete().await()
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting contact from Firestore: ${e.message}")
            }
        }
    }

    suspend fun syncContacts() {
        val user = auth.currentUser ?: return
        val unsynced = contactDao.getUnsyncedContacts()
        if (unsynced.isEmpty()) return

        val batch = firestore.batch()
        val contactsRef = firestore.collection("users").document(user.uid).collection("contacts")

        unsynced.forEach { contact ->
            val docRef = contactsRef.document(contact.id)
            batch.set(docRef, mapOf(
                "name" to contact.name,
                "phoneNumber" to contact.phoneNumber,
                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            ))
        }

        try {
            batch.commit().await()
            contactDao.markAsSynced(unsynced.map { it.id })
            Log.d(TAG, "Successfully synced ${unsynced.size} contacts")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync contacts: ${e.message}")
        }
    }
}
