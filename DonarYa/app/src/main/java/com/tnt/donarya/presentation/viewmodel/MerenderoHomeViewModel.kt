package com.tnt.donarya.presentation.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.tnt.donarya.data.repository.MerenderoRepositoryImpl
import com.tnt.donarya.data.repository.NeedRepositoryImpl
import com.tnt.donarya.data.repository.UserRepositoryImpl
import com.tnt.donarya.domain.usecase.MarkNeedCoveredUseCase
import com.tnt.donarya.presentation.state.MerenderoHomeUiState

class MerenderoHomeViewModel : ViewModel() {

    private val merenderoRepository = MerenderoRepositoryImpl
    private val needRepository = NeedRepositoryImpl
    private val markNeedCoveredUseCase = MarkNeedCoveredUseCase(needRepository)

    private val _uiState = MutableStateFlow<MerenderoHomeUiState>(
        MerenderoHomeUiState.Loading
    )
    val uiState: StateFlow<MerenderoHomeUiState> = _uiState

    init { cargar() }

    fun refresh() { cargar() }

    private fun cargar() {
        val currentUser = UserRepositoryImpl.getCurrentUser()
        val merenderoId = currentUser?.merenderoId
        if (merenderoId != null) {
            actualizarEstado(merenderoId)
        } else {
            _uiState.value = MerenderoHomeUiState.Loading
        }
    }

    fun marcarComoCubierta(needId: String) {
        val result = markNeedCoveredUseCase(needId)
        if (result.isSuccess) {
            val currentUser = UserRepositoryImpl.getCurrentUser()
            val merenderoId = currentUser?.merenderoId ?: return
            actualizarEstado(merenderoId)
        }
    }

    private fun actualizarEstado(merenderoId: String) {
        val merendero = merenderoRepository.getById(merenderoId) ?: return
        val needs = needRepository.getByMerendero(merenderoId)

        _uiState.value = MerenderoHomeUiState.Success(
            merendero = merendero,
            activeNeeds = needs.filter { !it.isCovered },
            coveredNeeds = needs.filter { it.isCovered }
        )
    }
}