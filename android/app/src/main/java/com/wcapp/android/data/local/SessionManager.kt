package com.wcapp.android.data.local

import com.wcapp.android.domain.model.SessionState
import com.wcapp.android.domain.model.UserInfo
import com.wcapp.android.security.SecurePrefs
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf

/**
 * Gestor de sesión reactivo.
 */
class SessionManager(
    private val prefs: SecurePrefs
) {
    private val _sessionState = mutableStateOf(
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

    val sessionState: State<SessionState> = _sessionState

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
