package com.tnt.donarya.presentation.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import com.tnt.donarya.data.NotificationHelper
import com.tnt.donarya.data.repository.NotificationRepositoryImpl
import com.tnt.donarya.domain.model.NotificationItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    private var seenIds = mutableSetOf<String>()

    fun loadNotifications(context: Context? = null) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        kotlinx.coroutines.runBlocking {
            val list = NotificationRepositoryImpl.getNotifications()
            val unread = list.count { !it.isRead }

            if (context != null) {
                NotificationHelper.init(context)
                val newOnes = list.filter { it.id !in seenIds && !it.isRead }
                if (newOnes.isNotEmpty()) {
                    val grouped = newOnes.map {
                        Triple(it.id, "DonarYa", it.message)
                    }
                    NotificationHelper.showMultiple(context, grouped)
                    seenIds.addAll(newOnes.map { it.id })
                }
            }

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

    fun refresh(context: Context? = null) {
        NotificationRepositoryImpl.invalidateCache()
        loadNotifications(context)
    }
}
