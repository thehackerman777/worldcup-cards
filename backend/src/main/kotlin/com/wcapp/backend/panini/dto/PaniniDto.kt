package com.wcapp.backend.panini.dto

data class PaniniUserResponse(
    val nickname: String,
    val duplicates: List<String>,
    val missing: List<String>,
    val completion: Int,
    val lastSync: String,
    val profileFound: Boolean = true,
    val fromCache: Boolean = false
)

data class PaniniSyncRequest(
    val nickname: String,
    val duplicates: List<String> = emptyList(),
    val missing: List<String> = emptyList(),
    val completion: Int = 0,
    val totalCollection: Int = 0
)

data class PaniniSyncResponse(
    val nickname: String,
    val cardsSynced: Int,
    val duplicatesFound: Int,
    val missingFound: Int,
    val completion: Int,
    val syncedAt: String
)

data class PaniniSearchResponse(
    val results: List<PaniniUserSummary>,
    val total: Int
)

data class PaniniUserSummary(
    val nickname: String,
    val displayName: String?,
    val completion: Int,
    val duplicateCount: Int,
    val lastSync: String?
)

data class PaniniMatchResponse(
    val userA: String,
    val userB: String,
    val compatibilityScore: Int,
    val matches: List<PaniniMatchEntry>
)

data class PaniniMatchEntry(
    val cardCode: String,
    val offeredBy: String,
    val requestedBy: String,
    val quantity: Int
)

data class PaniniErrorResponse(
    val error: String,
    val message: String,
    val code: String
)
