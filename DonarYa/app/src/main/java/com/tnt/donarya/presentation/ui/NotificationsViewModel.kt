package com.tnt.donarya.presentation.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tnt.donarya.data.NotificationHelper
import com.tnt.donarya.data.NotifPayload
import com.tnt.donarya.data.repository.NotificationRepositoryImpl
import com.tnt.donarya.domain.model.NotificationItem
import kotlinx.coroutines.launch
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

    init { viewModelScope.launch { loadNotifications() } }

    suspend fun loadNotifications(context: Context? = null) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        try {
            val list = NotificationRepositoryImpl.getNotifications()
            val unread = list.count { !it.isRead }

            if (context != null) {
                NotificationHelper.init(context)
                val newOnes = list.filter { it.id !in seenIds && !it.isRead }
                if (newOnes.isNotEmpty()) {
                    val payloads = newOnes.map { item ->
                        NotifPayload(
                            id = item.id,
                            title = "DonarYa",
                            message = item.message,
                            relatedNeedId = item.relatedNeedId ?: ""
                        )
                    }
                    NotificationHelper.showMultiple(context, payloads)
                    seenIds.addAll(newOnes.map { it.id })
                }
            }

            _uiState.value = _uiState.value.copy(
                notifications = list,
                unreadCount = unread,
                isLoading = false
            )
        } catch (_: Exception) {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun markAsRead(notiId: String) {
        viewModelScope.launch {
            try {
                NotificationRepositoryImpl.markAsRead(notiId)
                val list = NotificationRepositoryImpl.getNotifications()
                val unread = list.count { !it.isRead }
                _uiState.value = _uiState.value.copy(
                    notifications = list,
                    unreadCount = unread
                )
            } catch (_: Exception) { }
        }
    }

    fun refresh(context: Context? = null) {
        viewModelScope.launch {
            NotificationRepositoryImpl.invalidateCache()
            loadNotifications(context)
        }
    }
}
