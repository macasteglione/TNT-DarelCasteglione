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
        }
    }
}
