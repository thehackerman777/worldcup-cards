package com.wcapp.android.data.remote

import com.wcapp.android.security.SecurePrefs
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Cliente API para comunicación con el backend Spring Boot.
 * Usa la URL configurada desde SecurePrefs (dinámica, no hardcodeada).
 */
class ApiService(
    private val prefs: SecurePrefs
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = false
    }

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(this@ApiService.json)
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.HEADERS
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 30_000
        }
        defaultRequest {
            contentType(ContentType.Application.Json)
            accept(ContentType.Application.Json)
        }
    }

    private val baseUrl: String get() = prefs.serverUrl.trimEnd('/')

    private fun HttpRequestBuilder.auth() {
        prefs.accessToken?.let { token ->
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }

    // ── Auth ──────────────────────────────────────────────
    suspend fun login(username: String, password: String): ApiResponse = runCatching {
        httpClient.post("$baseUrl/api/v1/auth/login") {
            setBody(mapOf("username" to username, "password" to password))
        }.body<ApiResponse>()
    }

    suspend fun register(username: String, email: String, password: String, displayName: String?): ApiResponse = runCatching {
        httpClient.post("$baseUrl/api/v1/auth/register") {
            setBody(
                mapOf(
                    "username" to username,
                    "email" to email,
                    "password" to password,
                    "displayName" to (displayName ?: username)
                )
            )
        }.body<ApiResponse>()
    }

    // ── Cards ─────────────────────────────────────────────
    suspend fun getCards(page: Int = 0, size: Int = 50, team: String? = null): CardsResponse = runCatching {
        httpClient.get("$baseUrl/api/v1/cards") {
            auth()
            parameter("page", page)
            parameter("size", size)
            team?.let { parameter("team", it) }
        }.body<CardsResponse>()
    }

    suspend fun getCard(id: String): CardResponse = runCatching {
        httpClient.get("$baseUrl/api/v1/cards/$id") {
            auth()
        }.body<CardResponse>()
    }

    // ── Album ─────────────────────────────────────────────
    suspend fun getAlbum(): AlbumResponse = runCatching {
        httpClient.get("$baseUrl/api/v1/album") {
            auth()
        }.body<AlbumResponse>()
    }

    // ── Repeated ──────────────────────────────────────────
    suspend fun getRepeatedCards(): List<UserCardResponse> = runCatching {
        httpClient.get("$baseUrl/api/v1/album/repeated") {
            auth()
        }.body<List<UserCardResponse>>()
    }

    // ── Exchanges ─────────────────────────────────────────
    suspend fun getExchanges(): ExchangesResponse = runCatching {
        httpClient.get("$baseUrl/api/v1/exchanges") {
            auth()
        }.body<ExchangesResponse>()
    }

    suspend fun getAvailableExchanges(): ExchangesResponse = runCatching {
        httpClient.get("$baseUrl/api/v1/exchanges/available") {
            auth()
        }.body<ExchangesResponse>()
    }

    suspend fun createExchange(request: CreateExchangeRequest): ExchangeResponse = runCatching {
        httpClient.post("$baseUrl/api/v1/exchanges") {
            auth()
            setBody(request)
        }.body<ExchangeResponse>()
    }

    suspend fun acceptExchange(id: String): ExchangeResponse = runCatching {
        httpClient.put("$baseUrl/api/v1/exchanges/$id/accept") {
            auth()
        }.body<ExchangeResponse>()
    }

    suspend fun rejectExchange(id: String): ExchangeResponse = runCatching {
        httpClient.put("$baseUrl/api/v1/exchanges/$id/reject") {
            auth()
        }.body<ExchangeResponse>()
    }

    suspend fun completeExchange(id: String): ExchangeResponse = runCatching {
        httpClient.put("$baseUrl/api/v1/exchanges/$id/complete") {
            auth()
        }.body<ExchangeResponse>()
    }

    // ── Panini ────────────────────────────────────────────
    suspend fun paniniLookup(nickname: String): PaniniLookupResponse = runCatching {
        httpClient.get("$baseUrl/api/v1/panini/user/$nickname") {
            // Public endpoint, no auth needed
        }.body<PaniniLookupResponse>()
    }

    suspend fun paniniSearch(query: String): PaniniSearchRoot = runCatching {
        httpClient.get("$baseUrl/api/v1/panini/search") {
            parameter("q", query)
        }.body<PaniniSearchRoot>()
    }

    suspend fun paniniSync(request: PaniniSyncClientRequest): PaniniSyncClientResponse = runCatching {
        httpClient.post("$baseUrl/api/v1/panini/user/sync") {
            auth()
            setBody(request)
        }.body<PaniniSyncClientResponse>()
    }

    fun close() {
        httpClient.close()
    }
}

// ── Response DTOs (serialización directa) ────────────────
@kotlinx.serialization.Serializable
data class ApiResponse(
    val token: String = "",
    val refreshToken: String = "",
    val expiresIn: Long = 0,
    val user: UserResponse? = null
)

@kotlinx.serialization.Serializable
data class UserResponse(
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val role: String = "USER"
)

@kotlinx.serialization.Serializable
data class CardResponse(
    val id: String = "",
    val name: String = "",
    val cardNumber: Int = 0,
    val team: String = "",
    val position: String? = null,
    val imageUrl: String? = null,
    val rarity: String = "COMMON",
    val description: String? = null,
    val year: Int = 2026,
    val edition: String = ""
)

@kotlinx.serialization.Serializable
data class CardsResponse(
    val cards: List<CardResponse> = emptyList(),
    val totalPages: Int = 0,
    val totalElements: Long = 0,
    val currentPage: Int = 0,
    val pageSize: Int = 50
)

@kotlinx.serialization.Serializable
data class AlbumResponse(
    val userId: String = "",
    val username: String = "",
    val totalCards: Int = 0,
    val albumCards: Long = 0,
    val repeatedCards: Int = 0,
    val completionPercentage: Double = 0.0,
    val cards: List<UserCardResponse> = emptyList()
)

@kotlinx.serialization.Serializable
data class UserCardResponse(
    val id: String = "",
    val card: CardResponse,
    val quantity: Int = 1,
    val isInAlbum: Boolean = false,
    val isRepeated: Boolean = false,
    val tradeable: Boolean = false
)

@kotlinx.serialization.Serializable
data class ExchangesResponse(
    val exchanges: List<ExchangeResponse> = emptyList()
)

@kotlinx.serialization.Serializable
data class ExchangeResponse(
    val id: String = "",
    val requester: ExchangeUserInfo = ExchangeUserInfo(),
    val receiver: ExchangeUserInfo = ExchangeUserInfo(),
    val status: String = "PENDING",
    val message: String? = null,
    val offeredCards: List<ExchangeItemResponse> = emptyList(),
    val requestedCards: List<ExchangeItemResponse> = emptyList(),
    val createdAt: String = "",
    val updatedAt: String? = null
)

@kotlinx.serialization.Serializable
data class ExchangeUserInfo(
    val id: String = "",
    val username: String = "",
    val displayName: String? = null
)

@kotlinx.serialization.Serializable
data class ExchangeItemResponse(
    val id: String = "",
    val card: CardResponse,
    val quantity: Int = 1,
    val offeredBy: String = ""
)

@kotlinx.serialization.Serializable
data class CreateExchangeRequest(
    val receiverId: String,
    val message: String? = null,
    val offeredCards: List<ExchangeCardEntry>,
    val requestedCards: List<ExchangeCardEntry>
)

@kotlinx.serialization.Serializable
data class ExchangeCardEntry(
    val cardId: String,
    val quantity: Int = 1
)

// ── Panini DTOs ──────────────────────────────────────────
@kotlinx.serialization.Serializable
data class PaniniLookupResponse(
    val nickname: String = "",
    val duplicates: List<String> = emptyList(),
    val missing: List<String> = emptyList(),
    val completion: Int = 0,
    val lastSync: String = "",
    val profileFound: Boolean = true,
    val fromCache: Boolean = false
)

@kotlinx.serialization.Serializable
data class PaniniSearchRoot(
    val results: List<PaniniSearchItem> = emptyList(),
    val total: Int = 0
)

@kotlinx.serialization.Serializable
data class PaniniSearchItem(
    val nickname: String = "",
    val displayName: String? = null,
    val completion: Int = 0,
    val duplicateCount: Int = 0,
    val lastSync: String? = null
)

@kotlinx.serialization.Serializable
data class PaniniSyncClientRequest(
    val nickname: String,
    val duplicates: List<String> = emptyList(),
    val missing: List<String> = emptyList(),
    val completion: Int = 0,
    val totalCollection: Int = 0
)

@kotlinx.serialization.Serializable
data class PaniniSyncClientResponse(
    val nickname: String = "",
    val cardsSynced: Int = 0,
    val duplicatesFound: Int = 0,
    val missingFound: Int = 0,
    val completion: Int = 0,
    val syncedAt: String = ""
)
