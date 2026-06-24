package com.tnt.donarya.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.tnt.donarya.data.remote.dto.UpdateNeedRequestDto
import com.tnt.donarya.domain.model.NeedItem
import com.tnt.donarya.domain.model.NeedType
import com.tnt.donarya.domain.model.UrgencyLevel
import com.tnt.donarya.domain.repository.NeedRepository
import kotlinx.coroutines.tasks.await

class FirebaseNeedRepository : NeedRepository {

    private val db = FirebaseFirestore.getInstance()
    private val needsCollection = db.collection("needs")
    private val confirmationsCollection = db.collection("donorConfirmations")

    suspend fun confirmNeed(needId: String): Result<Int> {
        val userId = FirebaseUserRepository.getCurrentUser()?.id
            ?: return Result.failure(Exception("No hay usuario logueado"))
        return try {
            val confirmationId = "${needId}_${userId}"
            val existing = confirmationsCollection.document(confirmationId).get().await()
            if (existing.exists()) {
                return Result.failure(Exception("Ya confirmaste esta donación"))
            }

            confirmationsCollection.document(confirmationId).set(
                hashMapOf(
                    "needId" to needId,
                    "userId" to userId,
                    "createdAt" to com.google.firebase.Timestamp.now()
                )
            ).await()

            needsCollection.document(needId)
                .update("donorsOnWay", com.google.firebase.firestore.FieldValue.increment(1))
                .await()

            val updated = needsCollection.document(needId).get().await()
            val donorsOnWay = (updated.data?.get("donorsOnWay") as? Long)?.toInt() ?: 1

            // Crear notificación para el usuario del merendero (no el docId del merendero)
            val needData = updated.data
            val merenderoId = needData?.get("merenderoId") as? String ?: ""
            if (merenderoId.isNotEmpty()) {
                val merenderoUser = db.collection("users")
                    .whereEqualTo("merenderoId", merenderoId)
                    .get()
                    .await()
                    .documents
                    .firstOrNull()
                val merenderoUserId = merenderoUser?.id ?: ""
                if (merenderoUserId.isNotEmpty()) {
                    val notiData = hashMapOf(
                        "userId" to merenderoUserId,
                        "type" to "DONATION_CONFIRMED",
                        "message" to "Un donante confirmó su ayuda para: ${needData?.get("title") ?: ""}",
                        "relatedNeedId" to needId,
                        "relatedUserId" to userId,
                        "isRead" to false,
                        "createdAt" to java.text.SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss'Z'",
                            java.util.Locale.US
                        ).format(java.util.Date())
                    )
                    db.collection("notifications").add(notiData).await()
                }
            }

            Result.success(donorsOnWay)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkIfConfirmed(needId: String): Boolean {
        val userId = FirebaseUserRepository.getCurrentUser()?.id ?: return false
        return try {
            val confirmationId = "${needId}_${userId}"
            confirmationsCollection.document(confirmationId).get().await().exists()
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun getByMerendero(merenderoId: String): List<NeedItem> {
        return try {
            val snapshot = needsCollection
                .whereEqualTo("merenderoId", merenderoId)
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toNeedItem() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun add(merenderoId: String, need: NeedItem) {
        val data = hashMapOf(
            "merenderoId" to merenderoId,
            "title" to need.title,
            "description" to need.description,
            "type" to need.type.name,
            "urgency" to need.urgency.name,
            "items" to need.items,
            "isCovered" to false,
            "createdAt" to com.google.firebase.Timestamp.now(),
            "donorsOnWay" to 0
        )
        needsCollection.add(data).await()

        // Notificar a todos los donantes
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val donors = db.collection("users")
                    .whereEqualTo("rol", "DONANTE")
                    .get()
                    .await()
                val batch = db.batch()
                val now =
                    java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
                        .format(java.util.Date())
                for (doc in donors.documents) {
                    val donorId = doc.id
                    val notiRef = db.collection("notifications").document()
                    batch.set(
                        notiRef, hashMapOf(
                            "userId" to donorId,
                            "type" to "NEW_NEED",
                            "message" to "Nueva necesidad publicada: ${need.title}",
                            "relatedNeedId" to "",
                            "relatedUserId" to merenderoId,
                            "isRead" to false,
                            "createdAt" to now
                        )
                    )
                }
                batch.commit().await()
            } catch (e: Exception) {
                android.util.Log.e("FirebaseNeedRepo", "Error al notificar donantes", e)
            }
        }
    }

    override fun markAsCovered(needId: String): Result<Unit> {
        return try {
            kotlinx.coroutines.runBlocking {
                needsCollection.document(needId).update("isCovered", true).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun delete(needId: String): Result<Unit> {
        return try {
            needsCollection.document(needId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun update(needId: String, req: UpdateNeedRequestDto): Result<Unit> {
        return try {
            val data = hashMapOf(
                "title" to req.title,
                "description" to req.description,
                "type" to req.type,
                "urgency" to req.urgency,
                "items" to req.items
            )
            needsCollection.document(needId).update(data as Map<String, Any>).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getById(needId: String): NeedItem? {
        return try {
            needsCollection.document(needId).get().await().toNeedItem()
        } catch (e: Exception) {
            null
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.toNeedItem(): NeedItem? {
        return try {
            val data = this.data ?: return null
            NeedItem(
                id = this.id,
                merenderoId = data["merenderoId"] as? String ?: "",
                title = data["title"] as? String ?: "",
                description = data["description"] as? String ?: "",
                type = NeedType.valueOf(data["type"] as? String ?: "OTROS"),
                urgency = UrgencyLevel.valueOf(data["urgency"] as? String ?: "SIN_APURO"),
                items = (data["items"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                isCovered = data["isCovered"] as? Boolean ?: false,
                donorsOnWay = (data["donorsOnWay"] as? Long)?.toInt() ?: 0,
                publishedMinutesAgo = calculateMinutesAgo(data["createdAt"] as? com.google.firebase.Timestamp)
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateMinutesAgo(timestamp: com.google.firebase.Timestamp?): Int {
        if (timestamp == null) return 0
        val diff = com.google.firebase.Timestamp.now().seconds - timestamp.seconds
        return (diff / 60).toInt()
    }
}
