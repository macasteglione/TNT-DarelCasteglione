package com.tnt.donarya.data.repository

import com.tnt.donarya.domain.model.Merendero
import com.tnt.donarya.domain.model.SampleData.needs1
import com.tnt.donarya.domain.repository.MerenderoRepository

class MerenderoRepositoryImpl : MerenderoRepository {
    private val merenderos = mutableListOf(
        // pegás los merenderos que tenías en SampleData
        Merendero(
            "m1",
            "Los Girasoles",
            "Av. San Martín 1240, Barrio Norte",
            "Barrio Norte",
            "Marta González",
            "+54 9 341 555-1234",
            34,
            3,
            12,
            true,
            1.2,
            14,
            -42.7692,
            -65.0375
        ),
        Merendero(
            "m2",
            "San Cayetano",
            "Calle 25 de Mayo 890",
            "Villa Pueyrredón",
            "Carlos Ruiz",
            "+54 9 341 555-5678",
            45,
            2,
            8,
            false,
            2.8,
            35,
            -42.7800,
            -65.0500
        ),
        Merendero(
            "m3",
            "Filomena",
            "Ruta 3 km 5",
            "Periferia Sur",
            "Ana López",
            "+54 9 341 555-9999",
            20,
            1,
            5,
            false,
            4.1,
            52,
            -42.7900,
            -65.0200
        )
    )
    override fun getAll() = merenderos.toList()
    override fun getById(id: String) = merenderos.find { it.id == id }
}