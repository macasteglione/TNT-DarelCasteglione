package com.tnt.donarya.data.repository

import android.annotation.SuppressLint
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tnt.donarya.domain.model.NotificationItem
import com.tnt.donarya.domain.repository.NotificationRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

object FirebaseNotificationRepository : NotificationRepository {

    @SuppressLint("StaticFieldLeak")
    private val db = FirebaseFirestore.getInstance()
    private val notificationsCollection = db.collection("notifications")

    private var cached: List<NotificationItem>? = null

    override suspend fun getNotifications(): List<NotificationItem> {
        val currentUser = FirebaseUserRepository.getCurrentUser()
        val userId = currentUser?.id ?: return cached ?: emptyList()

        return try {
            val snapshot = notificationsCollection
                .whereEqualTo("userId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val list = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                NotificationItem(
                    id = doc.id,
                    userId = data["userId"] as? String ?: "",
                    type = data["type"] as? String ?: "",
                    message = data["message"] as? String ?: "",
                    relatedNeedId = data["relatedNeedId"] as? String,
                    relatedUserId = data["relatedUserId"] as? String,
                    isRead = data["isRead"] as? Boolean ?: false,
                    createdAt = data["createdAt"] as? String ?: ""
                )
            }
            cached = list
            list
        } catch (_: Exception) {
            cached ?: emptyList()
        }
    }

    override suspend fun markAsRead(notiId: String): Result<Unit> {
        return try {
            notificationsCollection.document(notiId).update("isRead", true).await()
            cached = cached?.map { if (it.id == notiId) it.copy(isRead = true) else it }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun invalidateCache() {
        cached = null
    }

    fun getUnreadRealtime(userId: String): Flow<List<NotificationItem>> = callbackFlow {
        val listener = notificationsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        NotificationItem(
                            id = doc.id,
                            userId = data["userId"] as? String ?: "",
                            type = data["type"] as? String ?: "",
                            message = data["message"] as? String ?: "",
                            relatedNeedId = data["relatedNeedId"] as? String,
                            relatedUserId = data["relatedUserId"] as? String,
                            isRead = data["isRead"] as? Boolean ?: false,
                            createdAt = data["createdAt"] as? String ?: ""
                        )
                    }
                    trySend(items)
                }
            }
        awaitClose { listener.remove() }
    }
}
