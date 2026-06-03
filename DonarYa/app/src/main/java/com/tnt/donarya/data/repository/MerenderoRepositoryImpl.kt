package com.tnt.donarya.data.repository

import com.tnt.donarya.domain.model.Merendero
import com.tnt.donarya.domain.repository.MerenderoRepository

object MerenderoRepositoryImpl : MerenderoRepository {
    private val merenderos = mutableListOf<Merendero>()

    override fun getAll() = merenderos.toList()
    override fun getById(id: String) = merenderos.find { it.id == id }
    override fun add(merendero: Merendero): Merendero {
        val index = merenderos.indexOfFirst { it.id == merendero.id }
        if (index != -1) {
            merenderos[index] = merendero
        } else {
            merenderos.add(merendero)
        }
        return merendero
    }
}
