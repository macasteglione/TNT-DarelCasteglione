package com.tnt.donarya.data.model

enum class UserRole { DONANTE, MERENDERO }

enum class UrgencyLevel(val label: String, val color: Long) {
    URGENTE("Urgente", 0xFFE63946),
    ESTA_SEMANA("Esta semana", 0xFFF4A261),
    SIN_APURO("Sin apuro", 0xFF52B788)
}

enum class NeedType(val label: String, val emoji: String) {
    ALIMENTOS("Alimentos", "🍞"),
    ROPA("Ropa", "👕"),
    GAS("Gas", "🔥"),
    ABRIGO("Abrigo", "🧥"),
    OTROS("Otros", "📦")
}

data class NeedItem(
    val id: String,
    val title: String,
    val description: String,
    val type: NeedType,
    val urgency: UrgencyLevel,
    val items: List<String> = emptyList(),
    val publishedMinutesAgo: Int = 0,
    val donorsOnWay: Int = 0,
    val isCovered: Boolean = false
)

data class Merendero(
    val id: String,
    val name: String,
    val address: String,
    val neighborhood: String,
    val coordinator: String,
    val whatsapp: String,
    val kidsCount: Int,
    val activeNeeds: Int,
    val coveredNeeds: Int,
    val isVerified: Boolean = false,
    val distanceKm: Double = 0.0,
    val walkMinutes: Int = 0,
    val needs: List<NeedItem> = emptyList(),
    val latitude: Double = -42.7692,
    val longitude: Double = -65.0375
)

data class AlertItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: AlertType,
    val minutesAgo: Int,
    val isRead: Boolean = false,
    val isUrgent: Boolean = false
)

enum class AlertType { URGENTE, RESPUESTA, CUBIERTA, AGRADECIMIENTO, RECORDATORIO, SISTEMA }

data class UserProfile(
    val name: String,
    val role: UserRole,
    val donationsCount: Int = 0,
    val mendecerosHelped: Int = 0,
    val beneficiados: Int = 0,
    val badges: List<Badge> = emptyList(),
    val recentDonations: List<DonationRecord> = emptyList()
)

data class Badge(val name: String, val emoji: String, val earned: Boolean)

data class DonationRecord(
    val type: NeedType,
    val merenderoName: String,
    val daysAgo: Int,
    val delivered: Boolean
)

// Sample data
object SampleData {

    val needs1 = listOf(
        NeedItem(
            "n1",
            "Alimentos secos",
            "Nos quedamos sin arroz, fideos y aceite para la merienda de hoy. Tenemos 34 chicos esperando. Cualquier cantidad ayuda.",
            NeedType.ALIMENTOS,
            UrgencyLevel.URGENTE,
            listOf("Arroz", "Fideos", "Aceite", "Harina"),
            23,
            2
        ),
        NeedItem(
            "n2",
            "Ropa de abrigo",
            "Camperas, bufandas, guantes para los niños.",
            NeedType.ROPA,
            UrgencyLevel.ESTA_SEMANA,
            listOf("Camperas", "Bufandas", "Guantes"),
            2880,
            0
        ),
        NeedItem(
            "n3",
            "Garrafa de gas",
            "1 garrafa de 10kg para la cocina.",
            NeedType.GAS,
            UrgencyLevel.SIN_APURO,
            listOf("Garrafa 10kg"),
            7200,
            1
        )
    )

    val merenderos = listOf(
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
            needs1,
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
            emptyList(),
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
            emptyList(),
            -42.7900,
            -65.0200
        )
    )

    val alerts = listOf(
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
            "Julián M. confirmó que lleva arroz y aceite · ETA 18 min",
            AlertType.RESPUESTA,
            6,
            false
        ),
        AlertItem(
            "a3",
            "Nueva urgencia: Gas",
            "Comedor San Cayetano necesita garrafa · 2,8 km de vos",
            AlertType.URGENTE,
            15,
            false,
            true
        ),
        AlertItem(
            "a4",
            "Necesidad cubierta",
            "Tu donación de ropa a Merendero Esperanza fue confirmada",
            AlertType.CUBIERTA,
            240,
            true
        ),
        AlertItem(
            "a5",
            "¡Gracias de Marta!",
            "Merendero Los Girasoles te agradeció por tu donación",
            AlertType.AGRADECIMIENTO,
            720,
            true
        ),
        AlertItem(
            "a6",
            "Recordatorio de donación",
            "Confirmaste llevar frazadas al Comedor Norte · ¿pudiste ir?",
            AlertType.RECORDATORIO,
            1440,
            true
        )
    )

    val donante = UserProfile(
        name = "Julián Morales",
        role = UserRole.DONANTE,
        donationsCount = 17,
        mendecerosHelped = 8,
        beneficiados = 142,
        badges = listOf(
            Badge("Estrella", "⭐", true),
            Badge("Plantador", "🌱", true),
            Badge("Fiel", "💙", true),
            Badge("Trofeo", "🏆", false)
        ),
        recentDonations = listOf(
            DonationRecord(NeedType.ALIMENTOS, "Los Girasoles", 3, true),
            DonationRecord(NeedType.ROPA, "Esperanza", 5, true),
            DonationRecord(NeedType.GAS, "San Cayetano", 12, true)
        )
    )

    val merendero = UserProfile(
        name = "Marta González",
        role = UserRole.MERENDERO,
        donationsCount = 0,
        mendecerosHelped = 0,
        beneficiados = 34
    )
}
