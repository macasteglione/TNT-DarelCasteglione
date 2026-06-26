package com.tnt.donarya.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnt.donarya.data.repository.FirebaseNeedRepository
import com.tnt.donarya.data.repository.MerenderoRepositoryImpl
import com.tnt.donarya.data.repository.NeedRepositoryImpl
import com.tnt.donarya.domain.model.MerenderoWithNeeds
import com.tnt.donarya.presentation.state.MerenderoDetailUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MerenderoDetailViewModel : ViewModel() {
    private val merenderoRepository = MerenderoRepositoryImpl
    private val needRepository = NeedRepositoryImpl
    private val firebaseNeedRepo = FirebaseNeedRepository()
    private val _uiState = MutableStateFlow<MerenderoDetailUiState>(MerenderoDetailUiState.Loading)
    val uiState: StateFlow<MerenderoDetailUiState> = _uiState

    private val _confirmState = MutableStateFlow<ConfirmState>(ConfirmState.Idle)
    val confirmState: StateFlow<ConfirmState> = _confirmState

    sealed class ConfirmState {
        object Idle : ConfirmState()
        object Loading : ConfirmState()
        object Success : ConfirmState()
        data class Error(val message: String) : ConfirmState()
    }

    fun loadNeed(needId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val need = needRepository.getById(needId)

            if (need == null) {
                _uiState.value = MerenderoDetailUiState.NotFound
                return@launch
            }

            MerenderoRepositoryImpl.invalidateCache()
            val merendero = merenderoRepository.getById(need.merenderoId)

            if (merendero == null) {
                _uiState.value = MerenderoDetailUiState.NotFound
                return@launch
            }

            _uiState.value = MerenderoDetailUiState.Success(
                MerenderoWithNeeds(
                    merendero = merendero,
                    needs = listOf(need)
                )
            )

            val yaConfirmo = firebaseNeedRepo.checkIfConfirmed(need.id)

            if (yaConfirmo) {
                _confirmState.value = ConfirmState.Success
            }
        }
    }

    fun confirmarDonacion(needId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _confirmState.value = ConfirmState.Loading
            val result = firebaseNeedRepo.confirmNeed(needId)
            _confirmState.value = if (result.isSuccess) {
                ConfirmState.Success
            } else {
                ConfirmState.Error(
                    result.exceptionOrNull()?.message ?: "Error al confirmar"
                )
            }
        }
    }
}
