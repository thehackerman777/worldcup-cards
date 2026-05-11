package com.wcapp.backend.sync.controller

import com.wcapp.backend.security.UserPrincipal
import com.wcapp.backend.sync.dto.*
import com.wcapp.backend.sync.service.SyncService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/sync")
class SyncController(
    private val syncService: SyncService
) {
    /**
     * POST /api/v1/sync
     *
     * Sincroniza cartas escaneadas desde el overlay Android.
     * Acepta lista de cartas con código, cantidad y estado.
     */
    @PostMapping
    fun syncCards(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: SyncRequest
    ): ResponseEntity<SyncResponse> {
        val response = syncService.syncCards(principal.id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}

@RestController
@RequestMapping("/api/v1/matches")
class MatchController(
    private val syncService: SyncService
) {
    /**
     * GET /api/v1/matches?needs=FWC-001,FWC-145
     *
     * Encuentra usuarios que tengan duplicados de las cartas
     * que el usuario actual necesita, basado en datos escaneados.
     */
    @GetMapping
    fun getMatches(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam needs: String
    ): ResponseEntity<MatchResponse> {
        val neededCards = needs.split(",").map { it.trim().uppercase() }
        val response = syncService.getMatches(principal.id, neededCards)
        return ResponseEntity.ok(response)
    }
}
