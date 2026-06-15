package com.tnt.donarya.backend.routes

import com.tnt.donarya.backend.database.Notifications
import com.tnt.donarya.backend.models.NotificationDto
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun Routing.notificationRoutes() {
    route("/api/notifications") {
        authenticate("auth-jwt") {
            get {
                val userId = call.principal<JWTPrincipal>()?.payload?.subject
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)

                val notis = transaction {
                    Notifications.selectAll()
                        .where { Notifications.userId eq userId }
                        .orderBy(Notifications.createdAt, SortOrder.DESC)
                        .limit(50)
                        .map { it.toDto() }
                }
                call.respond(notis)
            }

            put("/{id}/read") {
                val userId = call.principal<JWTPrincipal>()?.payload?.subject
                    ?: return@put call.respond(HttpStatusCode.Unauthorized)
                val notiId = call.parameters["id"]
                    ?: return@put call.respond(HttpStatusCode.BadRequest)

                transaction {
                    Notifications.update({
                        (Notifications.id eq notiId) and (Notifications.userId eq userId)
                    }) {
                        it[isRead] = true
                    }
                }
                call.respond(HttpStatusCode.OK, mapOf("ok" to true))
            }
        }
    }
}

private fun ResultRow.toDto() = NotificationDto(
    id = this[Notifications.id],
    userId = this[Notifications.userId],
    type = this[Notifications.type],
    message = this[Notifications.message],
    relatedNeedId = this[Notifications.relatedNeedId],
    relatedUserId = this[Notifications.relatedUserId],
    isRead = this[Notifications.isRead],
    createdAt = this[Notifications.createdAt].format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
)
