package com.tnt.donarya.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnt.donarya.data.remote.ApiClient
import com.tnt.donarya.data.remote.dto.UpdateNeedRequestDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.tnt.donarya.data.repository.MerenderoRepositoryImpl
import com.tnt.donarya.data.repository.NeedRepositoryImpl
import com.tnt.donarya.data.repository.UserRepositoryImpl
import com.tnt.donarya.domain.usecase.MarkNeedCoveredUseCase
import com.tnt.donarya.presentation.state.MerenderoHomeUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class MerenderoHomeViewModel : ViewModel() {

    private val merenderoRepository = MerenderoRepositoryImpl
    private val needRepository = NeedRepositoryImpl
    private val markNeedCoveredUseCase = MarkNeedCoveredUseCase(needRepository)

    private val _actionState = MutableStateFlow<ActionState>(ActionState.Idle)
    val actionState: StateFlow<ActionState> = _actionState


    private val _notificacion = MutableSharedFlow<String>()
    val notificacion: SharedFlow<String> = _notificacion


    private val donorsSnapshot = mutableMapOf<String, Int>()

    sealed class ActionState {
        object Idle    : ActionState()
        object Loading : ActionState()
        object Success : ActionState()
        data class Error(val message: String) : ActionState()
    }


    private val _uiState = MutableStateFlow<MerenderoHomeUiState>(
        MerenderoHomeUiState.Loading
    )
    val uiState: StateFlow<MerenderoHomeUiState> = _uiState

    init { cargar() }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val merenderoId = UserRepositoryImpl.getCurrentUser()?.merenderoId ?: return@launch
            NeedRepositoryImpl.invalidateCache()
            val needs = NeedRepositoryImpl.getByMerendero(merenderoId)

            // Detectar donantes nuevos
            needs.filter { !it.isCovered }.forEach { need ->
                val anterior = donorsSnapshot[need.id]
                if (anterior != null && need.donorsOnWay > anterior) {
                    val nuevos = need.donorsOnWay - anterior
                    val msg = if (nuevos == 1)
                        "🚶 Un donante nuevo va en camino para \"${need.title}\""
                    else
                        "🚶 $nuevos donantes nuevos para \"${need.title}\""
                    _notificacion.emit(msg)
                }
                donorsSnapshot[need.id] = need.donorsOnWay
            }

            actualizarEstado(merenderoId)
        }
    }

    private fun cargar() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentUser = UserRepositoryImpl.getCurrentUser()
            val merenderoId = currentUser?.merenderoId
            if (merenderoId != null) {
                actualizarEstado(merenderoId)
            } else {
                _uiState.value = MerenderoHomeUiState.Loading
            }
        }
    }

    fun marcarComoCubierta(needId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = markNeedCoveredUseCase(needId)
            if (result.isSuccess) {
                val currentUser = UserRepositoryImpl.getCurrentUser()
                val merenderoId = currentUser?.merenderoId ?: return@launch
                actualizarEstado(merenderoId)
            }
        }
    }

    private suspend fun actualizarEstado(merenderoId: String) {
        val merendero = merenderoRepository.getById(merenderoId) ?: return
        val needs = needRepository.getByMerendero(merenderoId)

        // Inicializar snapshot solo la primera carga
        needs.filter { !it.isCovered }.forEach { need ->
            if (!donorsSnapshot.containsKey(need.id)) {
                donorsSnapshot[need.id] = need.donorsOnWay
            }
        }

        _uiState.value = MerenderoHomeUiState.Success(
            merendero = merendero,
            activeNeeds = needs.filter { !it.isCovered },
            coveredNeeds = needs.filter { it.isCovered }
        )
    }

    fun eliminarNecesidad(needId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _actionState.value = ActionState.Loading
            val result = ApiClient.deleteNeed(needId)
            if (result.isSuccess) {
                _actionState.value = ActionState.Success
                val merenderoId = UserRepositoryImpl.getCurrentUser()?.merenderoId ?: return@launch
                NeedRepositoryImpl.invalidateCache()           // ← agregar esto
                NeedRepositoryImpl.refreshForMerendero(merenderoId)  // ← y esto
                actualizarEstado(merenderoId)
            } else {
                _actionState.value = ActionState.Error(
                    result.exceptionOrNull()?.message ?: "Error al eliminar"
                )
            }
        }
    }

}