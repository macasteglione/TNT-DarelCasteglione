package com.tnt.donarya.domain.model

data class NotificationItem(
    val id: String,
    val userId: String,
    val type: String,
    val message: String,
    val relatedNeedId: String? = null,
    val relatedUserId: String? = null,
    val isRead: Boolean = false,
    val createdAt: String
)
