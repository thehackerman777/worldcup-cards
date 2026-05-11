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
import kotlin.reflect.KClass

/**
 * ApiResult — tipo sellado para respuestas de API.
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val code: ErrorCode, val message: String, val detail: String? = null) : ApiResult<Nothing>()

    fun onSuccess(action: () -> Unit): ApiResult<T> {
        if (this is Success) action()
        return this
    }
    fun onSuccess(action: (T) -> Unit): ApiResult<T> {
        if (this is Success) action(data)
        return this
    }
    fun onFailure(action: (Error) -> Unit): ApiResult<T> {
        if (this is Error) action(this)
        return this
    }
    val isSuccess: Boolean get() = this is Success
}

enum class ErrorCode { NETWORK, SERVER, NOT_FOUND, UNAUTHORIZED, VALIDATION, PARSE_ERROR, UNKNOWN }

/**
 * Cliente API con manejo de errores global.
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
        install(ContentNegotiation) { json(this@ApiService.json) }
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

    /** Ejecuta un request y mapea errores */
    @Suppress("UNCHECKED_CAST")
    private suspend fun <T> request(
        kclass: KClass<T>,
        block: suspend () -> HttpResponse
    ): ApiResult<T> {
        return try {
            val response = block()
            val body = response.body<T>(json)
            ApiResult.Success(body)
        } catch (e: ClientRequestException) {
            val status = e.response.status
            val body = try { e.response.bodyAsText() } catch (_: Exception) { null }
            when (status) {
                HttpStatusCode.Unauthorized -> ApiResult.Error(ErrorCode.UNAUTHORIZED, "Credenciales inválidas", body)
                HttpStatusCode.NotFound -> ApiResult.Error(ErrorCode.NOT_FOUND, "Recurso no encontrado", body)
                HttpStatusCode.BadRequest -> ApiResult.Error(ErrorCode.VALIDATION, "Datos inválidos", body)
                HttpStatusCode.Gone -> ApiResult.Error(ErrorCode.VALIDATION, "Datos expirados", body)
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
    suspend fun login(username: String, password: String): ApiResult<ApiResponse> = request(ApiResponse::class) {
        httpClient.post("$baseUrl/api/v1/auth/login") {
            setBody(mapOf("username" to username, "password" to password))
        }
    }

    suspend fun register(u: String, e: String, p: String, dn: String?): ApiResult<ApiResponse> = request(ApiResponse::class) {
        httpClient.post("$baseUrl/api/v1/auth/register") {
            setBody(mapOf("username" to u, "email" to e, "password" to p, "displayName" to (dn ?: u)))
        }
    }

    // ── Cards ─────────────────────────────────────────────
    suspend fun getCards(page: Int = 0, size: Int = 50, team: String? = null): ApiResult<CardsResponse> = request(CardsResponse::class) {
        httpClient.get("$baseUrl/api/v1/cards") { auth(); parameter("page", page); parameter("size", size); team?.let { parameter("team", it) } }
    }

    suspend fun getCard(id: String): ApiResult<CardResponse> = request(CardResponse::class) {
        httpClient.get("$baseUrl/api/v1/cards/$id") { auth() }
    }

    // ── Album ─────────────────────────────────────────────
    suspend fun getAlbum(): ApiResult<AlbumResponse> = request(AlbumResponse::class) {
        httpClient.get("$baseUrl/api/v1/album") { auth() }
    }

    suspend fun getRepeatedCards(): ApiResult<List<UserCardResponse>> {
        return try {
            val list = httpClient.get("$baseUrl/api/v1/album/repeated") { auth() }
                .body<List<UserCardResponse>>(json)
            ApiResult.Success(list)
        } catch (e: Exception) {
            ApiResult.Error(ErrorCode.NETWORK, "Error al obtener repetidas: ${e.message?.take(60)}")
        }
    }

    // ── Exchanges ─────────────────────────────────────────
    suspend fun getExchanges(): ApiResult<ExchangesResponse> = request(ExchangesResponse::class) {
        httpClient.get("$baseUrl/api/v1/exchanges") { auth() }
    }

    suspend fun getAvailableExchanges(): ApiResult<ExchangesResponse> = request(ExchangesResponse::class) {
        httpClient.get("$baseUrl/api/v1/exchanges/available") { auth() }
    }

    suspend fun createExchange(req: CreateExchangeRequest): ApiResult<ExchangeResponse> = request(ExchangeResponse::class) {
        httpClient.post("$baseUrl/api/v1/exchanges") { auth(); setBody(req) }
    }

    suspend fun acceptExchange(id: String): ApiResult<ExchangeResponse> = request(ExchangeResponse::class) {
        httpClient.put("$baseUrl/api/v1/exchanges/$id/accept") { auth() }
    }

    suspend fun rejectExchange(id: String): ApiResult<ExchangeResponse> = request(ExchangeResponse::class) {
        httpClient.put("$baseUrl/api/v1/exchanges/$id/reject") { auth() }
    }

    suspend fun completeExchange(id: String): ApiResult<ExchangeResponse> = request(ExchangeResponse::class) {
        httpClient.put("$baseUrl/api/v1/exchanges/$id/complete") { auth() }
    }

    // ── Panini ────────────────────────────────────────────
    suspend fun paniniLocalLookup(nickname: String): ApiResult<PaniniLookupResponse> = request(PaniniLookupResponse::class) {
        httpClient.get("$baseUrl/api/v1/panini/local/$nickname")
    }

    suspend fun paniniExternalLookup(nickname: String): ApiResult<PaniniLookupResponse> = request(PaniniLookupResponse::class) {
        httpClient.get("$baseUrl/api/v1/panini/external/$nickname")
    }

    suspend fun paniniSearch(query: String): ApiResult<PaniniSearchRoot> = request(PaniniSearchRoot::class) {
        httpClient.get("$baseUrl/api/v1/panini/local/search") { parameter("q", query) }
    }

    fun close() { httpClient.close() }
}

// ── DTOs (igual que antes) ──────────────────────────────────
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
