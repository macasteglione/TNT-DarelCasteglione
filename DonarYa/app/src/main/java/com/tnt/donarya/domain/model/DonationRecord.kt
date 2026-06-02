package com.tnt.donarya.domain.model

data class DonationRecord(
    val type: NeedType,
    val merenderoName: String,
    val daysAgo: Int,
    val delivered: Boolean
)
