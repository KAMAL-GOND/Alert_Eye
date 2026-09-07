package com.example.alert_eye

import com.example.alert_eye.ui.AlertManager
import com.example.alert_eye.ui.MonitoringViewModel
import com.example.alert_eye.ui.SafetyEngine
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { SafetyEngine() }
    single { AlertManager(androidContext()) }
    viewModel { MonitoringViewModel(get(), get()) }
}
