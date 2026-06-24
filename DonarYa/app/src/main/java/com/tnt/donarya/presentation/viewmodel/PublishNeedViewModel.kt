package com.tnt.donarya.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnt.donarya.data.remote.dto.UpdateNeedRequestDto
import com.tnt.donarya.data.repository.MerenderoRepositoryImpl
import com.tnt.donarya.data.repository.NeedRepositoryImpl
import com.tnt.donarya.data.repository.UserRepositoryImpl
import com.tnt.donarya.domain.model.NeedItem
import com.tnt.donarya.domain.model.NeedType
import com.tnt.donarya.domain.model.UrgencyLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PublishNeedUiState {
    object Idle : PublishNeedUiState()
    object Loading : PublishNeedUiState()
    object Success : PublishNeedUiState()
    data class Error(val message: String) : PublishNeedUiState()
}

class PublishNeedViewModel : ViewModel() {

    // WhatsApp del merendero para mostrar en el formulario
    private val _whatsapp = MutableStateFlow("")
    val whatsapp: StateFlow<String> = _whatsapp

    // Campos para precargar en edición
    private val _loadedNeed = MutableStateFlow<NeedItem?>(null)
    val loadedNeed: StateFlow<NeedItem?> = _loadedNeed

    private val _uiState = MutableStateFlow<PublishNeedUiState>(PublishNeedUiState.Idle)
    val uiState: StateFlow<PublishNeedUiState> = _uiState

    init {
        loadMerenderoData()
    }


    private fun loadMerenderoData() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = UserRepositoryImpl.getCurrentUser()
            val merenderoId = user?.merenderoId ?: return@launch
            val merendero = MerenderoRepositoryImpl.getById(merenderoId)
            _whatsapp.value = merendero?.whatsapp ?: ""
        }
    }

    fun publicar(
        merenderoId: String,
        type: NeedType,
        urgency: UrgencyLevel,
        description: String,
        items: List<String>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = PublishNeedUiState.Loading
            val need = NeedItem(
                id = System.currentTimeMillis().toString(),
                merenderoId = merenderoId,
                title = items.firstOrNull() ?: "Necesidad",
                description = description,
                type = type,
                urgency = urgency,
                items = items,
                publishedMinutesAgo = 0,
                donorsOnWay = 0,
                isCovered = false
            )
            NeedRepositoryImpl.add(merenderoId, need)
            _uiState.value = PublishNeedUiState.Success
        }
    }


    fun cargarNecesidad(needId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val need = NeedRepositoryImpl.getById(needId) ?: return@launch
            _loadedNeed.value = need
        }
    }

    fun actualizarNecesidad(
        needId: String,
        type: NeedType,
        urgency: UrgencyLevel,
        description: String,
        items: List<String>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = PublishNeedUiState.Loading
            val req = UpdateNeedRequestDto(
                title = items.firstOrNull() ?: "Necesidad",
                description = description,
                type = type.name,
                urgency = urgency.name,
                items = items
            )
            val result = NeedRepositoryImpl.update(needId, req)
            if (result.isSuccess) {
                _uiState.value = PublishNeedUiState.Success
            } else {
                _uiState.value = PublishNeedUiState.Error(
                    result.exceptionOrNull()?.message ?: "Error al actualizar"
                )
            }
        }
    }
}