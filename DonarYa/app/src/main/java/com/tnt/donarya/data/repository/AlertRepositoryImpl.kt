package com.tnt.donarya.data.repository

import com.tnt.donarya.domain.model.*
import com.tnt.donarya.domain.repository.AlertRepository

class AlertRepositoryImpl : AlertRepository {

    private val alerts = mutableListOf(

        AlertItem(
            "a1",
            "Necesidad urgente cerca",
            "Los Girasoles necesita alimentos secos · 1,3 km",
            AlertType.URGENTE,
            2,
            false,
            true
        ),

        AlertItem(
            "a2",
            "Alguien va para allá",
            "Julián M. confirmó...",
            AlertType.RESPUESTA,
            6,
            false
        )
    )

    override fun getAll(): List<AlertItem> {
        return alerts
    }
}