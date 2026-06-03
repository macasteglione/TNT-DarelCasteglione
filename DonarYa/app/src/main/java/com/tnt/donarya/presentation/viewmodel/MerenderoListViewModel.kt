package com.tnt.donarya.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.tnt.donarya.data.repository.MerenderoRepositoryImpl
import com.tnt.donarya.data.repository.NeedRepositoryImpl
import com.tnt.donarya.domain.model.MerenderoWithNeeds
import com.tnt.donarya.presentation.state.MerenderoListUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MerenderoListViewModel : ViewModel() {

    private val merenderoRepository = MerenderoRepositoryImpl
    private val needRepository = NeedRepositoryImpl

    private val _uiState =
        MutableStateFlow<MerenderoListUiState>(
            MerenderoListUiState.Loading
        )

    val uiState: StateFlow<MerenderoListUiState> =
        _uiState

    init {
        cargarMerenderos()
    }

    private fun cargarMerenderos() {

        val merenderos =
            merenderoRepository.getAll()

        val merenderosWithNeeds =
            merenderos.map { merendero ->

                MerenderoWithNeeds(
                    merendero = merendero,
                    needs = needRepository
                        .getByMerendero(merendero.id)
                )
            }

        _uiState.value =
            MerenderoListUiState.Success(
                merenderosWithNeeds
            )
    }
}