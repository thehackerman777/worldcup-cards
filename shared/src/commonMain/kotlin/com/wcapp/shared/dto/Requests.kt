package com.wcapp.shared.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    @SerialName("displayName") val displayName: String? = null
)

@Serializable
data class RefreshTokenRequest(
    @SerialName("refreshToken") val refreshToken: String
)

@Serializable
data class AddCardRequest(
    @SerialName("cardId") val cardId: String,
    val quantity: Int = 1,
    @SerialName("isInAlbum") val isInAlbum: Boolean = true
)

@Serializable
data class CreateExchangeRequest(
    @SerialName("receiverId") val receiverId: String,
    val message: String? = null,
    @SerialName("offeredCards") val offeredCards: List<ExchangeCardEntry>,
    @SerialName("requestedCards") val requestedCards: List<ExchangeCardEntry>
)

@Serializable
data class ExchangeCardEntry(
    @SerialName("cardId") val cardId: String,
    val quantity: Int = 1
)

@Serializable
data class UpdateUserRequest(
    @SerialName("displayName") val displayName: String? = null,
    @SerialName("avatarUrl") val avatarUrl: String? = null,
    val phone: String? = null
)
