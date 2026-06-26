package com.tnt.donarya.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnt.donarya.data.repository.MerenderoRepositoryImpl
import com.tnt.donarya.data.repository.NeedRepositoryImpl
import com.tnt.donarya.data.repository.UserRepositoryImpl
import com.tnt.donarya.domain.model.Merendero
import com.tnt.donarya.domain.usecase.MarkNeedCoveredUseCase
import com.tnt.donarya.presentation.state.MerenderoHomeUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MerenderoHomeViewModel : ViewModel() {

    private val merenderoRepository = MerenderoRepositoryImpl
    private val needRepository = NeedRepositoryImpl
    private val markNeedCoveredUseCase = MarkNeedCoveredUseCase(needRepository)

    private val _actionState = MutableStateFlow<ActionState>(ActionState.Idle)


    private val _notificacion = MutableSharedFlow<String>()
    val notificacion: SharedFlow<String> = _notificacion


    private val donorsSnapshot = mutableMapOf<String, Int>()

    sealed class ActionState {
        object Idle : ActionState()
        object Loading : ActionState()
        object Success : ActionState()
        data class Error(val message: String) : ActionState()
    }


    private val _uiState = MutableStateFlow<MerenderoHomeUiState>(
        MerenderoHomeUiState.Loading
    )
    val uiState: StateFlow<MerenderoHomeUiState> = _uiState

    init {
        cargar()
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val merenderoId = UserRepositoryImpl.getCurrentUser()?.merenderoId ?: return@launch
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
            try {
                val currentUser = UserRepositoryImpl.getCurrentUser()
                val merenderoId = currentUser?.merenderoId
                if (merenderoId != null) {
                    actualizarEstado(merenderoId)
                } else {
                    _uiState.value = MerenderoHomeUiState.Loading
                }
            } catch (e: Exception) {
                _uiState.value = MerenderoHomeUiState.Error(
                    e.message ?: "Error al cargar datos"
                )
            }
        }
    }

    fun marcarComoCubierta(needId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _actionState.value = ActionState.Loading
            val result = markNeedCoveredUseCase(needId)
            if (result.isSuccess) {
                MerenderoRepositoryImpl.invalidateCache()
                val currentUser = UserRepositoryImpl.getCurrentUser()
                val merenderoId = currentUser?.merenderoId ?: return@launch
                actualizarEstado(merenderoId)
                _actionState.value = ActionState.Success
                _notificacion.emit("Necesidad marcada como cubierta")
            } else {
                val msg = result.exceptionOrNull()?.message ?: "Error al marcar como cubierta"
                _actionState.value = ActionState.Error(msg)
                _notificacion.emit("❌ $msg")
            }
        }
    }

    private suspend fun actualizarEstado(merenderoId: String) {
        try {
            var merendero = merenderoRepository.getById(merenderoId)
            if (merendero == null) {
                val user = UserRepositoryImpl.getCurrentUser()
                if (user != null) {
                    val newMerendero = Merendero(
                        id = merenderoId,
                        name = user.nombreComedor ?: user.nombre,
                        address = user.direccion ?: "",
                        neighborhood = "",
                        coordinator = user.nombre,
                        whatsapp = user.whatsapp ?: "",
                        kidsCount = 0,
                        activeNeeds = 0,
                        coveredNeeds = 0,
                        isVerified = false,
                        distanceKm = 0.0,
                        walkMinutes = 0,
                        latitude = 0.0,
                        longitude = 0.0
                    )
                    merenderoRepository.add(newMerendero)
                    merendero = newMerendero
                }
            }
            merendero = merendero ?: return
            val needs = needRepository.getByMerendero(merenderoId)

            needs.filter { !it.isCovered }.forEach { need ->
                if (!donorsSnapshot.containsKey(need.id)) {
                    donorsSnapshot[need.id] = need.donorsOnWay
                }
            }

            _uiState.value = MerenderoHomeUiState.Success(
                merendero = merendero,
                activeNeeds = needs.filter { !it.isCovered }.sortedBy { it.urgency.ordinal },
                coveredNeeds = needs.filter { it.isCovered }
            )
        } catch (e: Exception) {
            if (_uiState.value !is MerenderoHomeUiState.Success) {
                _uiState.value = MerenderoHomeUiState.Error(
                    e.message ?: "Error al cargar datos"
                )
            }
        }
    }

    fun eliminarNecesidad(needId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _actionState.value = ActionState.Loading
            val result = NeedRepositoryImpl.delete(needId)
            if (result.isSuccess) {
                _actionState.value = ActionState.Success
                val merenderoId = UserRepositoryImpl.getCurrentUser()?.merenderoId ?: return@launch
                actualizarEstado(merenderoId)
            } else {
                _actionState.value = ActionState.Error(
                    result.exceptionOrNull()?.message ?: "Error al eliminar"
                )
            }
        }
    }

}