package com.tnt.donarya.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.tnt.donarya.data.remote.dto.UpdateNeedRequestDto
import com.tnt.donarya.domain.model.NeedItem
import com.tnt.donarya.domain.model.NeedType
import com.tnt.donarya.domain.model.UrgencyLevel
import com.tnt.donarya.domain.repository.NeedRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseNeedRepository : NeedRepository {

    private val db = FirebaseFirestore.getInstance()
    private val needsCollection = db.collection("needs")

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

    /**
     * Devuelve un Flow que emite la lista de necesidades en tiempo real
     */
    fun getByMerenderoRealtime(merenderoId: String): Flow<List<NeedItem>> = callbackFlow {
        val subscription = needsCollection
            .whereEqualTo("merenderoId", merenderoId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { it.toNeedItem() }
                    trySend(items)
                }
            }
        awaitClose { subscription.remove() }
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
    }

    override fun markAsCovered(needId: String): Result<Unit> {
        // En una implementación real, esto debería ser suspend o usar un callback
        // Para mantener compatibilidad con la interfaz actual:
        return try {
            needsCollection.document(needId).update("isCovered", true)
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
