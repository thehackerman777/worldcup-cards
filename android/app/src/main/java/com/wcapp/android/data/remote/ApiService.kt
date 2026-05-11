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
 * ApiResult — respuesta tipada con manejo de errores.
 * No usa Result<T> de Kotlin (bugs conocidos).
 * No usa inline functions (problemas con smart casts).
 * Cada método maneja su propio try/catch.
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val code: ErrorCode, val message: String) : ApiResult<Nothing>()
}

enum class ErrorCode { NETWORK, SERVER, NOT_FOUND, UNAUTHORIZED, VALIDATION, PARSE }

class ApiService(private val prefs: SecurePrefs) {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true }

    private val client = HttpClient {
        install(ContentNegotiation) { json(this@ApiService.json) }
        install(HttpTimeout) { requestTimeoutMillis = 15_000; connectTimeoutMillis = 8_000; socketTimeoutMillis = 15_000 }
        defaultRequest { contentType(ContentType.Application.Json); accept(ContentType.Application.Json) }
    }

    private val baseUrl: String get() = prefs.serverUrl.trimEnd('/')
    private fun HttpRequestBuilder.auth() { prefs.accessToken?.let { header(HttpHeaders.Authorization, "Bearer $it") } }

    private fun errorMsg(e: Exception, fallback: String): String = when (e) {
        is HttpRequestTimeoutException -> "Tiempo de espera agotado. Revisa la URL."
        is java.net.ConnectException -> "No se pudo conectar a: $baseUrl"
        is java.net.UnknownHostException -> "Host desconocido: $baseUrl"
        is kotlinx.serialization.SerializationException -> "Error al leer respuesta del servidor"
        is ClientRequestException -> errorMsgForStatus(e.response.status, e.response)
        else -> e.message ?: fallback
    }

    private fun errorMsgForStatus(status: HttpStatusCode, resp: HttpResponse): String = when (status) {
        HttpStatusCode.Unauthorized -> "Credenciales inválidas"
        HttpStatusCode.NotFound -> "Recurso no encontrado"
        HttpStatusCode.BadRequest -> "Datos inválidos"
        HttpStatusCode.Gone -> "Datos expirados, resincroniza"
        HttpStatusCode.ServiceUnavailable -> "Servicio no disponible"
        else -> "Error del servidor (${status.value})"
    }

    private fun <T> success(data: T) = ApiResult.Success(data)
    private fun error(code: ErrorCode, msg: String) = ApiResult.Error(code, msg)
    private fun httpError(e: Exception, fallback: String = "Error de conexión") = error(ErrorCode.NETWORK, errorMsg(e, fallback))

    // ── Auth ──────────────────────────────────────────────
    suspend fun login(username: String, password: String): ApiResult<ApiResponse> = try {
        val r = client.post("$baseUrl/api/v1/auth/login") { setBody(mapOf("username" to username, "password" to password)) }
        success(r.body<ApiResponse>())
    } catch (e: Exception) { httpError(e) }

    suspend fun register(u: String, e: String, p: String, dn: String?): ApiResult<ApiResponse> = try {
        val r = client.post("$baseUrl/api/v1/auth/register") { setBody(mapOf("username" to u, "email" to e, "password" to p, "displayName" to (dn ?: u))) }
        success(r.body<ApiResponse>())
    } catch (e: Exception) { httpError(e) }

    // ── Cards ─────────────────────────────────────────────
    suspend fun getCards(page: Int = 0, size: Int = 50, team: String? = null): ApiResult<CardsResponse> = try {
        val r = client.get("$baseUrl/api/v1/cards") { auth(); parameter("page", page); parameter("size", size); team?.let { parameter("team", it) } }
        success(r.body<CardsResponse>())
    } catch (e: Exception) { httpError(e) }

    suspend fun getCard(id: String): ApiResult<CardResponse> = try {
        success(client.get("$baseUrl/api/v1/cards/$id") { auth() }.body<CardResponse>())
    } catch (e: Exception) { httpError(e) }

    // ── Album ─────────────────────────────────────────────
    suspend fun getAlbum(): ApiResult<AlbumResponse> = try {
        success(client.get("$baseUrl/api/v1/album") { auth() }.body<AlbumResponse>())
    } catch (e: Exception) { httpError(e) }

    suspend fun getRepeatedCards(): ApiResult<List<UserCardResponse>> = try {
        success(client.get("$baseUrl/api/v1/album/repeated") { auth() }.body<List<UserCardResponse>>())
    } catch (e: Exception) { httpError(e) }

    // ── Exchanges ─────────────────────────────────────────
    suspend fun getExchanges(): ApiResult<ExchangesResponse> = try {
        success(client.get("$baseUrl/api/v1/exchanges") { auth() }.body<ExchangesResponse>())
    } catch (e: Exception) { httpError(e) }

    suspend fun getAvailableExchanges(): ApiResult<ExchangesResponse> = try {
        success(client.get("$baseUrl/api/v1/exchanges/available") { auth() }.body<ExchangesResponse>())
    } catch (e: Exception) { httpError(e) }

    suspend fun createExchange(req: CreateExchangeRequest): ApiResult<ExchangeResponse> = try {
        success(client.post("$baseUrl/api/v1/exchanges") { auth(); setBody(req) }.body<ExchangeResponse>())
    } catch (e: Exception) { httpError(e) }

    suspend fun acceptExchange(id: String): ApiResult<ExchangeResponse> = try {
        success(client.put("$baseUrl/api/v1/exchanges/$id/accept") { auth() }.body<ExchangeResponse>())
    } catch (e: Exception) { httpError(e) }

    suspend fun rejectExchange(id: String): ApiResult<ExchangeResponse> = try {
        success(client.put("$baseUrl/api/v1/exchanges/$id/reject") { auth() }.body<ExchangeResponse>())
    } catch (e: Exception) { httpError(e) }

    suspend fun completeExchange(id: String): ApiResult<ExchangeResponse> = try {
        success(client.put("$baseUrl/api/v1/exchanges/$id/complete") { auth() }.body<ExchangeResponse>())
    } catch (e: Exception) { httpError(e) }

    // ── Panini ────────────────────────────────────────────
    suspend fun paniniLocalLookup(nickname: String): ApiResult<PaniniLookupResponse> = try {
        success(client.get("$baseUrl/api/v1/panini/local/$nickname").body<PaniniLookupResponse>())
    } catch (e: Exception) { httpError(e) }

    suspend fun paniniExternalLookup(nickname: String): ApiResult<PaniniLookupResponse> = try {
        success(client.get("$baseUrl/api/v1/panini/external/$nickname").body<PaniniLookupResponse>())
    } catch (e: Exception) { httpError(e) }

    suspend fun paniniSearch(query: String): ApiResult<PaniniSearchRoot> = try {
        success(client.get("$baseUrl/api/v1/panini/local/search") { parameter("q", query) }.body<PaniniSearchRoot>())
    } catch (e: Exception) { httpError(e) }

    fun close() { client.close() }
}

// ── DTOs ──────────────────────────────────────────────────
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

/** Extension functions para compatibilidad con .onSuccess {} .onFailure {} */
fun <T> ApiResult<T>.onSuccess(action: () -> Unit): ApiResult<T> {
    if (this is ApiResult.Success) action()
    return this
}
fun <T> ApiResult<T>.onSuccess(action: (T) -> Unit): ApiResult<T> {
    if (this is ApiResult.Success) action(data)
    return this
}
fun <T> ApiResult<T>.onFailure(action: (ApiResult.Error) -> Unit): ApiResult<T> {
    if (this is ApiResult.Error) action(this)
    return this
}
