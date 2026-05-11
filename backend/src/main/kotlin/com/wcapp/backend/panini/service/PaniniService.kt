package com.wcapp.backend.panini.service

import com.wcapp.backend.panini.dto.*
import com.wcapp.backend.panini.entity.*
import com.wcapp.backend.panini.repository.PaniniCardRepository
import com.wcapp.backend.panini.repository.PaniniProfileRepository
import com.wcapp.backend.panini.repository.PaniniSyncLogRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class PaniniService(
    private val profileRepository: PaniniProfileRepository,
    private val cardRepository: PaniniCardRepository,
    private val syncLogRepository: PaniniSyncLogRepository
) {
    companion object {
        const val CACHE_TTL_MINUTES = 15L
    }

    /**
     * GET /api/panini/user/{nickname}
     *
     * Busca un perfil de usuario que haya sido sincronizado previamente.
     * - Si existe y la caché es válida → retorna datos cacheados
     * - Si existe pero caché expiró → retorna error de sincronización (no mock)
     * - Si no existe → error de perfil no encontrado
     *
     * NUNCA genera datos mock/simulados.
     */
    fun getUserByNickname(nickname: String): PaniniUserResponse {
        val profile = profileRepository.findByNicknameIgnoreCase(nickname)

        if (profile == null) {
            throw UserNotFoundException("Perfil Panini no encontrado: '$nickname'. Debes sincronizar primero desde la app.")
        }

        if (!isCacheValid(profile)) {
            syncLogRepository.save(PaniniSyncLog(
                nickname = nickname.lowercase(),
                status = SyncStatus.FAILED,
                errorMessage = "Caché expirada. Requiere resincronización."
            ))
            throw SyncExpiredException(
                "Los datos de '$nickname' expiraron en caché. " +
                "Usa POST /api/v1/panini/user/sync para resincronizar."
            )
        }

        val duplicates = cardRepository.findByProfileIdAndIsDuplicateTrue(profile.id!!)
            .map { it.cardCode }
        val missing = cardRepository.findByProfileIdAndIsMissingTrue(profile.id!!)
            .map { it.cardCode }

        syncLogRepository.save(PaniniSyncLog(
            nickname = nickname.lowercase(),
            status = SyncStatus.FROM_CACHE,
            cardsFound = missing.size + duplicates.size,
            duplicatesFound = duplicates.size,
            fromCache = true
        ))

        return PaniniUserResponse(
            nickname = profile.nickname,
            duplicates = duplicates,
            missing = missing,
            completion = profile.completionPercentage,
            lastSync = profile.lastSync.toString(),
            profileFound = true,
            fromCache = true
        )
    }

    /**
     * POST /api/v1/panini/user/sync
     *
     * Recibe datos reales sincronizados desde la app Android.
     * No consulta APIs externas — la app es quien sincroniza y envía.
     */
    @Transactional
    fun syncUser(request: PaniniSyncRequest): PaniniSyncResponse {
        var profile = profileRepository.findByNicknameIgnoreCase(request.nickname)

        if (profile == null) {
            profile = PaniniProfile(
                nickname = request.nickname.lowercase(),
                displayName = request.nickname,
                completionPercentage = request.completion,
                totalCards = request.duplicates.size + request.totalCollection,
                totalCollection = request.totalCollection
            )
        } else {
            profile.completionPercentage = request.completion
            profile.totalCards = request.duplicates.size + request.totalCollection
            profile.totalCollection = request.totalCollection
        }

        profile.lastSync = LocalDateTime.now()
        profile.syncCount = (profile.syncCount ?: 0) + 1
        profileRepository.save(profile)

        // Reemplazar cartas sincronizadas
        cardRepository.findByProfileId(profile.id!!).forEach { cardRepository.delete(it) }

        request.duplicates.forEach { code ->
            cardRepository.save(PaniniCard(
                profile = profile,
                cardCode = code.uppercase(),
                isDuplicate = true,
                quantity = 2,
                isMissing = false
            ))
        }
        request.missing.forEach { code ->
            cardRepository.save(PaniniCard(
                profile = profile,
                cardCode = code.uppercase(),
                isDuplicate = false,
                quantity = 0,
                isMissing = true
            ))
        }

        val totalSynced = cardRepository.findByProfileId(profile.id!!).size
        val duplicatesFound = request.duplicates.size
        val missingFound = request.missing.size

        syncLogRepository.save(PaniniSyncLog(
            nickname = request.nickname.lowercase(),
            status = SyncStatus.SUCCESS,
            cardsFound = totalSynced,
            duplicatesFound = duplicatesFound,
            fromCache = false
        ))

        return PaniniSyncResponse(
            nickname = request.nickname,
            cardsSynced = totalSynced,
            duplicatesFound = duplicatesFound,
            missingFound = missingFound,
            completion = request.completion,
            syncedAt = LocalDateTime.now().toString()
        )
    }

    /**
     * GET /api/panini/search?q=texto
     *
     * Busca entre los perfiles que ya han sido sincronizados.
     * Solo muestra usuarios que existen en la base local.
     */
    fun searchUsers(query: String): PaniniSearchResponse {
        val allProfiles = profileRepository.findByActiveTrue()
        val filtered = allProfiles.filter {
            it.nickname.contains(query, ignoreCase = true) ||
            (it.displayName?.contains(query, ignoreCase = true) ?: false)
        }

        return PaniniSearchResponse(
            results = filtered.map { profile ->
                val duplicates = cardRepository.findByProfileIdAndIsDuplicateTrue(profile.id!!)
                PaniniUserSummary(
                    nickname = profile.nickname,
                    displayName = profile.displayName,
                    completion = profile.completionPercentage,
                    duplicateCount = duplicates.size,
                    lastSync = profile.lastSync.toString()
                )
            },
            total = filtered.size
        )
    }

    // ── Helpers ──────────────────────────────────────────

    private fun isCacheValid(profile: PaniniProfile): Boolean {
        return profile.lastSync.isAfter(LocalDateTime.now().minusMinutes(CACHE_TTL_MINUTES))
    }
}

class UserNotFoundException(message: String) : RuntimeException(message)
class SyncExpiredException(message: String) : RuntimeException(message)
