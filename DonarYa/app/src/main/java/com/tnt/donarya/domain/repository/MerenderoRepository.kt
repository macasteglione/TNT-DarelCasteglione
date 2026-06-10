package com.tnt.donarya.domain.repository

import com.tnt.donarya.domain.model.Merendero

interface MerenderoRepository {
    suspend fun getAll(): List<Merendero>
    suspend fun getById(id: String): Merendero?
    suspend fun add(merendero: Merendero): Merendero
}