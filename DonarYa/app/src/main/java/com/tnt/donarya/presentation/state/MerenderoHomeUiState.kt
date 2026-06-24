package com.tnt.donarya.presentation.state

import com.tnt.donarya.domain.model.Merendero
import com.tnt.donarya.domain.model.NeedItem

sealed class MerenderoHomeUiState {
    object Loading : MerenderoHomeUiState()
    data class Success(
        val merendero: Merendero,
        val activeNeeds: List<NeedItem>,
        val coveredNeeds: List<NeedItem>
    ) : MerenderoHomeUiState()

    data class Error(val message: String) : MerenderoHomeUiState()
}