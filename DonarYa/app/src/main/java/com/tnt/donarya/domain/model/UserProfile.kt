package com.tnt.donarya.domain.model

data class UserProfile(
    val name: String,
    val role: UserRole,
    val donationsCount: Int = 0,
    val mendecerosHelped: Int = 0,
    val beneficiados: Int = 0,
    val badges: List<Badge> = emptyList(),
    val recentDonations: List<DonationRecord> = emptyList()
)