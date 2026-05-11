package com.wcapp.android.data.local

import com.wcapp.android.domain.model.SessionState
import com.wcapp.android.domain.model.UserInfo
import com.wcapp.android.security.SecurePrefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gestor de sesión reactivo.
 * Expone un StateFlow para que la UI reaccione a cambios de autenticación.
 */
class SessionManager(
    private val prefs: SecurePrefs
) {
    private val _sessionState = MutableStateFlow(
        if (prefs.isLoggedIn) {
            SessionState(
                isLoggedIn = true,
                isLoading = false,
                token = prefs.accessToken,
                user = UserInfo(
                    id = prefs.userId ?: "",
                    username = prefs.username ?: "",
                    email = prefs.userEmail ?: "",
                    displayName = prefs.userDisplayName,
                    avatarUrl = null
                )
            )
        } else {
            SessionState(isLoggedIn = false, isLoading = false)
        }
    )

    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    fun saveSession(token: String, refreshToken: String, userId: String, username: String, email: String, displayName: String?) {
        prefs.saveSession(token, refreshToken, userId, username, email, displayName)
        _sessionState.value = SessionState(
            isLoggedIn = true,
            isLoading = false,
            token = token,
            user = UserInfo(userId, username, email, displayName, null)
        )
    }

    fun clearSession() {
        prefs.clearSession()
        _sessionState.value = SessionState(isLoggedIn = false, isLoading = false)
    }

    fun updateServerUrl(url: String) {
        prefs.serverUrl = url
    }

    fun getServerUrl(): String = prefs.serverUrl

    fun setDarkMode(enabled: Boolean) {
        prefs.darkModeEnabled = enabled
    }

    fun isDarkMode(): Boolean? = prefs.darkModeEnabled
}
