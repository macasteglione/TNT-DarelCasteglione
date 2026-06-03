package com.tnt.donarya.backend.middleware

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.util.*

private const val SECRET = "DonarYa-JWT-Secret-2026-KeepItSafe!"
private const val ISSUER = "donarya-backend"
private const val VALIDITY_MINUTES = 1440L // 24h

object JwtConfig {
    val algorithm = Algorithm.HMAC256(SECRET)

    fun generateToken(userId: String, email: String, rol: String): String {
        return JWT.create()
            .withIssuer(ISSUER)
            .withSubject(userId)
            .withClaim("email", email)
            .withClaim("rol", rol)
            .withExpiresAt(Date(System.currentTimeMillis() + VALIDITY_MINUTES * 60 * 1000))
            .sign(algorithm)
    }
}

fun Application.jwtAuthConfig() {
    install(Authentication) {
        jwt("auth-jwt") {
            verifier(JwtConfig.algorithm.let { JWT.require(it).withIssuer(ISSUER).build() })
            validate { credential ->
                val userId = credential.payload.subject
                if (userId != null) JWTPrincipal(credential.payload) else null
            }
        }
    }
}
