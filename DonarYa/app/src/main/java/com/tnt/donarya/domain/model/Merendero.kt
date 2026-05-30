package com.tnt.donarya.domain.model

data class Merendero(
    val id: String,
    val name: String,
    val address: String,
    val neighborhood: String,
    val coordinator: String,
    val whatsapp: String,
    val kidsCount: Int,
    val activeNeeds: Int,
    val coveredNeeds: Int,
    val isVerified: Boolean = false,
    val distanceKm: Double = 0.0,
    val walkMinutes: Int = 0,
    val latitude: Double = -42.7692,
    val longitude: Double = -65.0375
)