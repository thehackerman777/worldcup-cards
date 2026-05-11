package com.wcapp.backend.panini.controller

import com.wcapp.backend.panini.connector.PaniniExternalConnector
import com.wcapp.backend.panini.dto.*
import com.wcapp.backend.panini.service.PaniniService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/panini")
class PaniniController(
    private val paniniService: PaniniService,
    private val externalConnector: PaniniExternalConnector
) {
    // ═══════════════════════════════════════════════════
    // LOCAL - datos previamente sincronizados
    // ═══════════════════════════════════════════════════

    /** GET /api/v1/panini/local/{nickname} */
    @GetMapping("/local/{nickname}")
    fun getLocalUser(@PathVariable nickname: String): ResponseEntity<PaniniUserResponse> {
        val response = paniniService.getUserByNickname(nickname)
        return ResponseEntity.ok(response)
    }

    /** POST /api/v1/panini/local/sync */
    @PostMapping("/local/sync")
    fun syncLocalUser(@Valid @RequestBody request: PaniniSyncRequest): ResponseEntity<PaniniSyncResponse> {
        val response = paniniService.syncUser(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /** GET /api/v1/panini/local/search?q= */
    @GetMapping("/local/search")
    fun searchLocalUsers(@RequestParam q: String): ResponseEntity<PaniniSearchResponse> {
        val response = paniniService.searchUsers(q)
        return ResponseEntity.ok(response)
    }

    // ═══════════════════════════════════════════════════
    // EXTERNAL - consulta en nube (con fallback local)
    // ═══════════════════════════════════════════════════

    /**
     * GET /api/v1/panini/external/{nickname}
     *
     * Intenta consultar la API pública de Panini.
     * Si falla, revisa si tenemos datos cacheados localmente.
     * Si tampoco, error claro.
     *
     * Flujo: API externa → fallback local → error
     */
    @GetMapping("/external/{nickname}")
    fun getExternalUser(@PathVariable nickname: String): ResponseEntity<PaniniUserResponse> {
        // 1. Intentar API externa
        val external = externalConnector.tryLookupUser(nickname)
        if (external != null) {
            return ResponseEntity.ok(external)
        }

        // 2. Fallback: buscar en cache local
        try {
            val local = paniniService.getUserByNickname(nickname)
            return ResponseEntity.ok(
                local.copy(fromCache = true, profileFound = true)
            )
        } catch (e: Exception) {
            // 3. No hay datos ni locales ni externos
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                PaniniUserResponse(
                    nickname = nickname,
                    duplicates = emptyList(),
                    missing = emptyList(),
                    completion = 0,
                    lastSync = "",
                    profileFound = false,
                    fromCache = false
                )
            )
        }
    }

    // ═══════════════════════════════════════════════════
    // COMPATIBILIDAD
    // ═══════════════════════════════════════════════════

    /** GET /api/v1/panini/user/{nickname} → alias de /local/ */
    @GetMapping("/user/{nickname}")
    fun getUserByNickname(@PathVariable nickname: String): ResponseEntity<PaniniUserResponse> {
        return getLocalUser(nickname)
    }

    /**
     * GET /api/v1/panini/cloud/{nickname}
     *
     * Alias más intuitivo de /external/. Busca en la nube
     * con fallback a datos locales.
     */
    @GetMapping("/cloud/{nickname}")
    fun getCloudUser(@PathVariable nickname: String): ResponseEntity<PaniniUserResponse> {
        return getExternalUser(nickname)
    }
}
