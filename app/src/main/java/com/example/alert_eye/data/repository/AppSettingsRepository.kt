package com.example.alert_eye.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppSettings(
    val earThreshold: Float = 0.20f,
    val marThreshold: Float = 0.50f,
    val distractionSensitivity: Float = 2.0f,
    val isSoundAlertEnabled: Boolean = true,
    val isVibrationAlertEnabled: Boolean = true,
    val sosCountdownSeconds: Int = 15,
    val isImpactDetectionEnabled: Boolean = true,
    val isDrowsinessEnabled: Boolean = true,
    val isDistractionEnabled: Boolean = true,
    val isYawningEnabled: Boolean = true,
    val isPhoneDetectionEnabled: Boolean = true,
    val isDrinkingDetectionEnabled: Boolean = true
)

class AppSettingsRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    val earThreshold: Float get() = _settings.value.earThreshold
    val marThreshold: Float get() = _settings.value.marThreshold
    val distractionSensitivity: Float get() = _settings.value.distractionSensitivity
    val isSoundAlertEnabled: Boolean get() = _settings.value.isSoundAlertEnabled
    val isVibrationAlertEnabled: Boolean get() = _settings.value.isVibrationAlertEnabled
    val sosCountdownSeconds: Int get() = _settings.value.sosCountdownSeconds
    val isImpactDetectionEnabled: Boolean get() = _settings.value.isImpactDetectionEnabled
    val isDrowsinessEnabled: Boolean get() = _settings.value.isDrowsinessEnabled
    val isDistractionEnabled: Boolean get() = _settings.value.isDistractionEnabled
    val isYawningEnabled: Boolean get() = _settings.value.isYawningEnabled
    val isPhoneDetectionEnabled: Boolean get() = _settings.value.isPhoneDetectionEnabled
    val isDrinkingDetectionEnabled: Boolean get() = _settings.value.isDrinkingDetectionEnabled

    private fun loadSettings(): AppSettings {
        return AppSettings(
            earThreshold = prefs.getFloat(KEY_EAR_THRESHOLD, 0.20f),
            marThreshold = prefs.getFloat(KEY_MAR_THRESHOLD, 0.50f),
            distractionSensitivity = prefs.getFloat(KEY_DISTRACTION_SENSITIVITY, 2.0f),
            isSoundAlertEnabled = prefs.getBoolean(KEY_SOUND_ENABLED, true),
            isVibrationAlertEnabled = prefs.getBoolean(KEY_VIBRATION_ENABLED, true),
            sosCountdownSeconds = prefs.getInt(KEY_SOS_COUNTDOWN, 15),
            isImpactDetectionEnabled = prefs.getBoolean(KEY_IMPACT_ENABLED, true),
            isDrowsinessEnabled = prefs.getBoolean(KEY_DROWSINESS_ENABLED, true),
            isDistractionEnabled = prefs.getBoolean(KEY_DISTRACTION_ENABLED, true),
            isYawningEnabled = prefs.getBoolean(KEY_YAWNING_ENABLED, true),
            isPhoneDetectionEnabled = prefs.getBoolean(KEY_PHONE_ENABLED, true),
            isDrinkingDetectionEnabled = prefs.getBoolean(KEY_DRINKING_ENABLED, true)
        )
    }

    fun updateEarThreshold(value: Float) {
        val rounded = (Math.round(value * 100) / 100f)
        prefs.edit().putFloat(KEY_EAR_THRESHOLD, rounded).apply()
        _settings.value = _settings.value.copy(earThreshold = rounded)
    }

    fun updateMarThreshold(value: Float) {
        val rounded = (Math.round(value * 100) / 100f)
        prefs.edit().putFloat(KEY_MAR_THRESHOLD, rounded).apply()
        _settings.value = _settings.value.copy(marThreshold = rounded)
    }

    fun updateDistractionSensitivity(value: Float) {
        val rounded = (Math.round(value * 10) / 10f)
        prefs.edit().putFloat(KEY_DISTRACTION_SENSITIVITY, rounded).apply()
        _settings.value = _settings.value.copy(distractionSensitivity = rounded)
    }

    fun updateSoundAlertEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(isSoundAlertEnabled = enabled)
    }

    fun updateVibrationAlertEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(isVibrationAlertEnabled = enabled)
    }

    fun updateSosCountdownSeconds(seconds: Int) {
        prefs.edit().putInt(KEY_SOS_COUNTDOWN, seconds).apply()
        _settings.value = _settings.value.copy(sosCountdownSeconds = seconds)
    }

    fun updateImpactDetectionEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_IMPACT_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(isImpactDetectionEnabled = enabled)
    }

    fun updateDrowsinessEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DROWSINESS_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(isDrowsinessEnabled = enabled)
    }

    fun updateDistractionEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DISTRACTION_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(isDistractionEnabled = enabled)
    }

    fun updateYawningEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_YAWNING_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(isYawningEnabled = enabled)
    }

    fun updatePhoneDetectionEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PHONE_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(isPhoneDetectionEnabled = enabled)
    }

    fun updateDrinkingDetectionEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DRINKING_ENABLED, enabled).apply()
        _settings.value = _settings.value.copy(isDrinkingDetectionEnabled = enabled)
    }

    fun resetToDefaults() {
        prefs.edit().clear().apply()
        _settings.value = AppSettings()
    }

    companion object {
        private const val PREFS_NAME = "alert_eye_settings"
        private const val KEY_EAR_THRESHOLD = "ear_threshold"
        private const val KEY_MAR_THRESHOLD = "mar_threshold"
        private const val KEY_DISTRACTION_SENSITIVITY = "distraction_sensitivity"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        private const val KEY_SOS_COUNTDOWN = "sos_countdown"
        private const val KEY_IMPACT_ENABLED = "impact_enabled"
        private const val KEY_DROWSINESS_ENABLED = "drowsiness_enabled"
        private const val KEY_DISTRACTION_ENABLED = "distraction_enabled"
        private const val KEY_YAWNING_ENABLED = "yawning_enabled"
        private const val KEY_PHONE_ENABLED = "phone_enabled"
        private const val KEY_DRINKING_ENABLED = "drinking_enabled"
    }
}
