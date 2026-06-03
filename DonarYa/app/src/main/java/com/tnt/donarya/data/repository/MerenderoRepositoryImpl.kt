package com.tnt.donarya.data.repository

import com.tnt.donarya.data.remote.ApiClient
import com.tnt.donarya.data.remote.dto.UpdateMerenderoRequestDto
import com.tnt.donarya.domain.model.Merendero
import com.tnt.donarya.domain.repository.MerenderoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

object MerenderoRepositoryImpl : MerenderoRepository {

    private val merenderos = mutableListOf<Merendero>()
    private var fetched = false

    private suspend fun ensureFetched() {
        if (!fetched) {
            val result = ApiClient.getMerenderos()
            if (result.isSuccess) {
                merenderos.clear()
                merenderos.addAll(result.getOrThrow().map { it.toDomain() })
                fetched = true
            }
        }
    }

    override fun getAll(): List<Merendero> = runBlocking(Dispatchers.IO) {
        ensureFetched()
        merenderos.toList()
    }

    override fun getById(id: String): Merendero? = runBlocking(Dispatchers.IO) {
        ensureFetched()
        merenderos.find { it.id == id }
    }

    override fun add(merendero: Merendero): Merendero = runBlocking(Dispatchers.IO) {
        val index = merenderos.indexOfFirst { it.id == merendero.id }
        if (index != -1) {
            merenderos[index] = merendero
        } else {
            merenderos.add(merendero)
        }
        merendero
    }

    suspend fun refreshMerendero(id: String) {
        val result = ApiClient.getMerenderoDetail(id)
        if (result.isSuccess) {
            val dto = result.getOrThrow()
            val domain = dto.merendero.toDomain()
            val index = merenderos.indexOfFirst { it.id == id }
            if (index != -1) merenderos[index] = domain else merenderos.add(domain)
        }
    }

    suspend fun updateMerendero(id: String, req: UpdateMerenderoRequestDto) {
        ApiClient.updateMerendero(id, req)
    }
}

private fun com.tnt.donarya.data.remote.dto.MerenderoDto.toDomain() = Merendero(
    id = id,
    name = name,
    address = address,
    neighborhood = neighborhood,
    coordinator = coordinator,
    whatsapp = whatsapp,
    kidsCount = kidsCount,
    activeNeeds = activeNeeds,
    coveredNeeds = coveredNeeds,
    isVerified = isVerified,
    distanceKm = distanceKm,
    walkMinutes = walkMinutes,
    latitude = latitude,
    longitude = longitude
)
