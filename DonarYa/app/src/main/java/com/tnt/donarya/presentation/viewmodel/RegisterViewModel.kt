package com.tnt.donarya.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnt.donarya.data.remote.AddressRepository
import com.tnt.donarya.data.remote.dto.UpdateProfileRequestDto
import com.tnt.donarya.data.repository.UserRepositoryImpl
import com.tnt.donarya.domain.model.AddressSuggestion
import com.tnt.donarya.domain.model.UserRole
import com.tnt.donarya.domain.usecase.RegisterUseCase
import com.tnt.donarya.presentation.state.RegisterUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val registerUseCase = RegisterUseCase(UserRepositoryImpl)

    private val _uiState = MutableStateFlow<RegisterUiState>(RegisterUiState.Idle)
    val uiState: StateFlow<RegisterUiState> = _uiState

    private val _addressSuggestions = MutableStateFlow<List<AddressSuggestion>>(emptyList())
    val addressSuggestions: StateFlow<List<AddressSuggestion>> = _addressSuggestions

    private var selectedLat = 0.0
    private var selectedLng = 0.0
    private var addressSearchJob: Job? = null

    fun searchAddress(query: String) {
        addressSearchJob?.cancel()
        addressSearchJob = viewModelScope.launch {
            delay(500)
            val results = AddressRepository.search(query)
            _addressSuggestions.value = results
        }
    }

    fun selectAddress(suggestion: AddressSuggestion) {
        selectedLat = suggestion.lat
        selectedLng = suggestion.lon
        _addressSuggestions.value = emptyList()
    }

    fun clearAddressSuggestions() {
        _addressSuggestions.value = emptyList()
    }

    fun registrar(
        nombre: String,
        email: String,
        password: String,
        rol: UserRole,
        nombreComedor: String? = null,
        whatsapp: String? = null,
        direccion: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = RegisterUiState.Loading

            val result = registerUseCase(
                nombre, email, password, rol,
                nombreComedor, whatsapp, direccion,
                selectedLat, selectedLng
            )

            _uiState.value = if (result.isSuccess)
                RegisterUiState.Success(result.getOrThrow())
            else
                RegisterUiState.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
        }
    }



    fun actualizar(
        nombre: String,
        email: String,
        contrasenia: String,
        rol: UserRole,
        nombreComedor: String? = null,
        whatsapp: String? = null,
        direccion: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = RegisterUiState.Loading

            val result = UserRepositoryImpl.updateProfile(
                UpdateProfileRequestDto(
                    nombre = nombre,
                    email = email,
                    nombreComedor = nombreComedor?.ifBlank { null },
                    whatsapp = whatsapp?.ifBlank { null },
                    direccion = direccion?.ifBlank { null }
                )
            )

            _uiState.value = if (result.isSuccess)
                RegisterUiState.Success(result.getOrThrow())
            else
                RegisterUiState.Error(
                    result.exceptionOrNull()?.message ?: "Error al actualizar"
                )
        }
    }

    fun resetState() {
        _uiState.value = RegisterUiState.Idle
    }
}
