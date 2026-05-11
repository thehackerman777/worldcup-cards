package com.wcapp.backend.dto

data class AlbumResponse(
    val userId: String,
    val username: String,
    val totalCards: Int,
    val albumCards: Long,
    val repeatedCards: Int,
    val completionPercentage: Double,
    val cards: List<UserCardResponse>
)

data class UserCardResponse(
    val id: String,
    val card: CardResponse,
    val quantity: Int,
    val isInAlbum: Boolean,
    val isRepeated: Boolean,
    val tradeable: Boolean
)

data class AddCardRequest(
    val cardId: String,
    val quantity: Int = 1,
    val isInAlbum: Boolean = true
)
