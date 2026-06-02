package com.tnt.donarya.presentation.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.tnt.donarya.data.repository.UserRepositoryImpl
import com.tnt.donarya.domain.model.UserRole
import com.tnt.donarya.domain.usecase.RegisterUseCase
import com.tnt.donarya.presentation.state.RegisterUiState

class RegisterViewModel : ViewModel() {

    private val registerUseCase = RegisterUseCase(UserRepositoryImpl)

    private val _uiState = MutableStateFlow<RegisterUiState>(
        RegisterUiState.Idle
    )
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun registrar(
        nombre: String,
        email: String,
        password: String,
        rol: UserRole,
        nombreComedor: String? = null,
        whatsapp: String? = null
    ) {
        _uiState.value = RegisterUiState.Loading

        val result = registerUseCase(nombre, email, password, rol, nombreComedor, whatsapp)

        _uiState.value = if (result.isSuccess)
            RegisterUiState.Success(result.getOrThrow())
        else
            RegisterUiState.Error(result.exceptionOrNull()?.message ?: "Error desconocido")
    }

    fun resetState() {
        _uiState.value = RegisterUiState.Idle
    }
}