package com.tnt.donarya.domain.model
// Sample data
object SampleData {

    val needs1 = listOf(
        NeedItem(
            "n1",
            merenderoId = "m1",
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
            merenderoId = "m2",
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
            merenderoId = "m3",
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
