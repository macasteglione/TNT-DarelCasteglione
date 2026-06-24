package com.tnt.donarya.data

import android.content.Context
import com.tnt.donarya.data.repository.FirebaseNotificationRepository
import com.tnt.donarya.data.repository.FirebaseUserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object GlobalNotificationObserver {
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private var seenIds = mutableSetOf<String>()
    private var job: Job? = null
    private var appContext: Context? = null

    fun start(context: Context) {
        appContext = context
        val currentUser = FirebaseUserRepository.getCurrentUser() ?: return
        val userId = currentUser.id

        NotificationHelper.init(context)

        job?.cancel()
        job = CoroutineScope(Dispatchers.IO).launch {
            FirebaseNotificationRepository.getUnreadRealtime(userId)
                .collect { unread ->
                    val newOnes = unread.filter { it.id !in seenIds }
                    if (newOnes.isNotEmpty()) {
                        val payloads = newOnes.map {
                            NotifPayload(
                                id = it.id,
                                title = "DonarYa",
                                message = it.message,
                                relatedNeedId = it.relatedNeedId ?: ""
                            )
                        }
                        NotificationHelper.showMultiple(context, payloads)
                        seenIds.addAll(newOnes.map { it.id })
                    }
                    _unreadCount.value = unread.size
                }
        }
    }

    fun restartForCurrentUser() {
        appContext?.let { ctx ->
            seenIds.clear()
            start(ctx)
        }
    }
}
