package com.wcapp.android.data.remote

import com.wcapp.android.security.SecurePrefs
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Cliente API con tipos concretos (no Result<T>).
 * Los errores se mapean a ApiError para manejo uniforme.
 */

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val code: ErrorCode, val message: String, val detail: String? = null) : ApiResult<Nothing>()

    /** Compatibilidad: .onSuccess {} y .onSuccess { valor -> } */
    inline fun onSuccess(action: () -> Unit): ApiResult<T> {
        if (this is Success) action()
        return this
    }
    inline fun onSuccess(action: (T) -> Unit): ApiResult<T> {
        if (this is Success) action(data)
        return this
    }
    /** Compatibilidad: .onFailure { error -> } */
    inline fun onFailure(action: (Error) -> Unit): ApiResult<T> {
        if (this is Error) action(this)
        return this
    }
    val isSuccess: Boolean get() = this is Success
}

enum class ErrorCode {
    NETWORK,         // Sin conexion / timeout
    SERVER,          // Error 5xx
    NOT_FOUND,       // 404
    UNAUTHORIZED,    // 401
    VALIDATION,      // 400
    PARSE_ERROR,     // JSON no se puede leer
    UNKNOWN
}

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
            level = LogLevel.NONE
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 15_000
            connectTimeoutMillis = 8_000
            socketTimeoutMillis = 15_000
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

    /** Ejecuta un request y mapea errores a ApiResult */
    private suspend inline fun <reified T> request(
        crossinline block: suspend () -> T
    ): ApiResult<T> {
        return try {
            val data = block()
            ApiResult.Success(data)
        } catch (e: ClientRequestException) {
            val status = e.response.status
            val body = try { e.response.bodyAsText() } catch (_: Exception) { null }
            when (status) {
                HttpStatusCode.Unauthorized -> ApiResult.Error(ErrorCode.UNAUTHORIZED, "Credenciales inválidas", body)
                HttpStatusCode.NotFound -> ApiResult.Error(ErrorCode.NOT_FOUND, "Recurso no encontrado", body)
                HttpStatusCode.BadRequest -> ApiResult.Error(ErrorCode.VALIDATION, "Datos inválidos", body)
                HttpStatusCode.Gone -> ApiResult.Error(ErrorCode.VALIDATION, "Datos expirados, resincroniza", body)
                HttpStatusCode.ServiceUnavailable -> ApiResult.Error(ErrorCode.SERVER, "Servicio no disponible", body)
                else -> ApiResult.Error(ErrorCode.SERVER, "Error del servidor (${status.value})", body)
            }
        } catch (e: HttpRequestTimeoutException) {
            ApiResult.Error(ErrorCode.NETWORK, "Tiempo de espera agotado. Verifica la URL del servidor.")
        } catch (e: java.net.ConnectException) {
            ApiResult.Error(ErrorCode.NETWORK, "No se pudo conectar al servidor. Verifica la URL: $baseUrl")
        } catch (e: java.net.UnknownHostException) {
            ApiResult.Error(ErrorCode.NETWORK, "Host desconocido. Revisa la URL: $baseUrl")
        } catch (e: kotlinx.serialization.SerializationException) {
            ApiResult.Error(ErrorCode.PARSE_ERROR, "Error al leer respuesta del servidor", e.message)
        } catch (e: Exception) {
            ApiResult.Error(ErrorCode.NETWORK, "Error de conexión: ${e.message?.take(80) ?: "desconocido"}")
        }
    }

    // ── Auth ──────────────────────────────────────────────
    suspend fun login(username: String, password: String): ApiResult<ApiResponse> = request {
        httpClient.post("$baseUrl/api/v1/auth/login") {
            setBody(mapOf("username" to username, "password" to password))
        }.body<ApiResponse>()
    }

    suspend fun register(username: String, email: String, password: String, displayName: String?): ApiResult<ApiResponse> = request {
        httpClient.post("$baseUrl/api/v1/auth/register") {
            setBody(mapOf("username" to username, "email" to email, "password" to password, "displayName" to (displayName ?: username)))
        }.body<ApiResponse>()
    }

    // ── Cards ─────────────────────────────────────────────
    suspend fun getCards(page: Int = 0, size: Int = 50, team: String? = null): ApiResult<CardsResponse> = request {
        httpClient.get("$baseUrl/api/v1/cards") {
            auth()
            parameter("page", page)
            parameter("size", size)
            team?.let { parameter("team", it) }
        }.body<CardsResponse>()
    }

    suspend fun getCard(id: String): ApiResult<CardResponse> = request {
        httpClient.get("$baseUrl/api/v1/cards/$id") {
            auth()
        }.body<CardResponse>()
    }

    // ── Album ─────────────────────────────────────────────
    suspend fun getAlbum(): ApiResult<AlbumResponse> = request {
        httpClient.get("$baseUrl/api/v1/album") { auth() }.body<AlbumResponse>()
    }

    suspend fun getRepeatedCards(): ApiResult<List<UserCardResponse>> = request {
        httpClient.get("$baseUrl/api/v1/album/repeated") { auth() }.body<List<UserCardResponse>>()
    }

    // ── Exchanges ─────────────────────────────────────────
    suspend fun getExchanges(): ApiResult<ExchangesResponse> = request {
        httpClient.get("$baseUrl/api/v1/exchanges") { auth() }.body<ExchangesResponse>()
    }

    suspend fun getAvailableExchanges(): ApiResult<ExchangesResponse> = request {
        httpClient.get("$baseUrl/api/v1/exchanges/available") { auth() }.body<ExchangesResponse>()
    }

    // ── Panini ────────────────────────────────────────────
    suspend fun paniniLocalLookup(nickname: String): ApiResult<PaniniLookupResponse> = request {
        httpClient.get("$baseUrl/api/v1/panini/local/$nickname").body<PaniniLookupResponse>()
    }

    suspend fun paniniExternalLookup(nickname: String): ApiResult<PaniniLookupResponse> = request {
        httpClient.get("$baseUrl/api/v1/panini/external/$nickname").body<PaniniLookupResponse>()
    }

    suspend fun paniniSearch(query: String): ApiResult<PaniniSearchRoot> = request {
        httpClient.get("$baseUrl/api/v1/panini/local/search") {
            parameter("q", query)
        }.body<PaniniSearchRoot>()
    }

    fun close() {
        httpClient.close()
    }
}

// ── Response DTOs (sin cambios) ────────────────────────────
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
