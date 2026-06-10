package com.tnt.donarya.domain.repository

import com.tnt.donarya.data.remote.dto.UpdateNeedRequestDto
import com.tnt.donarya.domain.model.NeedItem

interface NeedRepository {
    suspend fun getByMerendero(merenderoId: String): List<NeedItem>
    suspend fun add(merenderoId: String, need: NeedItem)

    suspend fun delete(needId: String): Result<Unit>

    suspend fun update(needId: String, req: UpdateNeedRequestDto): Result<Unit>  // nueva
    fun markAsCovered(needId: String): Result<Unit>
}