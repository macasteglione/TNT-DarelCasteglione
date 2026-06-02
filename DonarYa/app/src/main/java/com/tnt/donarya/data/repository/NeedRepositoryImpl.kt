package com.tnt.donarya.data.repository

import com.tnt.donarya.domain.model.NeedItem
import com.tnt.donarya.domain.model.NeedType
import com.tnt.donarya.domain.model.UrgencyLevel
import com.tnt.donarya.domain.repository.NeedRepository

class NeedRepositoryImpl : NeedRepository {

    private val needs = mutableListOf(

        NeedItem(
            id = "n1",
            merenderoId = "m1",
            title = "Alimentos secos",
            description = "Nos quedamos sin arroz y fideos",
            type = NeedType.ALIMENTOS,
            urgency = UrgencyLevel.URGENTE,
            items = listOf("Arroz", "Fideos"),
            publishedMinutesAgo = 20,
            donorsOnWay = 1
        ),

        NeedItem(
            id = "n2",
            merenderoId = "m2",
            title = "Ropa de abrigo",
            description = "Camperas y frazadas",
            type = NeedType.ROPA,
            urgency = UrgencyLevel.ESTA_SEMANA,
            items = listOf("Camperas", "Frazadas"),
            publishedMinutesAgo = 180,
            donorsOnWay = 0,
            isCovered = true,
        )
    )

    override fun getByMerendero(
        merenderoId: String
    ): List<NeedItem> {

        return needs.filter {
            it.merenderoId == merenderoId
        }
    }

    override fun add(
        merenderoId: String,
        need: NeedItem
    ) {

        needs.add(need)
    }

    override fun markAsCovered(
        needId: String
    ): Result<Unit> {

        val index = needs.indexOfFirst {
            it.id == needId
        }

        return if (index != -1) {

            val current = needs[index]

            needs[index] = current.copy(
                isCovered = true
            )

            Result.success(Unit)

        } else {

            Result.failure(
                Exception("Necesidad no encontrada")
            )
        }
    }
}