package com.tnt.donarya.data.repository

import android.annotation.SuppressLint
import com.google.firebase.firestore.FirebaseFirestore
import com.tnt.donarya.domain.model.Merendero
import com.tnt.donarya.domain.repository.MerenderoRepository
import kotlinx.coroutines.tasks.await

object FirebaseMerenderoRepository : MerenderoRepository {

    @SuppressLint("StaticFieldLeak")
    private val db = FirebaseFirestore.getInstance()
    private val merenderosCollection = db.collection("merenderos")

    private var cached: List<Merendero>? = null

    private suspend fun ensureFetched() {
        if (cached != null) return
        val snapshot = merenderosCollection.get().await()
        cached = snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
            Merendero(
                id = doc.id,
                name = data["name"] as? String ?: "",
                address = data["address"] as? String ?: "",
                neighborhood = data["neighborhood"] as? String ?: "",
                coordinator = data["coordinator"] as? String ?: "",
                whatsapp = data["whatsapp"] as? String ?: "",
                kidsCount = (data["kidsCount"] as? Long)?.toInt() ?: 0,
                activeNeeds = (data["activeNeeds"] as? Long)?.toInt() ?: 0,
                coveredNeeds = (data["coveredNeeds"] as? Long)?.toInt() ?: 0,
                isVerified = data["isVerified"] as? Boolean ?: false,
                distanceKm = (data["distanceKm"] as? Double) ?: 0.0,
                walkMinutes = (data["walkMinutes"] as? Long)?.toInt() ?: 0,
                latitude = data["latitude"] as? Double ?: -42.7692,
                longitude = data["longitude"] as? Double ?: -65.0375
            )
        }
    }

    override suspend fun getAll(): List<Merendero> {
        ensureFetched()
        return cached ?: emptyList()
    }

    override suspend fun getById(id: String): Merendero? {
        ensureFetched()
        return cached?.find { it.id == id }
    }

    override suspend fun add(merendero: Merendero): Merendero {
        val data = hashMapOf(
            "name" to merendero.name,
            "address" to merendero.address,
            "neighborhood" to merendero.neighborhood,
            "coordinator" to merendero.coordinator,
            "whatsapp" to merendero.whatsapp,
            "kidsCount" to merendero.kidsCount,
            "activeNeeds" to merendero.activeNeeds,
            "coveredNeeds" to merendero.coveredNeeds,
            "isVerified" to merendero.isVerified,
            "distanceKm" to merendero.distanceKm,
            "walkMinutes" to merendero.walkMinutes,
            "latitude" to merendero.latitude,
            "longitude" to merendero.longitude
        )
        if (merendero.id.isNotEmpty()) {
            merenderosCollection.document(merendero.id).set(data).await()
        } else {
            val docRef = merenderosCollection.add(data).await()
            return merendero.copy(id = docRef.id)
        }
        cached = null
        return merendero
    }

    fun invalidateCache() {
        cached = null
    }
}
