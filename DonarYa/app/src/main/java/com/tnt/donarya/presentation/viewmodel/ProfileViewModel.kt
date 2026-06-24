package com.tnt.donarya.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnt.donarya.data.remote.dto.DonationHistoryDto
import com.tnt.donarya.data.remote.dto.NeedHistoryDto
import com.tnt.donarya.data.repository.FirebaseUserRepository
import com.tnt.donarya.data.repository.MerenderoRepositoryImpl
import com.tnt.donarya.data.repository.UserRepositoryImpl
import com.tnt.donarya.domain.model.Badge
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

            val badges = if (user?.rol != UserRole.MERENDERO) {
                val count = donationHistory.size
                listOf(
                    Badge("Primera ayuda", "🌟", count >= 1),
                    Badge("Cinco ayudas", "🏅", count >= 5),
                    Badge("Diez ayudas", "🎖️", count >= 10),
                    Badge("Veinticinco ayudas", "🏆", count >= 25),
                    Badge("Cincuenta ayudas", "👑", count >= 50),
                    Badge("Cien ayudas", "💎", count >= 100),
                )
            } else {
                val needsCount = needsHistory.size
                val coveredCount = needsHistory.count { it.isCovered }
                val totalDonors = needsHistory.sumOf { it.donorsOnWay }
                val oldestDaysAgo = needsHistory.minOfOrNull { it.daysAgo } ?: 0
                listOf(
                    Badge("Primera necesidad", "📢", needsCount >= 1),
                    Badge("Cinco necesidades", "📦", needsCount >= 5),
                    Badge("Diez necesidades", "📦", needsCount >= 10),
                    Badge("Primera cubierta", "✅", coveredCount >= 1),
                    Badge("Cinco cubiertas", "✅", coveredCount >= 5),
                    Badge("Diez cubiertas", "✅", coveredCount >= 10),
                    Badge("Primer donante", "🚶", totalDonors >= 1),
                    Badge("Cinco donantes", "🤝", totalDonors >= 5),
                    Badge("Diez donantes", "🌟", totalDonors >= 10),
                    Badge("Una semana", "⏳", oldestDaysAgo >= 7),
                    Badge("Un mes", "📅", oldestDaysAgo >= 30),
                    Badge("Un año", "🎂", oldestDaysAgo >= 365),
                )
            }

            _profileData.value = ProfileData(
                user = user?.copy(badges = badges),
                merendero = merendero,
                donationHistory = donationHistory,
                needsHistory = needsHistory
            )
        }
    }
}