package com.tnt.donarya.backend.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
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

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val token: String,
    val user: UserDto
)

@Serializable
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

@Serializable
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
    val distanceKm: Double,
    val walkMinutes: Int,
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class NeedItemDto(
    val id: String,
    val merenderoId: String,
    val title: String,
    val description: String,
    val type: String,
    val urgency: String,
    val items: List<String>,
    val publishedMinutesAgo: Int,
    val donorsOnWay: Int,
    val isCovered: Boolean
)

@Serializable
data class CreateNeedRequest(
    val title: String,
    val description: String,
    val type: String,
    val urgency: String,
    val items: List<String>
)

@Serializable
data class MerenderoWithNeedsDto(
    val merendero: MerenderoDto,
    val needs: List<NeedItemDto>
)

@Serializable
data class UpdateMerenderoRequest(
    val name: String? = null,
    val address: String? = null,
    val neighborhood: String? = null,
    val whatsapp: String? = null,
    val kidsCount: Int? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

@Serializable
data class ConfirmNeedResponse(
    val ok: Boolean,
    val donorsOnWay: Int
)

@Serializable
data class ConfirmationDto(
    val donorId: String,
    val donorNombre: String,
    val needId: String,
    val needTitle: String
)

@Serializable
data class UpdateNeedRequest(
    val title: String,
    val description: String,
    val type: String,
    val urgency: String,
    val items: List<String>
)
