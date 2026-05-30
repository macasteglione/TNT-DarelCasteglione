package com.tnt.donarya.domain.repository

import com.tnt.donarya.domain.model.NeedItem

interface NeedRepository {
    fun getByMerendero(merenderoId: String): List<NeedItem>
    fun add(merenderoId: String, need: NeedItem)
    fun markAsCovered(needId: String): Result<Unit>
}