package com.tnt.donarya.data.repository

import com.tnt.donarya.data.remote.ApiClient
import com.tnt.donarya.data.remote.dto.NotificationDto
import com.tnt.donarya.domain.model.NotificationItem
import com.tnt.donarya.domain.repository.NotificationRepository

object NotificationRepositoryImpl : NotificationRepository {

    private var cached: List<NotificationItem>? = null

    override suspend fun getNotifications(): List<NotificationItem> {
        val result = ApiClient.getNotifications()
        if (result.isSuccess) {
            val list = result.getOrThrow().map { it.toDomain() }
            cached = list
            return list
        }
        return cached ?: emptyList()
    }

    override suspend fun markAsRead(notiId: String): Result<Unit> {
        val result = ApiClient.markNotificationRead(notiId)
        if (result.isSuccess) {
            cached = cached?.map { if (it.id == notiId) it.copy(isRead = true) else it }
        }
        return result
    }

    fun invalidateCache() {
        cached = null
    }
}

private fun NotificationDto.toDomain() = NotificationItem(
    id = id,
    userId = userId,
    type = type,
    message = message,
    relatedNeedId = relatedNeedId,
    relatedUserId = relatedUserId,
    isRead = isRead,
    createdAt = createdAt
)
