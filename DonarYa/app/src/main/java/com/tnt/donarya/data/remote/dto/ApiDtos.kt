package com.tnt.donarya.data.remote.dto

data class UpdateNeedRequestDto(
    val title: String,
    val description: String,
    val type: String,
    val urgency: String,
    val items: List<String>
)

data class UpdateProfileRequestDto(
    val nombre: String,
    val email: String,
    val nombreComedor: String? = null,
    val whatsapp: String? = null,
    val direccion: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class DonationHistoryDto(
    val needTitle: String,
    val needType: String,
    val merenderoName: String,
    val daysAgo: Int
)

data class NeedHistoryDto(
    val title: String,
    val type: String,
    val daysAgo: Int
)
