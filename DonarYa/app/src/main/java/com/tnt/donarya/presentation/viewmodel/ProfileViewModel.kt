package com.tnt.donarya.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnt.donarya.data.repository.MerenderoRepositoryImpl
import com.tnt.donarya.data.repository.UserRepositoryImpl
import com.tnt.donarya.domain.model.Merendero
import com.tnt.donarya.domain.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileData(
    val user: User?,
    val merendero: Merendero?
)

class ProfileViewModel : ViewModel() {

    private val _profileData = MutableStateFlow(ProfileData(null, null))
    val profileData: StateFlow<ProfileData> = _profileData

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = UserRepositoryImpl.getCurrentUser()
            val merendero = user?.merenderoId?.let {
                MerenderoRepositoryImpl.getById(it)
            }
            _profileData.value = ProfileData(user = user, merendero = merendero)
        }
    }
}