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
import com.tnt.donarya.backend.models.ConfirmNeedResponse
import com.tnt.donarya.backend.models.UpdateNeedRequest
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq


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

                transaction {
                    Needs.update({ Needs.id eq needId }) {
                        it[Needs.isCovered] = true
                    }
                    exec("UPDATE merenderos SET active_needs = GREATEST(active_needs - 1, 0), covered_needs = covered_needs + 1 WHERE id = ?", listOf(Merenderos.id.columnType to merenderoId))
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
        }
    }
}
