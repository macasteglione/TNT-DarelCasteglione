package com.tnt.donarya.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnt.donarya.data.repository.UserRepositoryImpl
import com.tnt.donarya.presentation.state.LoginUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    private val repository = UserRepositoryImpl

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = LoginUiState.Error("Completá todos los campos")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = LoginUiState.Loading
            val result = repository.login(email, password)
            _uiState.value = if (result.isSuccess) LoginUiState.Success(result.getOrThrow())
            else LoginUiState.Error("Usuario o contraseña incorrectos")
        }
    }
}