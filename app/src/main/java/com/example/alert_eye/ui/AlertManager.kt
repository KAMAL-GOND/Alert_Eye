package com.example.alert_eye.ui

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.alert_eye.data.SafetyEvent
import com.example.alert_eye.data.Severity
import com.example.alert_eye.data.repository.AppSettingsRepository

class AlertManager(
    private val context: Context,
    private val settingsRepository: AppSettingsRepository? = null
) {

    private var currentRingtone: Ringtone? = null
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    fun triggerAlert(event: SafetyEvent) {
        val isSoundEnabled = settingsRepository?.isSoundAlertEnabled ?: true
        val isVibrationEnabled = settingsRepository?.isVibrationAlertEnabled ?: true

        if (currentRingtone?.isPlaying == true) {
            return // Already playing an alert
        }

        when (event.severity) {
            Severity.HIGH, Severity.EMERGENCY -> {
                if (isSoundEnabled) playAlarm()
                if (isVibrationEnabled) vibrate(longArrayOf(0, 500, 200, 500))
            }
            Severity.MEDIUM -> {
                if (isSoundEnabled) playNotification()
                if (isVibrationEnabled) vibrate(longArrayOf(0, 300))
            }
            Severity.LOW -> {
                // Just visual, handled by Compose UI
            }
        }
    }

    fun stopAlert() {
        if (currentRingtone?.isPlaying == true) {
            currentRingtone?.stop()
        }
        vibrator.cancel()
    }

    private fun playAlarm() {
        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) 
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            currentRingtone = RingtoneManager.getRingtone(context, alarmUri)
            currentRingtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun playNotification() {
        try {
            val notifUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            currentRingtone = RingtoneManager.getRingtone(context, notifUri)
            currentRingtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun vibrate(pattern: LongArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }
}
