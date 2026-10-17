package com.example.alert_eye.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.alert_eye.data.local.SafetyEventDao
import com.example.alert_eye.data.local.SafetyEventEntity
import com.example.alert_eye.data.repository.SyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val safetyEventDao: SafetyEventDao,
    private val syncRepository: SyncRepository? = null
) : ViewModel() {

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    val events: StateFlow<List<SafetyEventEntity>> = safetyEventDao.getAllEvents()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        syncNow()
    }

    fun syncNow() {
        val repo = syncRepository ?: return
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                repo.syncPendingEvents()
            } finally {
                _isSyncing.value = false
            }
        }
    }
}
