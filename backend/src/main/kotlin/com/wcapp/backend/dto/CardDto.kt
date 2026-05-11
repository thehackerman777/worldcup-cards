package com.wcapp.backend.dto

data class CardResponse(
    val id: String,
    val name: String,
    val cardNumber: Int,
    val team: String,
    val position: String?,
    val imageUrl: String?,
    val rarity: String,
    val description: String?,
    val year: Int,
    val edition: String
)

data class CardListResponse(
    val cards: List<CardResponse>,
    val totalPages: Int,
    val totalElements: Long,
    val currentPage: Int,
    val pageSize: Int
)
