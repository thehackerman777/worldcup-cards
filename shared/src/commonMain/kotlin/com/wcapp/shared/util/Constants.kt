package com.wcapp.shared.util

object Constants {
    // API endpoints (base URL configured at runtime)
    const val API_PREFIX = "/api/v1"

    // Auth
    const val AUTH_REGISTER = "$API_PREFIX/auth/register"
    const val AUTH_LOGIN = "$API_PREFIX/auth/login"
    const val AUTH_REFRESH = "$API_PREFIX/auth/refresh"

    // Users
    const val USERS_ME = "$API_PREFIX/users/me"

    // Cards
    const val CARDS = "$API_PREFIX/cards"

    // Album
    const val ALBUM = "$API_PREFIX/album"
    const val ALBUM_REPEATED = "$API_PREFIX/album/repeated"
    const val ALBUM_CARDS = "$API_PREFIX/album/cards"

    // Exchanges
    const val EXCHANGES = "$API_PREFIX/exchanges"
    const val EXCHANGES_AVAILABLE = "$API_PREFIX/exchanges/available"

    fun exchangeAction(id: String, action: String) = "$API_PREFIX/exchanges/$id/$action"

    fun cardDetail(id: String) = "$API_PREFIX/cards/$id"
    fun albumCardRemove(id: String) = "$API_PREFIX/album/cards/$id"
}
