package com.tnt.donarya.presentation.state

import com.tnt.donarya.domain.model.AlertItem

sealed class AlertsUiState {
    object Loading : AlertsUiState()
    data class Success(val alerts: List<AlertItem>) : AlertsUiState()
}