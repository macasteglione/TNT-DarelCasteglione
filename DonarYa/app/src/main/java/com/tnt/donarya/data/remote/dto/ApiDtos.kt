package com.tnt.donarya.data.remote.dto

data class RegisterRequestDto(
    val nombre: String,
    val email: String,
    val password: String,
    val rol: String,
    val nombreComedor: String? = null,
    val whatsapp: String? = null,
    val direccion: String? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class AuthResponseDto(
    val token: String,
    val user: UserDto
)

data class UserDto(
    val id: String,
    val nombre: String,
    val email: String,
    val rol: String,
    val nombreComedor: String? = null,
    val whatsapp: String? = null,
    val direccion: String? = null,
    val merenderoId: String? = null,
    val donationsCount: Int = 0,
    val mendecerosHelped: Int = 0,
    val beneficiados: Int = 0
)

data class MerenderoDto(
    val id: String,
    val name: String,
    val address: String,
    val neighborhood: String,
    val coordinator: String,
    val whatsapp: String,
    val kidsCount: Int,
    val activeNeeds: Int,
    val coveredNeeds: Int,
    val isVerified: Boolean,
    val distanceKm: Double = 0.0,
    val walkMinutes: Int = 0,
    val latitude: Double,
    val longitude: Double
)

data class NeedItemDto(
    val id: String,
    val merenderoId: String,
    val title: String,
    val description: String,
    val type: String,
    val urgency: String,
    val items: List<String>,
    val publishedMinutesAgo: Int = 0,
    val donorsOnWay: Int = 0,
    val isCovered: Boolean = false
)

data class CreateNeedRequestDto(
    val title: String,
    val description: String,
    val type: String,
    val urgency: String,
    val items: List<String>
)

data class MerenderoWithNeedsDto(
    val merendero: MerenderoDto,
    val needs: List<NeedItemDto>
)

data class UpdateMerenderoRequestDto(
    val name: String? = null,
    val address: String? = null,
    val neighborhood: String? = null,
    val whatsapp: String? = null,
    val kidsCount: Int? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class ConfirmNeedResponseDto(
    val ok: Boolean,
    val donorsOnWay: Int
)

data class UpdateNeedRequestDto(
    val title: String,
    val description: String,
    val type: String,
    val urgency: String,
    val items: List<String>
)
