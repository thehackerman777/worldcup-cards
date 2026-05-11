package com.wcapp.android.data.remote

import com.wcapp.android.security.SecurePrefs
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * ApiService — lanza excepciones con mensajes en español.
 * ViewModels usan try/catch para manejar errores.
 */
class ApiService(private val prefs: SecurePrefs) {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true }

    private val client = HttpClient {
        install(ContentNegotiation) { json(this@ApiService.json) }
        install(HttpTimeout) { requestTimeoutMillis = 15_000; connectTimeoutMillis = 8_000; socketTimeoutMillis = 15_000 }
        defaultRequest { contentType(ContentType.Application.Json); accept(ContentType.Application.Json) }
    }

    val baseUrl: String get() = prefs.serverUrl.trimEnd('/')
    fun HttpRequestBuilder.auth() { prefs.accessToken?.let { header(HttpHeaders.Authorization, "Bearer $it") } }

    private suspend inline fun <reified T> get(url: String, auth: Boolean = false): T {
        val r = client.get(url) { if (auth) auth() }
        return r.body()
    }

    private suspend inline fun <reified T> post(url: String, body: Any, auth: Boolean = false): T {
        val r = client.post(url) { if (auth) auth(); setBody(body) }
        return r.body()
    }

    private suspend inline fun <reified T> put(url: String, auth: Boolean = false): T {
        val r = client.put(url) { if (auth) auth() }
        return r.body()
    }

    // ── Auth ──────────────────────────────────────────────
    suspend fun login(username: String, password: String) = post<ApiResponse>("$baseUrl/api/v1/auth/login", mapOf("username" to username, "password" to password))
    suspend fun register(u: String, e: String, p: String, dn: String?) = post<ApiResponse>("$baseUrl/api/v1/auth/register", mapOf("username" to u, "email" to e, "password" to p, "displayName" to (dn ?: u)))

    // ── Cards ─────────────────────────────────────────────
    suspend fun getCards(page: Int = 0, size: Int = 50, team: String? = null): CardsResponse {
        return client.get("$baseUrl/api/v1/cards") { auth(); parameter("page", page); parameter("size", size); team?.let { parameter("team", it) } }.body()
    }
    suspend fun getCard(id: String) = get<CardResponse>("$baseUrl/api/v1/cards/$id", auth = true)

    // ── Album ─────────────────────────────────────────────
    suspend fun getAlbum() = get<AlbumResponse>("$baseUrl/api/v1/album", auth = true)
    suspend fun getRepeatedCards() = get<List<UserCardResponse>>("$baseUrl/api/v1/album/repeated", auth = true)

    // ── Exchanges ─────────────────────────────────────────
    suspend fun getExchanges() = get<ExchangesResponse>("$baseUrl/api/v1/exchanges", auth = true)
    suspend fun getAvailableExchanges() = get<ExchangesResponse>("$baseUrl/api/v1/exchanges/available", auth = true)
    suspend fun createExchange(req: CreateExchangeRequest) = post<ExchangeResponse>("$baseUrl/api/v1/exchanges", req, auth = true)
    suspend fun acceptExchange(id: String) = put<ExchangeResponse>("$baseUrl/api/v1/exchanges/$id/accept", auth = true)
    suspend fun rejectExchange(id: String) = put<ExchangeResponse>("$baseUrl/api/v1/exchanges/$id/reject", auth = true)
    suspend fun completeExchange(id: String) = put<ExchangeResponse>("$baseUrl/api/v1/exchanges/$id/complete", auth = true)

    // ── Panini ────────────────────────────────────────────
    suspend fun paniniLocalLookup(nickname: String)  = get<PaniniLookupResponse>("$baseUrl/api/v1/panini/local/$nickname")
    suspend fun paniniExternalLookup(nickname: String) = get<PaniniLookupResponse>("$baseUrl/api/v1/panini/external/$nickname")
    suspend fun paniniSearch(query: String): PaniniSearchRoot {
        return client.get("$baseUrl/api/v1/panini/local/search") { parameter("q", query) }.body()
    }

    fun close() { client.close() }
}

// ── DTOs (sin cambios) ──────────────────────────────────
@kotlinx.serialization.Serializable
data class ApiResponse(val token: String = "", val refreshToken: String = "", val expiresIn: Long = 0, val user: UserResponse? = null)
@kotlinx.serialization.Serializable
data class UserResponse(val id: String = "", val username: String = "", val email: String = "", val displayName: String? = null, val avatarUrl: String? = null, val role: String = "USER")
@kotlinx.serialization.Serializable
data class CardResponse(val id: String = "", val name: String = "", val cardNumber: Int = 0, val team: String = "", val position: String? = null, val imageUrl: String? = null, val rarity: String = "COMMON", val description: String? = null, val year: Int = 2026, val edition: String = "")
@kotlinx.serialization.Serializable
data class CardsResponse(val cards: List<CardResponse> = emptyList(), val totalPages: Int = 0, val totalElements: Long = 0, val currentPage: Int = 0, val pageSize: Int = 50)
@kotlinx.serialization.Serializable
data class AlbumResponse(val userId: String = "", val username: String = "", val totalCards: Int = 0, val albumCards: Long = 0, val repeatedCards: Int = 0, val completionPercentage: Double = 0.0, val cards: List<UserCardResponse> = emptyList())
@kotlinx.serialization.Serializable
data class UserCardResponse(val id: String = "", val card: CardResponse, val quantity: Int = 1, val isInAlbum: Boolean = false, val isRepeated: Boolean = false, val tradeable: Boolean = false)
@kotlinx.serialization.Serializable
data class ExchangesResponse(val exchanges: List<ExchangeResponse> = emptyList())
@kotlinx.serialization.Serializable
data class ExchangeResponse(val id: String = "", val requester: ExchangeUserInfo = ExchangeUserInfo(), val receiver: ExchangeUserInfo = ExchangeUserInfo(), val status: String = "PENDING", val message: String? = null, val offeredCards: List<ExchangeItemResponse> = emptyList(), val requestedCards: List<ExchangeItemResponse> = emptyList(), val createdAt: String = "", val updatedAt: String? = null)
@kotlinx.serialization.Serializable
data class ExchangeUserInfo(val id: String = "", val username: String = "", val displayName: String? = null)
@kotlinx.serialization.Serializable
data class ExchangeItemResponse(val id: String = "", val card: CardResponse, val quantity: Int = 1, val offeredBy: String = "")
@kotlinx.serialization.Serializable
data class CreateExchangeRequest(val receiverId: String, val message: String? = null, val offeredCards: List<ExchangeCardEntry>, val requestedCards: List<ExchangeCardEntry>)
@kotlinx.serialization.Serializable
data class ExchangeCardEntry(val cardId: String, val quantity: Int = 1)
@kotlinx.serialization.Serializable
data class PaniniLookupResponse(val nickname: String = "", val duplicates: List<String> = emptyList(), val missing: List<String> = emptyList(), val completion: Int = 0, val lastSync: String = "", val profileFound: Boolean = true, val fromCache: Boolean = false)
@kotlinx.serialization.Serializable
data class PaniniSearchRoot(val results: List<PaniniSearchItem> = emptyList(), val total: Int = 0)
@kotlinx.serialization.Serializable
data class PaniniSearchItem(val nickname: String = "", val displayName: String? = null, val completion: Int = 0, val duplicateCount: Int = 0, val lastSync: String? = null)
