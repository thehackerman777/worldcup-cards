package com.wcapp.android.domain.model

/**
 * Modelos de dominio para la app Android.
 * Estos son wrappers/adapters de los modelos KMP compartidos.
 */
data class UserInfo(
    val id: String,
    val username: String,
    val email: String,
    val displayName: String?,
    val avatarUrl: String?
)

data class SessionState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = true,
    val token: String? = null,
    val user: UserInfo? = null,
    val error: String? = null
)

data class ServerConfig(
    val host: String,
    val port: String = "8080",
    val useHttps: Boolean = false
) {
    val baseUrl: String get() = "${if (useHttps) "https" else "http"}://$host:$port"
}
