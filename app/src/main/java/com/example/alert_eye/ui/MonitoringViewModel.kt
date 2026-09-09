package com.example.alert_eye.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alert_eye.data.EventType
import com.example.alert_eye.data.SafetyEvent
import com.example.alert_eye.data.Severity
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.example.alert_eye.hardware.ImpactDetector
import com.example.alert_eye.hardware.SosManager
import kotlinx.coroutines.delay

data class MonitoringUiState(
    val faceResult: FaceLandmarkerResult? = null,
    val imageWidth: Int = 1,
    val imageHeight: Int = 1,
    val isDrowsy: Boolean = false,
    val isYawning: Boolean = false,
    val isDistracted: Boolean = false,
    val isUsingPhone: Boolean = false,
    val isDrinking: Boolean = false,
    val isSosActive: Boolean = false,
    val sosCountdown: Int = 15
)

class MonitoringViewModel(
    private val safetyEngine: SafetyEngine,
    private val alertManager: AlertManager,
    private val impactDetector: ImpactDetector,
    private val sosManager: SosManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MonitoringUiState())
    val uiState: StateFlow<MonitoringUiState> = _uiState.asStateFlow()
    
    private var countdownJob: kotlinx.coroutines.Job? = null

    init {
        impactDetector.start {
            triggerSosCountdown()
        }
    }

    fun triggerSosCountdown() {
        if (_uiState.value.isSosActive) return
        _uiState.value = _uiState.value.copy(isSosActive = true, sosCountdown = 15)
        countdownJob = viewModelScope.launch {
            for (i in 15 downTo 1) {
                _uiState.value = _uiState.value.copy(sosCountdown = i)
                delay(1000)
            }
            sendSos()
            _uiState.value = _uiState.value.copy(isSosActive = false)
        }
    }

    fun cancelSos() {
        countdownJob?.cancel()
        _uiState.value = _uiState.value.copy(isSosActive = false)
    }

    private fun sendSos() {
        viewModelScope.launch {
            sosManager.sendSosMessage()
        }
    }

    fun processFaceResult(result: FaceLandmarkerResult, width: Int, height: Int) {
        viewModelScope.launch {
            safetyEngine.processFaceResult(result)
            
            _uiState.value = _uiState.value.copy(
                faceResult = result,
                imageWidth = width,
                imageHeight = height,
                isDrowsy = safetyEngine.isDrowsy,
                isYawning = safetyEngine.isYawning,
                isDistracted = safetyEngine.isDistracted
            )
            evaluateAlerts()
        }
    }

    fun processObjectResult(result: ObjectDetectorResult) {
        viewModelScope.launch {
            safetyEngine.processObjectResult(result)
            
            _uiState.value = _uiState.value.copy(
                isUsingPhone = safetyEngine.isUsingPhone,
                isDrinking = safetyEngine.isDrinking
            )
            evaluateAlerts()
        }
    }

    private var drowsyStartTime: Long = 0L

    private fun evaluateAlerts() {
        val state = _uiState.value
        
        if (state.isDrowsy) {
            if (drowsyStartTime == 0L) {
                drowsyStartTime = System.currentTimeMillis()
            } else if (System.currentTimeMillis() - drowsyStartTime >= 5000L) {
                triggerSosCountdown()
                drowsyStartTime = System.currentTimeMillis() // Reset timer
            }
        } else {
            drowsyStartTime = 0L
        }

        if (state.isDrowsy) {
            alertManager.triggerAlert(SafetyEvent(type = EventType.DROWSINESS, severity = Severity.HIGH))
        } else if (state.isDrinking || state.isUsingPhone) {
            val type = if (state.isDrinking) EventType.DRINKING else EventType.PHONE_USE
            alertManager.triggerAlert(SafetyEvent(type = type, severity = Severity.HIGH))
        } else if (state.isDistracted) {
            alertManager.triggerAlert(SafetyEvent(type = EventType.DISTRACTION, severity = Severity.MEDIUM))
        } else if (state.isYawning) {
            alertManager.triggerAlert(SafetyEvent(type = EventType.YAWNING, severity = Severity.LOW))
        } else {
            alertManager.stopAlert()
        }
    }

    override fun onCleared() {
        super.onCleared()
        alertManager.stopAlert()
        impactDetector.stop()
    }
}
