package com.tnt.donarya.backend.routes

import com.tnt.donarya.backend.database.Merenderos
import com.tnt.donarya.backend.database.Needs
import com.tnt.donarya.backend.database.Users
import com.tnt.donarya.backend.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Routing.merenderoRoutes() {
    route("/api/merenderos") {
        get {
            val merenderos = transaction {
                Merenderos.selectAll().map { row ->
                    MerenderoDto(
                        id = row[Merenderos.id],
                        name = row[Merenderos.name],
                        address = row[Merenderos.address],
                        neighborhood = row[Merenderos.neighborhood],
                        coordinator = row[Merenderos.coordinator],
                        whatsapp = row[Merenderos.whatsapp],
                        kidsCount = row[Merenderos.kidsCount],
                        activeNeeds = row[Merenderos.activeNeeds],
                        coveredNeeds = row[Merenderos.coveredNeeds],
                        isVerified = row[Merenderos.isVerified],
                        distanceKm = 0.0,
                        walkMinutes = 0,
                        latitude = row[Merenderos.latitude],
                        longitude = row[Merenderos.longitude]
                    )
                }
            }
            call.respond(HttpStatusCode.OK, merenderos)
        }

        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID requerido"))
            val row = transaction { Merenderos.selectAll().where { Merenderos.id eq id }.singleOrNull() }
            if (row == null) return@get call.respond(HttpStatusCode.NotFound, mapOf("error" to "No encontrado"))

            val needs = transaction {
                Needs.selectAll().where { Needs.merenderoId eq id }.map { row ->
                    NeedItemDto(
                        id = row[Needs.id],
                        merenderoId = row[Needs.merenderoId],
                        title = row[Needs.title],
                        description = row[Needs.description],
                        type = row[Needs.type],
                        urgency = row[Needs.urgency],
                        items = parseJsonList(row[Needs.items]),
                        publishedMinutesAgo = row[Needs.publishedMinutesAgo],
                        donorsOnWay = row[Needs.donorsOnWay],
                        isCovered = row[Needs.isCovered]
                    )
                }
            }

            val m = MerenderoDto(
                id = row[Merenderos.id],
                name = row[Merenderos.name],
                address = row[Merenderos.address],
                neighborhood = row[Merenderos.neighborhood],
                coordinator = row[Merenderos.coordinator],
                whatsapp = row[Merenderos.whatsapp],
                kidsCount = row[Merenderos.kidsCount],
                activeNeeds = row[Merenderos.activeNeeds],
                coveredNeeds = row[Merenderos.coveredNeeds],
                isVerified = row[Merenderos.isVerified],
                distanceKm = 0.0,
                walkMinutes = 0,
                latitude = row[Merenderos.latitude],
                longitude = row[Merenderos.longitude]
            )
            call.respond(HttpStatusCode.OK, MerenderoWithNeedsDto(merendero = m, needs = needs))
        }

        authenticate("auth-jwt") {
            put("/{id}") {
                val userId = call.principal<JWTPrincipal>()?.payload?.subject ?: return@put call.respond(HttpStatusCode.Unauthorized)
                val id = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)

                if (userId != id) return@put call.respond(HttpStatusCode.Forbidden)

                val req = call.receive<UpdateMerenderoRequest>()
                transaction {
                    val sets = mutableListOf<String>()
                    val args = mutableListOf<Pair<IColumnType<*>, Any?>>()
                    req.name?.let { sets.add("name = ?"); args.add(Merenderos.name.columnType to it) }
                    req.address?.let { sets.add("address = ?"); args.add(Merenderos.address.columnType to it) }
                    req.neighborhood?.let { sets.add("neighborhood = ?"); args.add(Merenderos.neighborhood.columnType to it) }
                    req.whatsapp?.let { sets.add("whatsapp = ?"); args.add(Merenderos.whatsapp.columnType to it) }
                    req.kidsCount?.let { sets.add("kids_count = ?"); args.add(Merenderos.kidsCount.columnType to it) }
                    req.latitude?.let { sets.add("latitude = ?"); args.add(Merenderos.latitude.columnType to it) }
                    req.longitude?.let { sets.add("longitude = ?"); args.add(Merenderos.longitude.columnType to it) }
                    if (sets.isNotEmpty()) {
                        args.add(Merenderos.id.columnType to id)
                        exec("UPDATE merenderos SET ${sets.joinToString(", ")} WHERE id = ?", args)
                    }
                }
                call.respond(HttpStatusCode.OK, mapOf("ok" to true))
            }
        }
    }
}

private fun parseJsonList(json: String): List<String> {
    return try {
        kotlinx.serialization.json.Json.decodeFromString<List<String>>(json)
    } catch (_: Exception) {
        emptyList()
    }
}
