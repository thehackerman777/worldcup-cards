package com.wcapp.backend.panini.service

import com.wcapp.backend.panini.dto.*
import com.wcapp.backend.panini.entity.*
import com.wcapp.backend.panini.repository.PaniniCardRepository
import com.wcapp.backend.panini.repository.PaniniProfileRepository
import com.wcapp.backend.panini.repository.PaniniSyncLogRepository
import org.slf4j.LoggerFactory
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
    private val log = LoggerFactory.getLogger(PaniniService::class.java)

    companion object {
        const val CACHE_TTL_MINUTES = 15L
    }

    fun getUserByNickname(nickname: String): PaniniUserResponse {
        log.info("🔍 LOCAL LOOKUP: nickname='{}'", nickname)

        val profile = profileRepository.findByNicknameIgnoreCase(nickname)

        if (profile == null) {
            log.warn("❌ Perfil no encontrado en DB local: '{}'", nickname)
            throw UserNotFoundException("Perfil Panini no encontrado: '$nickname'. Debes sincronizar primero desde la app.")
        }

        if (!isCacheValid(profile)) {
            log.warn("⏰ Caché expirada para '{}' (último sync: {})", nickname, profile.lastSync)
            syncLogRepository.save(PaniniSyncLog(
                nickname = nickname.lowercase(),
                status = SyncStatus.FAILED,
                errorMessage = "Caché expirada. Requiere resincronización."
            ))
            throw SyncExpiredException(
                "Los datos de '$nickname' expiraron en caché. " +
                "Usa POST /api/v1/panini/local/sync para resincronizar."
            )
        }

        val duplicates = cardRepository.findByProfileIdAndIsDuplicateTrue(profile.id!!)
            .map { it.cardCode }
        val missing = cardRepository.findByProfileIdAndIsMissingTrue(profile.id!!)
            .map { it.cardCode }

        log.info("✅ LOOKUP OK: '{}' | {} repetidas, {} faltantes | desde caché", nickname, duplicates.size, missing.size)

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

    @Transactional
    fun syncUser(request: PaniniSyncRequest): PaniniSyncResponse {
        log.info("📥 SYNC: nickname='{}' | {} repetidas, {} faltantes, {}% completado",
            request.nickname, request.duplicates.size, request.missing.size, request.completion)

        var profile = profileRepository.findByNicknameIgnoreCase(request.nickname)

        if (profile == null) {
            log.info("🆕 Creando nuevo perfil para: '{}'", request.nickname)
            profile = PaniniProfile(
                nickname = request.nickname.lowercase(),
                displayName = request.nickname,
                completionPercentage = request.completion,
                totalCards = request.duplicates.size + request.totalCollection,
                totalCollection = request.totalCollection
            )
        } else {
            log.info("🔄 Actualizando perfil existente: '{}'", request.nickname)
            profile.completionPercentage = request.completion
            profile.totalCards = request.duplicates.size + request.totalCollection
            profile.totalCollection = request.totalCollection
        }

        profile.lastSync = LocalDateTime.now()
        profile.syncCount = (profile.syncCount ?: 0) + 1
        profileRepository.save(profile)

        // Reemplazar cartas sincronizadas
        val existing = cardRepository.findByProfileId(profile.id!!)
        if (existing.isNotEmpty()) {
            log.info("🗑️ Limpiando {} registros anteriores para '{}'", existing.size, request.nickname)
            existing.forEach { cardRepository.delete(it) }
        }

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

        log.info("✅ SYNC OK: '{}' | {} cartas totales, {} repetidas, {} faltantes",
            request.nickname, totalSynced, duplicatesFound, missingFound)

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

    fun searchUsers(query: String): PaniniSearchResponse {
        log.info("🔎 SEARCH: query='{}'", query)

        val allProfiles = profileRepository.findByActiveTrue()
        val filtered = allProfiles.filter {
            it.nickname.contains(query, ignoreCase = true) ||
            (it.displayName?.contains(query, ignoreCase = true) ?: false)
        }

        log.info("✅ SEARCH OK: '{}' → {} resultados de {} perfiles totales",
            query, filtered.size, allProfiles.size)

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

    private fun isCacheValid(profile: PaniniProfile): Boolean {
        return profile.lastSync.isAfter(LocalDateTime.now().minusMinutes(CACHE_TTL_MINUTES))
    }
}

class UserNotFoundException(message: String) : RuntimeException(message)
class SyncExpiredException(message: String) : RuntimeException(message)
