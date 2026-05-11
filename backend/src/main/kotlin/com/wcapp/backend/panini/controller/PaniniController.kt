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
    /**
     * ──────────────────────────────────────────────────────
     * ENDPOINTS LOCALES (datos previamente sincronizados)
     * ──────────────────────────────────────────────────────
     */

    /**
     * GET /api/v1/panini/local/{nickname}
     *
     * Consulta datos sincronados previamente en nuestra base local.
     * - Si el perfil existe y caché válida → retorna datos
     * - Si caché expiró → 410 con mensaje de resincronizar
     * - Si nunca se sincronizó → 404 con instrucciones
     *
     * NUNCA consulta la API externa de Panini.
     * NUNCA genera datos simulados.
     */
    @GetMapping("/local/{nickname}")
    fun getLocalUser(@PathVariable nickname: String): ResponseEntity<PaniniUserResponse> {
        val response = paniniService.getUserByNickname(nickname)
        return ResponseEntity.ok(response)
    }

    /**
     * POST /api/v1/panini/local/sync
     *
     * Guarda datos de colección enviados desde la app Android.
     * Actualiza o crea el perfil local.
     */
    @PostMapping("/local/sync")
    fun syncLocalUser(@Valid @RequestBody request: PaniniSyncRequest): ResponseEntity<PaniniSyncResponse> {
        val response = paniniService.syncUser(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * GET /api/v1/panini/local/search?q=texto
     *
     * Busca usuarios que existen en nuestra base local sincronizada.
     */
    @GetMapping("/local/search")
    fun searchLocalUsers(@RequestParam q: String): ResponseEntity<PaniniSearchResponse> {
        val response = paniniService.searchUsers(q)
        return ResponseEntity.ok(response)
    }

    /**
     * ──────────────────────────────────────────────────────
     * ENDPOINTS EXTERNOS (consulta directa a Panini)
     * ──────────────────────────────────────────────────────
     */

    /**
     * GET /api/v1/panini/external/{nickname}
     *
     * Consulta directamente la API oficial/publica de Panini.
     * - Requiere app.panini.external.enabled=true en config
     * - Si la API externa no responde → error claro
     * - NUNCA cae a datos locales ni mock
     */
    @GetMapping("/external/{nickname}")
    fun getExternalUser(@PathVariable nickname: String): ResponseEntity<PaniniUserResponse> {
        val response = externalConnector.lookupUser(nickname)
        return ResponseEntity.ok(response)
    }

    /**
     * Preservamos la ruta original /user/{nickname} como alias de
     * /local/{nickname} por compatibilidad.
     */
    @GetMapping("/user/{nickname}")
    fun getUserByNickname(@PathVariable nickname: String): ResponseEntity<PaniniUserResponse> {
        return getLocalUser(nickname)
    }
}
