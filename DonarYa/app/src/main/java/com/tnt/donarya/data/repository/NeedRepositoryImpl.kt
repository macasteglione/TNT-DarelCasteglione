package com.tnt.donarya.data.repository

import com.tnt.donarya.data.remote.ApiClient
import com.tnt.donarya.data.remote.dto.CreateNeedRequestDto
import com.tnt.donarya.domain.model.NeedItem
import com.tnt.donarya.domain.model.NeedType
import com.tnt.donarya.domain.model.UrgencyLevel
import com.tnt.donarya.domain.repository.NeedRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

object NeedRepositoryImpl : NeedRepository {

    private val needs = mutableListOf<NeedItem>()
    private var fetched = false

    private suspend fun ensureFetched(merenderoId: String) {
        if (!fetched) {
            val result = ApiClient.getMerenderoDetail(merenderoId)
            if (result.isSuccess) {
                val dto = result.getOrThrow()
                needs.addAll(dto.needs.map { it.toDomain() })
                fetched = true
            }
        }
    }

    override fun getByMerendero(merenderoId: String): List<NeedItem> = runBlocking(Dispatchers.IO) {
        ensureFetched(merenderoId)
        needs.filter { it.merenderoId == merenderoId }
    }

    override fun add(merenderoId: String, need: NeedItem) {
        runBlocking(Dispatchers.IO) {
            ApiClient.createNeed(
                CreateNeedRequestDto(
                    title = need.title,
                    description = need.description,
                    type = need.type.name,
                    urgency = need.urgency.name,
                    items = need.items
                )
            )
            needs.add(need)
        }
    }

    override fun markAsCovered(needId: String): Result<Unit> = runBlocking(Dispatchers.IO) {
        val result = ApiClient.markNeedCovered(needId)
        if (result.isSuccess) {
            val index = needs.indexOfFirst { it.id == needId }
            if (index != -1) {
                needs[index] = needs[index].copy(isCovered = true)
            }
        }
        result
    }

    suspend fun refreshForMerendero(merenderoId: String) {
        val result = ApiClient.getMerenderoDetail(merenderoId)
        if (result.isSuccess) {
            val dto = result.getOrThrow()
            needs.removeAll { it.merenderoId == merenderoId }
            needs.addAll(dto.needs.map { it.toDomain() })
        }
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
