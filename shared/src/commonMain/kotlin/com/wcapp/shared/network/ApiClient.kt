package com.wcapp.shared.network

import com.wcapp.shared.dto.*
import com.wcapp.shared.model.*
import com.wcapp.shared.util.Constants
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

class ApiClient(
    private val baseUrl: String,
    private val tokenProvider: TokenProvider? = null
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = false
    }

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(this@ApiClient.json)
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

    private fun HttpRequestBuilder.auth() {
        tokenProvider?.getToken()?.let { token ->
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }

    // ── Auth ──────────────────────────────────────────────

    suspend fun login(request: LoginRequest): Result<AuthTokens> = runCatching {
        httpClient.post("$baseUrl${Constants.AUTH_LOGIN}") {
            setBody(request)
        }.body<AuthTokens>()
    }

    suspend fun register(request: RegisterRequest): Result<AuthTokens> = runCatching {
        httpClient.post("$baseUrl${Constants.AUTH_REGISTER}") {
            setBody(request)
        }.body<AuthTokens>()
    }

    suspend fun refreshToken(request: RefreshTokenRequest): Result<AuthTokens> = runCatching {
        httpClient.post("$baseUrl${Constants.AUTH_REFRESH}") {
            setBody(request)
        }.body<AuthTokens>()
    }

    // ── Users ─────────────────────────────────────────────

    suspend fun getMe(): Result<User> = runCatching {
        httpClient.get("$baseUrl${Constants.USERS_ME}") {
            auth()
        }.body<User>()
    }

    suspend fun updateMe(request: UpdateUserRequest): Result<User> = runCatching {
        httpClient.put("$baseUrl${Constants.USERS_ME}") {
            auth()
            setBody(request)
        }.body<User>()
    }

    // ── Cards ─────────────────────────────────────────────

    suspend fun getCards(page: Int = 0, size: Int = 50, team: String? = null): Result<CardPage> = runCatching {
        httpClient.get("$baseUrl${Constants.CARDS}") {
            auth()
            parameter("page", page)
            parameter("size", size)
            team?.let { parameter("team", it) }
        }.body<CardPage>()
    }

    suspend fun getCard(id: String): Result<Card> = runCatching {
        httpClient.get("$baseUrl${Constants.cardDetail(id)}") {
            auth()
        }.body<Card>()
    }

    // ── Album ─────────────────────────────────────────────

    suspend fun getAlbum(): Result<Album> = runCatching {
        httpClient.get("$baseUrl${Constants.ALBUM}") {
            auth()
        }.body<Album>()
    }

    suspend fun addCard(request: AddCardRequest): Result<UserCard> = runCatching {
        httpClient.post("$baseUrl${Constants.ALBUM_CARDS}") {
            auth()
            setBody(request)
        }.body<UserCard>()
    }

    suspend fun getRepeatedCards(): Result<List<UserCard>> = runCatching {
        httpClient.get("$baseUrl${Constants.ALBUM_REPEATED}") {
            auth()
        }.body<List<UserCard>>()
    }

    suspend fun removeCard(userCardId: String): Result<Unit> = runCatching {
        httpClient.delete("$baseUrl${Constants.albumCardRemove(userCardId)}") {
            auth()
        }
        Unit
    }

    // ── Exchanges ─────────────────────────────────────────

    suspend fun createExchange(request: CreateExchangeRequest): Result<Exchange> = runCatching {
        httpClient.post("$baseUrl${Constants.EXCHANGES}") {
            auth()
            setBody(request)
        }.body<Exchange>()
    }

    suspend fun getExchanges(): Result<ExchangeList> = runCatching {
        httpClient.get("$baseUrl${Constants.EXCHANGES}") {
            auth()
        }.body<ExchangeList>()
    }

    suspend fun getAvailableExchanges(): Result<ExchangeList> = runCatching {
        httpClient.get("$baseUrl${Constants.EXCHANGES_AVAILABLE}") {
            auth()
        }.body<ExchangeList>()
    }

    suspend fun acceptExchange(id: String): Result<Exchange> = runCatching {
        httpClient.put("$baseUrl${Constants.exchangeAction(id, "accept")}") {
            auth()
        }.body<Exchange>()
    }

    suspend fun rejectExchange(id: String): Result<Exchange> = runCatching {
        httpClient.put("$baseUrl${Constants.exchangeAction(id, "reject")}") {
            auth()
        }.body<Exchange>()
    }

    suspend fun completeExchange(id: String): Result<Exchange> = runCatching {
        httpClient.put("$baseUrl${Constants.exchangeAction(id, "complete")}") {
            auth()
        }.body<Exchange>()
    }

    fun close() {
        httpClient.close()
    }
}

interface TokenProvider {
    suspend fun getToken(): String?
}
