package com.wcapp.shared.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val username: String,
    val email: String,
    @SerialName("displayName") val displayName: String? = null,
    @SerialName("avatarUrl") val avatarUrl: String? = null,
    val role: String = "USER"
)

@Serializable
data class Card(
    val id: String,
    val name: String,
    @SerialName("cardNumber") val cardNumber: Int,
    val team: String,
    val position: String? = null,
    @SerialName("imageUrl") val imageUrl: String? = null,
    val rarity: String = "COMMON",
    val description: String? = null,
    val year: Int = 2026,
    val edition: String = "Mundial 2026"
)

@Serializable
data class UserCard(
    val id: String,
    val card: Card,
    val quantity: Int = 1,
    @SerialName("isInAlbum") val isInAlbum: Boolean = false,
    @SerialName("isRepeated") val isRepeated: Boolean = false,
    val tradeable: Boolean = false
)

@Serializable
data class Album(
    @SerialName("userId") val userId: String,
    val username: String,
    @SerialName("totalCards") val totalCards: Int,
    @SerialName("albumCards") val albumCards: Long,
    @SerialName("repeatedCards") val repeatedCards: Int,
    @SerialName("completionPercentage") val completionPercentage: Double,
    val cards: List<UserCard> = emptyList()
)

@Serializable
data class Exchange(
    val id: String,
    val requester: ExchangeUserInfo,
    val receiver: ExchangeUserInfo,
    val status: String = "PENDING",
    val message: String? = null,
    @SerialName("offeredCards") val offeredCards: List<ExchangeItem> = emptyList(),
    @SerialName("requestedCards") val requestedCards: List<ExchangeItem> = emptyList(),
    @SerialName("createdAt") val createdAt: String = ""
)

@Serializable
data class ExchangeUserInfo(
    val id: String,
    val username: String,
    @SerialName("displayName") val displayName: String? = null
)

@Serializable
data class ExchangeItem(
    val id: String,
    val card: Card,
    val quantity: Int = 1,
    @SerialName("offeredBy") val offeredBy: String = ""
)

@Serializable
data class AuthTokens(
    val token: String,
    @SerialName("refreshToken") val refreshToken: String,
    @SerialName("tokenType") val tokenType: String = "Bearer",
    @SerialName("expiresIn") val expiresIn: Long,
    val user: User
)

@Serializable
data class CardPage(
    val cards: List<Card>,
    @SerialName("totalPages") val totalPages: Int,
    @SerialName("totalElements") val totalElements: Long,
    @SerialName("currentPage") val currentPage: Int,
    @SerialName("pageSize") val pageSize: Int
)

@Serializable
data class ExchangeList(
    val exchanges: List<Exchange>
)

@Serializable
data class ErrorBody(
    val timestamp: String = "",
    val status: Int = 0,
    val error: String = "",
    val message: String = "",
    val path: String = ""
)
