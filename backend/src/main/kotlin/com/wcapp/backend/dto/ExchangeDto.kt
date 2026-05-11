package com.wcapp.backend.dto

data class CreateExchangeRequest(
    val receiverId: String,
    val message: String? = null,
    val offeredCards: List<ExchangeCardEntry>,
    val requestedCards: List<ExchangeCardEntry>
)

data class ExchangeCardEntry(
    val cardId: String,
    val quantity: Int = 1
)

data class ExchangeResponse(
    val id: String,
    val requester: ExchangeUserInfo,
    val receiver: ExchangeUserInfo,
    val status: String,
    val message: String?,
    val offeredCards: List<ExchangeItemResponse>,
    val requestedCards: List<ExchangeItemResponse>,
    val createdAt: String,
    val updatedAt: String?
)

data class ExchangeUserInfo(
    val id: String,
    val username: String,
    val displayName: String?
)

data class ExchangeItemResponse(
    val id: String,
    val card: CardResponse,
    val quantity: Int,
    val offeredBy: String
)

data class ExchangeListResponse(
    val exchanges: List<ExchangeResponse>
)
