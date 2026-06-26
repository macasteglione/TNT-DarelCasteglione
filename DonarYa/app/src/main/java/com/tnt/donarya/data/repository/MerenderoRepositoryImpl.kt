package com.tnt.donarya.data.repository

import com.tnt.donarya.domain.model.Merendero
import com.tnt.donarya.domain.repository.MerenderoRepository

object MerenderoRepositoryImpl : MerenderoRepository {

    override suspend fun getAll(): List<Merendero> {
        return FirebaseMerenderoRepository.getAll()
    }

    override suspend fun getById(id: String): Merendero? {
        return FirebaseMerenderoRepository.getById(id)
    }

    override suspend fun add(merendero: Merendero): Merendero {
        return FirebaseMerenderoRepository.add(merendero)
    }

    fun invalidateCache() {
        FirebaseMerenderoRepository.invalidateCache()
    }

    suspend fun updateWhatsapp(merenderoId: String, whatsapp: String) {
        FirebaseMerenderoRepository.updateWhatsapp(merenderoId, whatsapp)
    }
}
