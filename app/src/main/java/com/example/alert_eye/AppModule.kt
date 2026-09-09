package com.example.alert_eye

import androidx.room.Room
import com.example.alert_eye.data.local.AlertEyeDatabase
import com.example.alert_eye.data.repository.AuthRepository
import com.example.alert_eye.data.repository.SyncRepository
import com.example.alert_eye.ui.AlertManager
import com.example.alert_eye.ui.MonitoringViewModel
import com.example.alert_eye.ui.SafetyEngine
import com.example.alert_eye.ui.auth.AuthViewModel
import com.example.alert_eye.data.repository.ContactRepository
import com.example.alert_eye.hardware.ImpactDetector
import com.example.alert_eye.hardware.LocationTracker
import com.example.alert_eye.hardware.SosManager
import com.example.alert_eye.ui.contacts.ContactViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Core
    single { SafetyEngine() }
    single { AlertManager(androidContext()) }

    // Firebase
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }

    // Database
    single {
        Room.databaseBuilder(
            androidContext(),
            AlertEyeDatabase::class.java,
            "alerteye_db"
        ).fallbackToDestructiveMigration().build()
    }
    single { get<AlertEyeDatabase>().safetyEventDao() }
    single { get<AlertEyeDatabase>().contactDao() }

    // Hardware
    single { ImpactDetector(androidContext()) }
    single { LocationTracker(androidContext()) }
    single { SosManager(androidContext(), get(), get()) }

    // Repositories
    single { AuthRepository(get()) }
    single { SyncRepository(get(), get(), get()) }
    single { ContactRepository(get(), get(), get()) }

    // ViewModels
    viewModel { MonitoringViewModel(get(), get(), get(), get()) }
    viewModel { AuthViewModel(get()) }
    viewModel { ContactViewModel(get()) }
}
