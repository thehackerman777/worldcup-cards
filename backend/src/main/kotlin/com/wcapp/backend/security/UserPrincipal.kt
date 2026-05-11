package com.wcapp.backend.security

data class UserPrincipal(
    val id: String,
    val username: String,
    val role: String
)
