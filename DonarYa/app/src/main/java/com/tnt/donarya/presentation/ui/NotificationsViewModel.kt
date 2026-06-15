package com.tnt.donarya.presentation.ui

import androidx.lifecycle.ViewModel
import com.tnt.donarya.data.repository.NotificationRepositoryImpl
import com.tnt.donarya.domain.model.NotificationItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NotificationsUiState(
    val notifications: List<NotificationItem> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class NotificationsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    fun loadNotifications() {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        kotlinx.coroutines.runBlocking {
            val list = NotificationRepositoryImpl.getNotifications()
            val unread = list.count { !it.isRead }
            _uiState.value = _uiState.value.copy(
                notifications = list,
                unreadCount = unread,
                isLoading = false
            )
        }
    }

    fun markAsRead(notiId: String) {
        kotlinx.coroutines.runBlocking {
            NotificationRepositoryImpl.markAsRead(notiId)
            val list = NotificationRepositoryImpl.getNotifications()
            val unread = list.count { !it.isRead }
            _uiState.value = _uiState.value.copy(
                notifications = list,
                unreadCount = unread
            )
        }
    }

    fun refresh() {
        NotificationRepositoryImpl.invalidateCache()
        loadNotifications()
    }

    companion object {
        fun getUnreadCount(): Int {
            return kotlinx.coroutines.runBlocking {
                val list = NotificationRepositoryImpl.getNotifications()
                list.count { !it.isRead }
            }
        }
    }
}
