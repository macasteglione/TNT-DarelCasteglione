package com.tnt.donarya.domain.model

data class AlertItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: AlertType,
    val minutesAgo: Int,
    val isRead: Boolean = false,
    val isUrgent: Boolean = false
)
