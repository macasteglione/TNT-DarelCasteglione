package com.tnt.donarya.presentation.state

import com.tnt.donarya.domain.model.MerenderoWithNeeds

sealed class MerenderoListUiState {

    object Loading : MerenderoListUiState()

    data class Success(
        val merenderos: List<MerenderoWithNeeds>
    ) : MerenderoListUiState()

    data class Error(
        val message: String
    ) : MerenderoListUiState()
}