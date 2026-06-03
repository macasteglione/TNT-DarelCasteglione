package com.tnt.donarya.domain.model

data class User(
    val id: String,
    val nombre: String,
    val email: String,
    val password: String,
    val rol: UserRole,
    // Campos merendero
    val nombreComedor: String? = null,
    val direccion: String? = null,
    val whatsapp: String? = null,
    val cantidadChicos: Int? = null,
    val merenderoId: String? = null,
    // Campos impacto donante
    val donationsCount: Int = 0,
    val mendecerosHelped: Int = 0,
    val beneficiados: Int = 0,
    val badges: List<Badge> = emptyList(),
    val recentDonations: List<DonationRecord> = emptyList()
)