package com.tnt.donarya.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnt.donarya.data.remote.dto.DonationHistoryDto
import com.tnt.donarya.data.remote.dto.NeedHistoryDto
import com.tnt.donarya.data.repository.FirebaseUserRepository
import com.tnt.donarya.data.repository.MerenderoRepositoryImpl
import com.tnt.donarya.data.repository.UserRepositoryImpl
import com.tnt.donarya.domain.model.Merendero
import com.tnt.donarya.domain.model.User
import com.tnt.donarya.domain.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileData(
    val user: User?,
    val merendero: Merendero?,
    val donationHistory: List<DonationHistoryDto> = emptyList(),
    val needsHistory: List<NeedHistoryDto> = emptyList()
)

class ProfileViewModel : ViewModel() {

    private val _profileData = MutableStateFlow(
        ProfileData(
            user = UserRepositoryImpl.getCurrentUser(),
            merendero = null
        )
    )
    val profileData: StateFlow<ProfileData> = _profileData

    init {
        loadProfile()
    }

    fun refresh() {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            MerenderoRepositoryImpl.invalidateCache()
            val user = UserRepositoryImpl.getCurrentUser()
            val merendero = user?.merenderoId?.let {
                MerenderoRepositoryImpl.getById(it)
            }

            val donationHistory = if (user?.rol != UserRole.MERENDERO)
                FirebaseUserRepository.getDonationHistory()
            else emptyList()

            val needsHistory = if (user?.rol == UserRole.MERENDERO)
                FirebaseUserRepository.getNeedsHistory()
            else emptyList()

            _profileData.value = ProfileData(
                user = user,
                merendero = merendero,
                donationHistory = donationHistory,
                needsHistory = needsHistory
            )
        }
    }
}