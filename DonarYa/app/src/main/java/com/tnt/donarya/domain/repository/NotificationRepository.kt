package com.tnt.donarya.domain.repository

import com.tnt.donarya.domain.model.NotificationItem

interface NotificationRepository {
    suspend fun getNotifications(): List<NotificationItem>
    suspend fun markAsRead(notiId: String): Result<Unit>
}
