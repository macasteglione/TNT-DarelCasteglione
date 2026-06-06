package com.tnt.donarya.data.remote

import com.google.gson.Gson
import com.tnt.donarya.domain.model.AddressSuggestion
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

object AddressRepository {

    private val client = HttpClient(OkHttp)

    private val gson = Gson()

    private data class NominatimResult(
        val display_name: String,
        val lat: String,
        val lon: String
    )

    suspend fun search(query: String): List<AddressSuggestion> {
        if (query.isBlank()) return emptyList()
        val response = client.get("https://nominatim.openstreetmap.org/search") {
            parameter("q", query)
            parameter("format", "json")
            parameter("limit", 5)
            parameter("addressdetails", 1)
            header("User-Agent", "DonarYa/Android/1.0")
        }
        val body = response.bodyAsText()
        val results = gson.fromJson(body, Array<NominatimResult>::class.java) ?: return emptyList()
        return results.map { AddressSuggestion(it.display_name, it.lat.toDouble(), it.lon.toDouble()) }
    }
}
