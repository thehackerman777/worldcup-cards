package com.wcapp.backend.sync.dto

data class SyncRequest(
    val cards: List<SyncCardEntry>
)

data class SyncCardEntry(
    val cardCode: String,
    val quantity: Int = 1,
    val isDuplicate: Boolean = false,
    val scanTimestamp: Long? = null
)

data class SyncResponse(
    val syncedCount: Int,
    val duplicatesFound: Int,
    val message: String = "Sincronización exitosa"
)

data class MatchRequest(
    val neededCards: List<String>,
    val maxDistance: Int? = null  // Future: geolocation
)

data class MatchResponse(
    val matches: List<MatchResult>
)

data class MatchResult(
    val userId: String,
    val username: String,
    val displayName: String?,
    val offeredCards: List<String>,
    val neededCards: List<String>,
    val matchScore: Int  // 0-100
)
