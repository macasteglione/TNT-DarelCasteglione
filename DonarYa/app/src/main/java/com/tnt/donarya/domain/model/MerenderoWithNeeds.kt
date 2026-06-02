package com.tnt.donarya.domain.model

data class MerenderoWithNeeds(
    val merendero: Merendero,
    val needs: List<NeedItem>
)