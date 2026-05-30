package com.tnt.donarya.domain.model

data class NeedItem(
    val id: String,
    val merenderoId: String,
    val title: String,
    val description: String,
    val type: NeedType,
    val urgency: UrgencyLevel,
    val items: List<String> = emptyList(),
    val publishedMinutesAgo: Int = 0,
    val donorsOnWay: Int = 0,
    val isCovered: Boolean = false
)