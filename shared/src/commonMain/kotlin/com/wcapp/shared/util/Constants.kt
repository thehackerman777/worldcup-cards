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

    // Panini — Local (datos sincronizados en nuestro servidor)
    const val PANINI_LOCAL = "$API_PREFIX/panini/local"
    const val PANINI_LOCAL_SYNC = "$API_PREFIX/panini/local/sync"
    const val PANINI_LOCAL_SEARCH = "$API_PREFIX/panini/local/search"

    /** GET /api/v1/panini/local/{nickname} */
    fun paniniLocalUser(nickname: String) = "$API_PREFIX/panini/local/$nickname"

    // Panini — External (consulta directa a API pública de Panini)
    const val PANINI_EXTERNAL = "$API_PREFIX/panini/external"

    /** GET /api/v1/panini/external/{nickname} */
    fun paniniExternalUser(nickname: String) = "$API_PREFIX/panini/external/$nickname"

    // Compatibilidad (alias de /local/)
    fun paniniUser(nickname: String) = "$API_PREFIX/panini/user/$nickname"

    fun cardDetail(id: String) = "$API_PREFIX/cards/$id"
    fun albumCardRemove(id: String) = "$API_PREFIX/album/cards/$id"
}
