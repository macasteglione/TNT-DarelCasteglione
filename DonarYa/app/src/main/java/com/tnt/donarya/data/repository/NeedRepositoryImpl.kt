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

    private val firebaseRepo = FirebaseNeedRepository()
    private val useFirebase = true 

    private val needsByMerendero = mutableMapOf<String, List<NeedItem>>()
    private val fetchedMerenderos = mutableSetOf<String>()
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
        if (useFirebase) return firebaseRepo.getByMerendero(merenderoId)
        ensureFetched(merenderoId)
        return mutex.withLock { needsByMerendero[merenderoId] ?: emptyList() }
    }

    override suspend fun add(merenderoId: String, need: NeedItem) {
        if (useFirebase) {
            firebaseRepo.add(merenderoId, need)
            return
        }
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
            mutex.withLock { fetchedMerenderos.remove(merenderoId) }
        }
    }

    override fun markAsCovered(needId: String): Result<Unit> {
        if (useFirebase) return firebaseRepo.markAsCovered(needId)
        return runBlocking(Dispatchers.IO) {
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
    }

    suspend fun refreshForMerendero(merenderoId: String) {
        if (useFirebase) return
        mutex.withLock { fetchedMerenderos.remove(merenderoId) }
        ensureFetched(merenderoId)
    }

    fun invalidateCache() {
        if (useFirebase) return
        runBlocking { mutex.withLock { fetchedMerenderos.clear() } }
    }

    suspend fun getById(needId: String): NeedItem? {
        if (useFirebase) return firebaseRepo.getById(needId)
        return mutex.withLock {
            needsByMerendero.values
                .flatten()
                .firstOrNull { it.id == needId }
        }
    }

    override suspend fun delete(needId: String): Result<Unit> {
        if (useFirebase) return firebaseRepo.delete(needId)
        return ApiClient.deleteNeed(needId)
    }

    override suspend fun update(
        needId: String,
        req: UpdateNeedRequestDto
    ): Result<Unit> {
        if (useFirebase) return firebaseRepo.update(needId, req)
        return ApiClient.updateNeed(needId, req)
    }
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
