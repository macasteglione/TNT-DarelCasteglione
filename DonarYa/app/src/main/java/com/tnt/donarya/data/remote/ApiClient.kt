package com.tnt.donarya.data.remote

import com.google.gson.Gson
import com.tnt.donarya.data.remote.dto.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import com.tnt.donarya.BuildConfig
object ApiClient {
    private val BASE_URL = if (BuildConfig.DEBUG)
        "http://192.168.0.10:8080/api"   // teléfono físico
    else
        "http://10.0.2.2:8080/api"        // emulador

    private val gson = Gson()

    private val errorParser = Gson()

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            gson(contentType = ContentType.Application.Json)
        }
        install(io.ktor.client.plugins.HttpTimeout) {
            requestTimeoutMillis = 15000   // 15 segundos
            connectTimeoutMillis = 10000   // 10 segundos
            socketTimeoutMillis  = 15000   // 15 segundos
        }
    }

    private suspend fun HttpRequestBuilder.withAuth() {
        val token = TokenStorage.getToken()
        if (token != null) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }

    private suspend fun HttpResponse.ensureSuccess() {
        if (!status.isSuccess()) {
            val body = bodyAsText()
            val msg = try {
                errorParser.fromJson(body, Map::class.java)["error"] as? String
            } catch (_: Exception) { null }
            throw Exception(msg ?: "Error del servidor: ${status.value}")
        }
    }

    // Auth
    suspend fun register(req: RegisterRequestDto): Result<AuthResponseDto> = runCatching {
        val response = client.post("$BASE_URL/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(gson.toJson(req))
        }
        response.ensureSuccess()
        gson.fromJson(response.bodyAsText(), AuthResponseDto::class.java)
    }

    suspend fun login(req: LoginRequestDto): Result<AuthResponseDto> = runCatching {
        val response = client.post("$BASE_URL/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(gson.toJson(req))
        }
        response.ensureSuccess()
        gson.fromJson(response.bodyAsText(), AuthResponseDto::class.java)
    }

    // Merenderos
    suspend fun getMerenderos(): Result<List<MerenderoDto>> = runCatching {
        val response = client.get("$BASE_URL/merenderos")
        response.ensureSuccess()
        gson.fromJson(response.bodyAsText(), Array<MerenderoDto>::class.java).toList()
    }

    suspend fun getMerenderoDetail(id: String): Result<MerenderoWithNeedsDto> = runCatching {
        val response = client.get("$BASE_URL/merenderos/$id")
        response.ensureSuccess()
        gson.fromJson(response.bodyAsText(), MerenderoWithNeedsDto::class.java)
    }

    suspend fun updateMerendero(id: String, req: UpdateMerenderoRequestDto): Result<Unit> = runCatching {
        val response = client.put("$BASE_URL/merenderos/$id") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(gson.toJson(req))
        }
        response.ensureSuccess()
    }

    // Needs
    suspend fun createNeed(req: CreateNeedRequestDto): Result<NeedItemDto> = runCatching {
        val response = client.post("$BASE_URL/needs") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(gson.toJson(req))
        }
        response.ensureSuccess()
        gson.fromJson(response.bodyAsText(), NeedItemDto::class.java)
    }

    suspend fun markNeedCovered(needId: String): Result<Unit> = runCatching {
        val response = client.put("$BASE_URL/needs/$needId/cover") {
            withAuth()
        }
        response.ensureSuccess()
    }

    suspend fun confirmNeed(needId: String): Result<ConfirmNeedResponseDto> = runCatching {
        val response = client.post("$BASE_URL/needs/$needId/confirm") {
            withAuth()
        }
        response.ensureSuccess()
        gson.fromJson(response.bodyAsText(), ConfirmNeedResponseDto::class.java)
    }

    suspend fun checkIfConfirmed(needId: String): Boolean = runCatching {
        val response = client.get("$BASE_URL/needs/$needId/confirmed") {
            withAuth()
        }
        val json = response.bodyAsText()
        gson.fromJson(json, Map::class.java)["confirmed"] as? Boolean ?: false
    }.getOrDefault(false)


    suspend fun deleteNeed(needId: String): Result<Unit> = runCatching {
        val response = client.delete("$BASE_URL/needs/$needId") {
            withAuth()
        }
        response.ensureSuccess()
    }

    suspend fun updateNeed(
        needId: String,
        req: UpdateNeedRequestDto
    ): Result<Unit> = runCatching {
        val response = client.put("$BASE_URL/needs/$needId") {
            withAuth()
            contentType(ContentType.Application.Json)
            setBody(gson.toJson(req))
        }
        response.ensureSuccess()
    }
}
