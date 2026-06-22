package com.tnt.donarya.backend.routes

import at.favre.lib.crypto.bcrypt.BCrypt
import com.tnt.donarya.backend.database.Merenderos
import com.tnt.donarya.backend.database.Users
import com.tnt.donarya.backend.middleware.JwtConfig
import com.tnt.donarya.backend.models.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Routing.authRoutes() {
    route("/api/auth") {
        post("/register") {
            val req = call.receive<RegisterRequest>()

            if (req.nombre.isBlank() || req.email.isBlank() || req.password.isBlank()) {
                return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Campos obligatorios vacíos"))
            }
            if (!req.email.contains("@")) {
                return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Email inválido"))
            }
            if (req.password.length < 6) {
                return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "La contraseña debe tener al menos 6 caracteres"))
            }

            val rol = req.rol.uppercase()
            if (rol == "MERENDERO") {
                if (req.whatsapp.isNullOrBlank()) {
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "WhatsApp es obligatorio para merenderos"))
                }
                if (req.nombreComedor.isNullOrBlank()) {
                    return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Nombre del comedor es obligatorio"))
                }
            }

            val existing = transaction {
                Users.selectAll().where { Users.email eq req.email }.singleOrNull()
            }
            if (existing != null) {
                return@post call.respond(HttpStatusCode.Conflict, mapOf("error" to "El email ya está registrado"))
            }

            val id = "u_${System.currentTimeMillis()}"
            val hash = BCrypt.withDefaults().hashToString(12, req.password.toCharArray())

            transaction {
                Users.insert {
                    it[Users.id] = id
                    it[Users.nombre] = req.nombre
                    it[Users.email] = req.email
                    it[Users.passwordHash] = hash
                    it[Users.rol] = rol
                    it[Users.nombreComedor] = req.nombreComedor
                    it[Users.whatsapp] = req.whatsapp
                    it[Users.direccion] = req.direccion
                    it[Users.createdAt] = LocalDateTime.now()
                }

                if (rol == "MERENDERO") {
                    Merenderos.insert {
                        it[Merenderos.id] = id
                        it[Merenderos.userId] = id
                        it[Merenderos.name] = req.nombreComedor ?: req.nombre
                        it[Merenderos.coordinator] = req.nombre
                        it[Merenderos.whatsapp] = req.whatsapp ?: ""
                        it[Merenderos.address] = req.direccion ?: ""
                        it[Merenderos.neighborhood] = ""
                        it[Merenderos.kidsCount] = 0
                        it[Merenderos.activeNeeds] = 0
                        it[Merenderos.coveredNeeds] = 0
                        it[Merenderos.isVerified] = false
                        it[Merenderos.latitude] = req.latitude
                        it[Merenderos.longitude] = req.longitude
                        it[Merenderos.createdAt] = LocalDateTime.now()
                    }
                }
            }

            val token = JwtConfig.generateToken(id, req.email, rol)
            call.respond(HttpStatusCode.Created, AuthResponse(
                token = token,
                user = UserDto(
                    id = id,
                    nombre = req.nombre,
                    email = req.email,
                    rol = rol,
                    nombreComedor = req.nombreComedor,
                    whatsapp = req.whatsapp,
                    direccion = req.direccion,
                    merenderoId = if (rol == "MERENDERO") id else null
                )
            ))
        }

        post("/login") {
            val req = call.receive<LoginRequest>()
            val row = transaction {
                Users.selectAll().where { Users.email eq req.email }.singleOrNull()
            }

            if (row == null) {
                return@post call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Credenciales inválidas"))
            }

            val hash = row[Users.passwordHash]
            val verified = BCrypt.verifyer().verify(req.password.toCharArray(), hash).verified
            if (!verified) {
                return@post call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Credenciales inválidas"))
            }

            val id = row[Users.id]
            val rol = row[Users.rol]
            val token = JwtConfig.generateToken(id, row[Users.email], rol)

            call.respond(HttpStatusCode.OK, AuthResponse(
                token = token,
                user = UserDto(
                    id = id,
                    nombre = row[Users.nombre],
                    email = row[Users.email],
                    rol = rol,
                    nombreComedor = row[Users.nombreComedor],
                    whatsapp = row[Users.whatsapp],
                    direccion = row[Users.direccion],
                    merenderoId = if (rol == "MERENDERO") id else null
                )
            ))
        }

        authenticate("auth-jwt") {
            put("/me") {
                val userId = call.principal<JWTPrincipal>()
                    ?.payload?.subject
                    ?: return@put call.respond(HttpStatusCode.Unauthorized)

                val req = call.receive<UpdateProfileRequest>()

                if (req.nombre.isBlank())
                    return@put call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "El nombre no puede estar vacío")
                    )
                if (!req.email.contains("@"))
                    return@put call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Email inválido")
                    )

                // Verificar que el email nuevo no lo tenga otro usuario
                val emailTaken = transaction {
                    Users.selectAll()
                        .where { (Users.email eq req.email) and (Users.id neq userId) }
                        .singleOrNull()
                }
                if (emailTaken != null)
                    return@put call.respond(
                        HttpStatusCode.Conflict,
                        mapOf("error" to "El email ya está en uso")
                    )

                val updatedRow = transaction {
                    Users.update({ Users.id eq userId }) {
                        it[nombre] = req.nombre
                        it[email] = req.email
                        it[nombreComedor] = req.nombreComedor
                        it[whatsapp] = req.whatsapp
                        it[direccion] = req.direccion
                    }

                    // Si es merendero, actualizar también la tabla Merenderos
                    val rol = Users.selectAll()
                        .where { Users.id eq userId }
                        .single()[Users.rol]

                    if (rol == "MERENDERO") {
                        Merenderos.update({ Merenderos.userId eq userId }) {
                            it[name] = req.nombreComedor ?: req.nombre
                            it[coordinator] = req.nombre
                            it[Merenderos.whatsapp] = req.whatsapp ?: ""
                            it[address] = req.direccion ?: ""
                        }
                    }

                    Users.selectAll().where { Users.id eq userId }.single()
                }

                call.respond(
                    HttpStatusCode.OK,
                    UserDto(
                        id = userId,
                        nombre = updatedRow[Users.nombre],
                        email = updatedRow[Users.email],
                        rol = updatedRow[Users.rol],
                        nombreComedor = updatedRow[Users.nombreComedor],
                        whatsapp = updatedRow[Users.whatsapp],
                        direccion = updatedRow[Users.direccion],
                        merenderoId = if (updatedRow[Users.rol] == "MERENDERO") userId else null
                    )
                )
            }
        }
    }
}
