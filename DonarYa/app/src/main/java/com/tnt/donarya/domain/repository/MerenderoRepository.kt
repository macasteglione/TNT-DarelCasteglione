package com.tnt.donarya.domain.repository

import com.tnt.donarya.domain.model.Merendero

interface MerenderoRepository {
    fun getAll(): List<Merendero>
    fun getById(id: String): Merendero?
    fun add(merendero: Merendero): Merendero
}