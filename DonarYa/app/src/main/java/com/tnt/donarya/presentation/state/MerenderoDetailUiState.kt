package com.tnt.donarya.presentation.state

import com.tnt.donarya.domain.model.MerenderoWithNeeds

sealed class MerenderoDetailUiState {

    object Loading : MerenderoDetailUiState()

    data class Success(
        val data: MerenderoWithNeeds
    ) : MerenderoDetailUiState()

    object NotFound : MerenderoDetailUiState()
}