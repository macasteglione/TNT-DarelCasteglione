package com.tnt.donarya.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.tnt.donarya.data.repository.AlertRepositoryImpl
import com.tnt.donarya.presentation.state.AlertsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AlertsViewModel : ViewModel() {

    private val repository = AlertRepositoryImpl()

    private val _uiState =
        MutableStateFlow<AlertsUiState>(AlertsUiState.Loading)

    val uiState: StateFlow<AlertsUiState> = _uiState

    init {
        loadAlerts()
    }

    private fun loadAlerts() {
        val alerts = repository.getAll()

        _uiState.value =
            AlertsUiState.Success(alerts)
    }
}