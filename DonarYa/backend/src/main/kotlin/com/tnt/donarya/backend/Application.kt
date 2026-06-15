package com.tnt.donarya.backend

import com.tnt.donarya.backend.database.initDatabase
import com.tnt.donarya.backend.database.DatabaseFactory
import com.tnt.donarya.backend.middleware.jwtAuthConfig
import com.tnt.donarya.backend.routes.authRoutes
import com.tnt.donarya.backend.routes.merenderoRoutes
import com.tnt.donarya.backend.routes.needRoutes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.response.*
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port, module = Application::module).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            prettyPrint = true
        })
    }

    install(CORS) {
        anyHost()
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, mapOf<String, String>("error" to (cause.message ?: "Error interno")))
        }
    }

    DatabaseFactory.init()
    initDatabase()

    jwtAuthConfig()

    routing {
        authRoutes()
        merenderoRoutes()
        needRoutes()
    }
}
