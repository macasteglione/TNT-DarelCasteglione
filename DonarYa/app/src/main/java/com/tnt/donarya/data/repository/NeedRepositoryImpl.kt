package com.tnt.donarya.data.repository

import com.tnt.donarya.data.remote.ApiClient
import com.tnt.donarya.data.remote.dto.CreateNeedRequestDto
import com.tnt.donarya.data.remote.dto.UpdateNeedRequestDto
import com.tnt.donarya.domain.model.NeedItem
import com.tnt.donarya.domain.model.NeedType
import com.tnt.donarya.domain.model.UrgencyLevel
import com.tnt.donarya.domain.repository.NeedRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object NeedRepositoryImpl : NeedRepository {

    // Usamos un Map para que cada merendero tenga su propia lista de needs,
    // en lugar de una lista plana compartida que se pisa entre coroutines.
    private val needsByMerendero = mutableMapOf<String, List<NeedItem>>()
    private val fetchedMerenderos = mutableSetOf<String>()

    // Mutex para serializar el acceso concurrente (pull-to-refresh lanza
    // varias coroutines en paralelo, una por merendero).
    private val mutex = Mutex()

    private suspend fun ensureFetched(merenderoId: String) {
        mutex.withLock {
            if (merenderoId in fetchedMerenderos) return
            val result = ApiClient.getMerenderoDetail(merenderoId)
            if (result.isSuccess) {
                val dto = result.getOrThrow()
                needsByMerendero[merenderoId] = dto.needs.map { it.toDomain() }
                fetchedMerenderos.add(merenderoId)
            }
        }
    }

    override suspend fun getByMerendero(merenderoId: String): List<NeedItem> {
        ensureFetched(merenderoId)
        return mutex.withLock { needsByMerendero[merenderoId] ?: emptyList() }
    }

    override suspend fun add(merenderoId: String, need: NeedItem) {
        val result = ApiClient.createNeed(
            CreateNeedRequestDto(
                title = need.title,
                description = need.description,
                type = need.type.name,
                urgency = need.urgency.name,
                items = need.items
            )
        )
        if (result.isSuccess) {
            // Forzar re-fetch la próxima vez para obtener el ID real del servidor
            mutex.withLock { fetchedMerenderos.remove(merenderoId) }
        }
    }

    override fun markAsCovered(needId: String): Result<Unit> = runBlocking(Dispatchers.IO) {
        val result = ApiClient.markNeedCovered(needId)
        if (result.isSuccess) {
            mutex.withLock {
                needsByMerendero.forEach { (mId, list) ->
                    val idx = list.indexOfFirst { it.id == needId }
                    if (idx != -1) {
                        needsByMerendero[mId] = list.toMutableList().also {
                            it[idx] = it[idx].copy(isCovered = true)
                        }
                    }
                }
            }
        }
        result
    }

    suspend fun refreshForMerendero(merenderoId: String) {
        mutex.withLock { fetchedMerenderos.remove(merenderoId) }
        ensureFetched(merenderoId)
    }

    fun invalidateCache() {
        // runBlocking porque la firma no es suspend y se llama desde el ViewModel
        runBlocking { mutex.withLock { fetchedMerenderos.clear() } }
    }

    suspend fun getById(needId: String): NeedItem? {
        return mutex.withLock {
            needsByMerendero.values
                .flatten()
                .firstOrNull { it.id == needId }
        }
    }

    override suspend fun delete(needId: String): Result<Unit> =
        ApiClient.deleteNeed(needId)

    override suspend fun update(
        needId: String,
        req: UpdateNeedRequestDto
    ): Result<Unit> = ApiClient.updateNeed(needId, req)

}

private fun com.tnt.donarya.data.remote.dto.NeedItemDto.toDomain() = NeedItem(
    id = id,
    merenderoId = merenderoId,
    title = title,
    description = description,
    type = try { NeedType.valueOf(type) } catch (_: Exception) { NeedType.OTROS },
    urgency = try { UrgencyLevel.valueOf(urgency) } catch (_: Exception) { UrgencyLevel.SIN_APURO },
    items = items,
    publishedMinutesAgo = publishedMinutesAgo,
    donorsOnWay = donorsOnWay,
    isCovered = isCovered
)