package com.tnt.donarya.data.repository

import com.tnt.donarya.domain.model.NeedItem
import com.tnt.donarya.domain.repository.NeedRepository

object NeedRepositoryImpl : NeedRepository {

    private val needs = mutableListOf<NeedItem>()

    override fun getByMerendero(merenderoId: String): List<NeedItem> {
        return needs.filter { it.merenderoId == merenderoId }
    }

    override fun add(merenderoId: String, need: NeedItem) {
        needs.add(need)
    }

    override fun markAsCovered(needId: String): Result<Unit> {
        val index = needs.indexOfFirst { it.id == needId }
        return if (index != -1) {
            val current = needs[index]
            needs[index] = current.copy(isCovered = true)
            Result.success(Unit)
        } else {
            Result.failure(Exception("Necesidad no encontrada"))
        }
    }
}
