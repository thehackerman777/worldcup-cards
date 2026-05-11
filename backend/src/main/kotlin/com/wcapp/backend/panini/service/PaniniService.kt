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
        // Caché TTL: 15 minutos
        const val CACHE_TTL_MINUTES = 15L
    }

    /**
     * Endpoint principal de prueba/validación.
     * GET /api/panini/user/{nickname}
     *
     * Busca perfil en caché o genera datos simulados.
     */
    fun getUserByNickname(nickname: String): PaniniUserResponse {
        val startTime = System.currentTimeMillis()

        val profile = profileRepository.findByNicknameIgnoreCase(nickname)

        if (profile != null && isCacheValid(profile)) {
            logSync(nickname, SyncStatus.FROM_CACHE, 0, 0, fromCache = true, responseTimeMs = System.currentTimeMillis() - startTime)
            return buildCachedResponse(profile)
        }

        // Si existe pero expiró, o no existe: devolver simulados + crear perfil
        val (duplicates, missing) = if (profile == null) {
            val mock = generateMockData(nickname)
            persistProfile(nickname, mock)
            mock
        } else {
            val mock = generateMockData(nickname)
            updateProfile(profile, mock)
            mock
        }

        val savedProfile = profileRepository.findByNicknameIgnoreCase(nickname)

        logSync(
            nickname = nickname,
            status = SyncStatus.SUCCESS,
            cardsFound = missing.size + duplicates.size,
            duplicatesFound = duplicates.size,
            fromCache = false,
            responseTimeMs = System.currentTimeMillis() - startTime
        )

        return PaniniUserResponse(
            nickname = nickname,
            duplicates = duplicates,
            missing = missing,
            completion = savedProfile?.completionPercentage ?: 78,
            lastSync = LocalDateTime.now().toString(),
            fromCache = false
        )
    }

    /**
     * Sincronización manual desde la app Android.
     * POST /api/panini/user/sync
     */
    @Transactional
    fun syncUser(request: PaniniSyncRequest): PaniniSyncResponse {
        val startTime = System.currentTimeMillis()

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
            profile.totalCollection = request.totalCollection
            profile.totalCards = request.duplicates.size + request.totalCollection
        }
        profile.lastSync = LocalDateTime.now()
        profile.syncCount = (profile.syncCount ?: 0) + 1
        profileRepository.save(profile)

        // Reemplazar cartas
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

        val duplicatesFound = request.duplicates.size
        val missingFound = request.missing.size
        val totalSynced = cardRepository.findByProfileId(profile.id!!).size

        logSync(
            nickname = request.nickname,
            status = SyncStatus.SUCCESS,
            cardsFound = totalSynced,
            duplicatesFound = duplicatesFound,
            fromCache = false,
            responseTimeMs = System.currentTimeMillis() - startTime
        )

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
     * Busca usuarios por nickname (búsqueda parcial).
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

    private fun buildCachedResponse(profile: PaniniProfile): PaniniUserResponse {
        val duplicates = cardRepository.findByProfileIdAndIsDuplicateTrue(profile.id!!)
            .map { it.cardCode }
        val missing = cardRepository.findByProfileIdAndIsMissingTrue(profile.id!!)
            .map { it.cardCode }

        return PaniniUserResponse(
            nickname = profile.nickname,
            duplicates = duplicates,
            missing = missing,
            completion = profile.completionPercentage,
            lastSync = profile.lastSync.toString(),
            fromCache = true
        )
    }

    @Transactional
    fun persistProfile(nickname: String, data: Pair<List<String>, List<String>>) {
        val (duplicates, missing) = data
        val profile = PaniniProfile(
            nickname = nickname.lowercase(),
            displayName = nickname,
            completionPercentage = calculateCompletion(duplicates.size, missing.size),
            totalCards = duplicates.size + missing.size,
            lastSync = LocalDateTime.now(),
            syncCount = 1
        )
        val saved = profileRepository.save(profile)

        duplicates.forEach { code ->
            cardRepository.save(PaniniCard(
                profile = saved,
                cardCode = code.uppercase(),
                isDuplicate = true,
                quantity = 2,
                isMissing = false
            ))
        }
        missing.forEach { code ->
            cardRepository.save(PaniniCard(
                profile = saved,
                cardCode = code.uppercase(),
                isDuplicate = false,
                quantity = 0,
                isMissing = true
            ))
        }
    }

    @Transactional
    fun updateProfile(profile: PaniniProfile, data: Pair<List<String>, List<String>>) {
        val (duplicates, missing) = data
        profile.completionPercentage = calculateCompletion(duplicates.size, missing.size)
        profile.totalCards = duplicates.size + missing.size
        profile.lastSync = LocalDateTime.now()
        profile.syncCount = (profile.syncCount ?: 0) + 1
        profileRepository.save(profile)

        // Reemplazar cartas (simplificado para MVP)
        val existing = cardRepository.findByProfileId(profile.id!!)
        existing.forEach { cardRepository.delete(it) }

        duplicates.forEach { code ->
            cardRepository.save(PaniniCard(
                profile = profile,
                cardCode = code.uppercase(),
                isDuplicate = true,
                quantity = 2,
                isMissing = false
            ))
        }
        missing.forEach { code ->
            cardRepository.save(PaniniCard(
                profile = profile,
                cardCode = code.uppercase(),
                isDuplicate = false,
                quantity = 0,
                isMissing = true
            ))
        }
    }

    private fun calculateCompletion(duplicates: Int, missing: Int): Int {
        val total = duplicates + missing
        if (total == 0) return 50
        val have = total - missing
        return ((have.toDouble() / total) * 100).toInt().coerceIn(0, 100)
    }

    /**
     * Genera datos simulados para el MVP.
     * Retorna (duplicates, missing).
     */
    private fun generateMockData(nickname: String): Pair<List<String>, List<String>> {
        val seed = nickname.hashCode()
        val rng = Random(seed.toLong())

        val allCards = List(888) { "FWC-${String.format("%03d", it + 1)}" }
        val shuffled = allCards.shuffled(rng)

        val have = shuffled.take(600)
        val missing = shuffled.drop(600).take(100)

        // Marcamos ~30% de las que tiene como duplicadas
        val duplicates = have.shuffled(rng).take(80)

        return Pair(duplicates, missing)
    }

    private fun logSync(nickname: String, status: SyncStatus, cardsFound: Int, duplicatesFound: Int, fromCache: Boolean, responseTimeMs: Long) {
        syncLogRepository.save(PaniniSyncLog(
            nickname = nickname.lowercase(),
            status = status,
            cardsFound = cardsFound,
            duplicatesFound = duplicatesFound,
            responseTimeMs = responseTimeMs,
            fromCache = fromCache
        ))
    }
}
