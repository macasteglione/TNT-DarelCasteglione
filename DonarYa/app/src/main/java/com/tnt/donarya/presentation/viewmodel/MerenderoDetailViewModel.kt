package com.tnt.donarya.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.tnt.donarya.data.repository.MerenderoRepositoryImpl
import com.tnt.donarya.data.repository.NeedRepositoryImpl
import com.tnt.donarya.domain.model.MerenderoWithNeeds
import com.tnt.donarya.presentation.state.MerenderoDetailUiState
import com.tnt.donarya.presentation.state.MerenderoListUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MerenderoDetailViewModel : ViewModel() {
    private val merenderoRepository = MerenderoRepositoryImpl()
    private val needRepository = NeedRepositoryImpl()
    private val _uiState = MutableStateFlow<MerenderoDetailUiState>(MerenderoDetailUiState.Loading)
    val uiState: StateFlow<MerenderoDetailUiState> = _uiState

    fun loadMerendero(id: String) {

        val merendero =
            merenderoRepository.getById(id)

        if (merendero == null) {

            _uiState.value =
                MerenderoDetailUiState.NotFound

            return
        }

        val needs =
            needRepository.getByMerendero(id)

        _uiState.value =
            MerenderoDetailUiState.Success(
                MerenderoWithNeeds(
                    merendero = merendero,
                    needs = needs
                )
            )
    }
}