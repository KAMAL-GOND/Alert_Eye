package com.example.alert_eye.ui.settings

import androidx.lifecycle.ViewModel
import com.example.alert_eye.data.repository.AppSettings
import com.example.alert_eye.data.repository.AppSettingsRepository
import kotlinx.coroutines.flow.StateFlow

class SettingsViewModel(
    private val settingsRepository: AppSettingsRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings

    fun updateEarThreshold(value: Float) {
        settingsRepository.updateEarThreshold(value)
    }

    fun updateMarThreshold(value: Float) {
        settingsRepository.updateMarThreshold(value)
    }

    fun updateDistractionSensitivity(value: Float) {
        settingsRepository.updateDistractionSensitivity(value)
    }

    fun toggleSoundAlert(enabled: Boolean) {
        settingsRepository.updateSoundAlertEnabled(enabled)
    }

    fun toggleVibrationAlert(enabled: Boolean) {
        settingsRepository.updateVibrationAlertEnabled(enabled)
    }

    fun updateSosCountdownSeconds(seconds: Int) {
        settingsRepository.updateSosCountdownSeconds(seconds)
    }

    fun toggleImpactDetection(enabled: Boolean) {
        settingsRepository.updateImpactDetectionEnabled(enabled)
    }

    fun toggleDrowsiness(enabled: Boolean) {
        settingsRepository.updateDrowsinessEnabled(enabled)
    }

    fun toggleDistraction(enabled: Boolean) {
        settingsRepository.updateDistractionEnabled(enabled)
    }

    fun toggleYawning(enabled: Boolean) {
        settingsRepository.updateYawningEnabled(enabled)
    }

    fun togglePhoneDetection(enabled: Boolean) {
        settingsRepository.updatePhoneDetectionEnabled(enabled)
    }

    fun toggleDrinkingDetection(enabled: Boolean) {
        settingsRepository.updateDrinkingDetectionEnabled(enabled)
    }

    fun resetToDefaults() {
        settingsRepository.resetToDefaults()
    }
}
