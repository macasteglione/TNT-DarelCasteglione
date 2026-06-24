package com.tnt.donarya.data.repository

import com.tnt.donarya.data.remote.dto.UpdateNeedRequestDto
import com.tnt.donarya.domain.model.NeedItem
import com.tnt.donarya.domain.repository.NeedRepository

object NeedRepositoryImpl : NeedRepository {

    private val firebaseRepo = FirebaseNeedRepository()

    override suspend fun getByMerendero(merenderoId: String): List<NeedItem> {
        return firebaseRepo.getByMerendero(merenderoId)
    }

    override suspend fun add(merenderoId: String, need: NeedItem) {
        firebaseRepo.add(merenderoId, need)
    }

    override fun markAsCovered(needId: String): Result<Unit> {
        return firebaseRepo.markAsCovered(needId)
    }

    suspend fun getById(needId: String): NeedItem? {
        return firebaseRepo.getById(needId)
    }

    override suspend fun delete(needId: String): Result<Unit> {
        return firebaseRepo.delete(needId)
    }

    override suspend fun update(
        needId: String,
        req: UpdateNeedRequestDto
    ): Result<Unit> {
        return firebaseRepo.update(needId, req)
    }
}
