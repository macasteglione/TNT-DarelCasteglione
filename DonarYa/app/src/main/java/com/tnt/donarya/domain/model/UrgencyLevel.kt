package com.tnt.donarya.domain.model

enum class UrgencyLevel(val label: String, val color: Long) {
    URGENTE("Urgente", 0xFFE63946),
    ESTA_SEMANA("Esta semana", 0xFFF4A261),
    SIN_APURO("Sin apuro", 0xFF52B788)
}