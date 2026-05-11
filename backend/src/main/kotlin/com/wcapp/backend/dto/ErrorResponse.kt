package com.wcapp.backend.dto

import java.time.LocalDateTime

data class ErrorResponse(
    val timestamp: String = LocalDateTime.now().toString(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)

data class UpdateUserRequest(
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val phone: String? = null
)
