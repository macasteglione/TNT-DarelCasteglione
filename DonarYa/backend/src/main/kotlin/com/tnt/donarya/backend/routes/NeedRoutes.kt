package com.tnt.donarya.backend.routes

import com.tnt.donarya.backend.database.Merenderos
import com.tnt.donarya.backend.database.Needs
import com.tnt.donarya.backend.models.CreateNeedRequest
import com.tnt.donarya.backend.models.NeedItemDto
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import com.tnt.donarya.backend.database.DonorConfirmations
import com.tnt.donarya.backend.database.Notifications
import com.tnt.donarya.backend.models.ConfirmNeedResponse
import com.tnt.donarya.backend.models.UpdateNeedRequest
import com.tnt.donarya.backend.database.Users
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

import com.tnt.donarya.backend.models.DonationHistoryDto
import com.tnt.donarya.backend.models.NeedHistoryDto
import com.tnt.donarya.backend.service.FirebaseService

fun Routing.needRoutes() {
    route("/api/needs") {
        authenticate("auth-jwt") {
            post {
                val userId = call.principal<JWTPrincipal>()?.payload?.subject ?: return@post call.respond(HttpStatusCode.Unauthorized)

                val merenderoRow = transaction {
                    Merenderos.selectAll().where { Merenderos.userId eq userId }.singleOrNull()
                }
                if (merenderoRow == null) return@post call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Solo merenderos pueden publicar necesidades"))

                val merenderoId = merenderoRow[Merenderos.id]
                val req = call.receive<CreateNeedRequest>()

                val id = "n_${System.currentTimeMillis()}"
                val itemsJson = Json.encodeToString(req.items)

                transaction {
                    Needs.insert {
                        it[Needs.id] = id
                        it[Needs.merenderoId] = merenderoId
                        it[Needs.title] = req.title
                        it[Needs.description] = req.description
                        it[Needs.type] = req.type
                        it[Needs.urgency] = req.urgency
                        it[Needs.items] = itemsJson
                        it[Needs.publishedMinutesAgo] = 0
                        it[Needs.donorsOnWay] = 0
                        it[Needs.isCovered] = false
                        it[Needs.createdAt] = LocalDateTime.now()
                    }

                    exec("UPDATE merenderos SET active_needs = active_needs + 1 WHERE id = ?", listOf(Merenderos.id.columnType to merenderoId))
                }

                val need = NeedItemDto(
                    id = id,
                    merenderoId = merenderoId,
                    title = req.title,
                    description = req.description,
                    type = req.type,
                    urgency = req.urgency,
                    items = req.items,
                    publishedMinutesAgo = 0,
                    donorsOnWay = 0,
                    isCovered = false
                )

                // Enviar notificación push a todos los donantes
                try {
                    val merenderoName = transaction {
                        Merenderos.selectAll().where { Merenderos.id eq merenderoId }.singleOrNull()?.get(Merenderos.name)
                    } ?: "Un merendero"

                    FirebaseService.sendToTopic(
                        topic = "donors",
                        title = "Nueva necesidad de $merenderoName",
                        body = req.title
                    )
                } catch (e: Exception) {
                    println("Error enviando notificación: ${e.message}")
                }

                call.respond(HttpStatusCode.Created, need)
            }

            put("/{id}/cover") {
                val userId = call.principal<JWTPrincipal>()?.payload?.subject ?: return@put call.respond(HttpStatusCode.Unauthorized)
                val needId = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest)

                val needRow = transaction { Needs.selectAll().where { Needs.id eq needId }.singleOrNull() }
                if (needRow == null) return@put call.respond(HttpStatusCode.NotFound)

                val merenderoRow = transaction {
                    Merenderos.selectAll().where { Merenderos.userId eq userId }.singleOrNull()
                }
                if (merenderoRow == null) return@put call.respond(HttpStatusCode.Forbidden)

                val merenderoId = merenderoRow[Merenderos.id]
                if (needRow[Needs.merenderoId] != merenderoId) return@put call.respond(HttpStatusCode.Forbidden)

                val needTitle = needRow[Needs.title]
                transaction {
                    Needs.update({ Needs.id eq needId }) {
                        it[Needs.isCovered] = true
                    }
                    exec("UPDATE merenderos SET active_needs = GREATEST(active_needs - 1, 0), covered_needs = covered_needs + 1 WHERE id = ?", listOf(Merenderos.id.columnType to merenderoId))

                    val confirmingDonors = DonorConfirmations.selectAll()
                        .where { DonorConfirmations.needId eq needId }
                        .map { it[DonorConfirmations.donorId] }

                    val now = LocalDateTime.now()
                    confirmingDonors.forEachIndexed { i, donorId ->
                        val nid = "noti_${System.currentTimeMillis()}_$i"
                        Notifications.insert {
                            it[Notifications.id] = nid
                            it[Notifications.userId] = donorId
                            it[Notifications.type] = "NECESIDAD_CUBIERTA"
                            it[Notifications.message] = "La necesidad \"$needTitle\" fue marcada como cubierta"
                            it[Notifications.relatedNeedId] = needId
                            it[Notifications.relatedUserId] = userId
                            it[Notifications.isRead] = false
                            it[Notifications.createdAt] = now
                        }
                    }
                }
                call.respond(HttpStatusCode.OK, mapOf("ok" to true))
            }
            post("/{id}/confirm") {
                val userId = call.principal<JWTPrincipal>()
                    ?.payload?.subject
                    ?: return@post call.respond(HttpStatusCode.Unauthorized)

                val needId = call.parameters["id"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest)

                // Verificar que la necesidad existe y no está cubierta
                val needRow = transaction {
                    Needs.selectAll()
                        .where { Needs.id eq needId }
                        .singleOrNull()
                } ?: return@post call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Necesidad no encontrada")
                )

                if (needRow[Needs.isCovered]) {
                    return@post call.respond(
                        HttpStatusCode.Conflict,
                        mapOf("error" to "La necesidad ya fue cubierta")
                    )
                }

                // Verificar que no confirmó antes
                val yaConfirmo = transaction {
                    DonorConfirmations.selectAll()
                        .where {
                            (DonorConfirmations.needId eq needId) and
                                    (DonorConfirmations.donorId eq userId)
                        }
                        .singleOrNull()
                }

                if (yaConfirmo != null) {
                    return@post call.respond(
                        HttpStatusCode.Conflict,
                        mapOf("error" to "Ya confirmaste que vas")
                    )
                }

                // Guardar confirmación y actualizar donorsOnWay
                val confirmId = "dc_${System.currentTimeMillis()}"
                val donorsOnWay = transaction {
                    DonorConfirmations.insert {
                        it[DonorConfirmations.id]        = confirmId
                        it[DonorConfirmations.needId]    = needId
                        it[DonorConfirmations.donorId]   = userId
                        it[DonorConfirmations.createdAt] = LocalDateTime.now()
                    }

                    Needs.update({ Needs.id eq needId }) {
                        with(SqlExpressionBuilder) {
                            it.update(Needs.donorsOnWay, Needs.donorsOnWay + 1)
                        }
                    }

                    Needs.selectAll()
                        .where { Needs.id eq needId }
                        .single()[Needs.donorsOnWay]
                }

                val needTitle = needRow[Needs.title]
                val donorNombre = transaction {
                    Users.selectAll().where { Users.id eq userId }.singleOrNull()?.get(Users.nombre)
                }
                val notiId = "noti_${System.currentTimeMillis()}"
                transaction {
                    val merenderoUserId = Merenderos.selectAll()
                        .where { Merenderos.id eq needRow[Needs.merenderoId] }
                        .singleOrNull()?.get(Merenderos.userId)
                    if (merenderoUserId != null) {
                        Notifications.insert {
                            it[Notifications.id] = notiId
                            it[Notifications.userId] = merenderoUserId
                            it[Notifications.type] = "DONANTE_CONFIRMADO"
                            it[Notifications.message] = "${donorNombre ?: "Un donante"} confirmó que va a ayudar con \"$needTitle\""
                            it[Notifications.relatedNeedId] = needId
                            it[Notifications.relatedUserId] = userId
                            it[Notifications.isRead] = false
                            it[Notifications.createdAt] = LocalDateTime.now()
                        }
                    }
                }

                call.respond(
                    HttpStatusCode.Created,
                    ConfirmNeedResponse(ok = true, donorsOnWay = donorsOnWay)
                )
            }
            get("/{id}/confirmed") {
                val userId = call.principal<JWTPrincipal>()
                    ?.payload?.subject
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)

                val needId = call.parameters["id"]
                    ?: return@get call.respond(HttpStatusCode.BadRequest)

                val yaConfirmo = transaction {
                    DonorConfirmations.selectAll()
                        .where {
                            (DonorConfirmations.needId eq needId) and
                                    (DonorConfirmations.donorId eq userId)
                        }
                        .singleOrNull() != null
                }

                call.respond(mapOf("confirmed" to yaConfirmo))
            }

            put("/{id}") {

                val userId =
                    call.principal<JWTPrincipal>()
                        ?.payload
                        ?.subject
                        ?: return@put call.respond(HttpStatusCode.Unauthorized)

                val needId =
                    call.parameters["id"]
                        ?: return@put call.respond(HttpStatusCode.BadRequest)

                val request = call.receive<UpdateNeedRequest>()

                val merenderoRow = transaction {
                    Merenderos.selectAll()
                        .where { Merenderos.userId eq userId }
                        .singleOrNull()
                }

                if (merenderoRow == null) {
                    return@put call.respond(
                        HttpStatusCode.Forbidden,
                        mapOf("error" to "Solo merenderos pueden editar necesidades")
                    )
                }

                val merenderoId = merenderoRow[Merenderos.id]

                val needRow = transaction {
                    Needs.selectAll()
                        .where { Needs.id eq needId }
                        .singleOrNull()
                }

                if (needRow == null) {
                    return@put call.respond(HttpStatusCode.NotFound)
                }

                if (needRow[Needs.merenderoId] != merenderoId) {
                    return@put call.respond(HttpStatusCode.Forbidden)
                }

                val itemsJson = Json.encodeToString(request.items)

                transaction {
                    Needs.update({ Needs.id eq needId }) {

                        it[title] = request.title
                        it[description] = request.description
                        it[type] = request.type
                        it[urgency] = request.urgency
                        it[items] = itemsJson
                    }
                }

                call.respond(
                    HttpStatusCode.OK,
                    mapOf("ok" to true)
                )
            }

            delete("/{id}") {

                val userId =
                    call.principal<JWTPrincipal>()
                        ?.payload
                        ?.subject
                        ?: return@delete call.respond(HttpStatusCode.Unauthorized)

                val needId =
                    call.parameters["id"]
                        ?: return@delete call.respond(HttpStatusCode.BadRequest)

                val merenderoRow = transaction {
                    Merenderos.selectAll()
                        .where { Merenderos.userId eq userId }
                        .singleOrNull()
                }

                if (merenderoRow == null) {
                    return@delete call.respond(HttpStatusCode.Forbidden)
                }

                val merenderoId = merenderoRow[Merenderos.id]

                val needRow = transaction {
                    Needs.selectAll()
                        .where { Needs.id eq needId }
                        .singleOrNull()
                }

                if (needRow == null) {
                    return@delete call.respond(HttpStatusCode.NotFound)
                }

                if (needRow[Needs.merenderoId] != merenderoId) {
                    return@delete call.respond(HttpStatusCode.Forbidden)
                }

                transaction {

                    DonorConfirmations.deleteWhere {
                        DonorConfirmations.needId eq needId
                    }

                    Needs.deleteWhere {
                        Needs.id eq needId
                    }

                    exec(
                        """
            UPDATE merenderos
            SET active_needs = GREATEST(active_needs - 1, 0)
            WHERE id = ?
            """.trimIndent(),
                        listOf(Merenderos.id.columnType to merenderoId)
                    )
                }

                call.respond(
                    HttpStatusCode.OK,
                    mapOf("ok" to true)
                )
            }

            // Historial del DONANTE — todas sus confirmaciones ya cubiertas
            get("/donations/history") {
                val userId = call.principal<JWTPrincipal>()?.payload?.subject
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)


                val historial = transaction {

                    val rows = DonorConfirmations
                        .innerJoin(Needs, { DonorConfirmations.needId }, { Needs.id })
                        .innerJoin(Merenderos, { Needs.merenderoId }, { Merenderos.id })
                        .selectAll()
                        .where {
                            (DonorConfirmations.donorId eq userId) and
                                    (Needs.isCovered eq true)
                        }

                    rows.map { row ->

                        println(
                            "donor=${row[DonorConfirmations.donorId]}, " +
                                    "need=${row[Needs.id]}, " +
                                    "covered=${row[Needs.isCovered]}"
                        )

                        DonationHistoryDto(
                            needTitle = row[Needs.title],
                            needType = row[Needs.type],
                            merenderoName = row[Merenderos.name],
                            daysAgo = java.time.Duration.between(
                                row[Needs.createdAt],
                                LocalDateTime.now()
                            ).toDays().toInt()
                        )
                    }
                }

                call.respond(HttpStatusCode.OK, historial)
            }// Historial del MERENDERO — todas sus necesidades cubiertas
            get("/history") {
                val userId = call.principal<JWTPrincipal>()?.payload?.subject
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)

                val merenderoRow = transaction {
                    Merenderos.selectAll().where { Merenderos.userId eq userId }.singleOrNull()
                } ?: return@get call.respond(HttpStatusCode.Forbidden)

                val historial = transaction {
                    Needs.selectAll()
                        .where {
                            (Needs.merenderoId eq merenderoRow[Merenderos.id]) and
                                    (Needs.isCovered eq true)
                        }
                        .map { row ->
                            NeedHistoryDto(
                                title = row[Needs.title],
                                type = row[Needs.type],
                                daysAgo = java.time.Duration.between(
                                    row[Needs.createdAt],
                                    LocalDateTime.now()
                                ).toDays().toInt()
                            )
                        }
                }
                call.respond(HttpStatusCode.OK, historial)
            }
        }
    }
}
