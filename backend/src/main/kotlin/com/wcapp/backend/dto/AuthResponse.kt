package com.wcapp.backend.dto

data class AuthResponse(
    val token: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val user: UserResponse
)

data class UserResponse(
    val id: String,
    val username: String,
    val email: String,
    val displayName: String?,
    val avatarUrl: String?,
    val role: String
)
