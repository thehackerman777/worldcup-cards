package com.wcapp.backend.panini.controller

import com.wcapp.backend.panini.dto.*
import com.wcapp.backend.panini.service.PaniniService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/panini")
class PaniniController(
    private val paniniService: PaniniService
) {
    /**
     * Endpoint de prueba/validación técnica.
     * GET /api/v1/panini/user/{nickname}
     *
     * Retorna información del usuario Panini con:
     * - Cartas duplicadas (códigos FWC-xxx)
     * - Cartas faltantes
     * - Porcentaje de completado
     * - Última sincronización
     *
     * No requiere autenticación para facilitar pruebas externas.
     */
    @GetMapping("/user/{nickname}")
    fun getUserByNickname(@PathVariable nickname: String): ResponseEntity<PaniniUserResponse> {
        val response = paniniService.getUserByNickname(nickname)
        return ResponseEntity.ok(response)
    }

    /**
     * Sincronizar colección desde la app.
     * POST /api/v1/panini/user/sync
     */
    @PostMapping("/user/sync")
    fun syncUser(@Valid @RequestBody request: PaniniSyncRequest): ResponseEntity<PaniniSyncResponse> {
        val response = paniniService.syncUser(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    /**
     * Buscar usuarios por nickname.
     * GET /api/v1/panini/search?q=thehacker
     */
    @GetMapping("/search")
    fun searchUsers(@RequestParam q: String): ResponseEntity<PaniniSearchResponse> {
        val response = paniniService.searchUsers(q)
        return ResponseEntity.ok(response)
    }
}
