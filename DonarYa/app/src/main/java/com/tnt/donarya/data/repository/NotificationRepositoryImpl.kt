package com.tnt.donarya.data.repository

import com.tnt.donarya.domain.model.NotificationItem
import com.tnt.donarya.domain.repository.NotificationRepository

object NotificationRepositoryImpl : NotificationRepository {

    override suspend fun getNotifications(): List<NotificationItem> {
        return FirebaseNotificationRepository.getNotifications()
    }

    override suspend fun markAsRead(notiId: String): Result<Unit> {
        return FirebaseNotificationRepository.markAsRead(notiId)
    }

    fun invalidateCache() {
        FirebaseNotificationRepository.invalidateCache()
    }
}
