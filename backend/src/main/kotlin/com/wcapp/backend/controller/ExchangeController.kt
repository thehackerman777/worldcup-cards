package com.wcapp.backend.controller

import com.wcapp.backend.dto.*
import com.wcapp.backend.security.UserPrincipal
import com.wcapp.backend.service.ExchangeService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/exchanges")
class ExchangeController(
    private val exchangeService: ExchangeService
) {
    @PostMapping
    fun createExchange(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: CreateExchangeRequest
    ): ResponseEntity<ExchangeResponse> {
        val response = exchangeService.createExchange(principal.id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    fun getExchanges(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ExchangeListResponse> {
        val exchanges = exchangeService.getUserExchanges(principal.id)
        return ResponseEntity.ok(ExchangeListResponse(exchanges = exchanges))
    }

    @GetMapping("/available")
    fun getAvailable(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ExchangeListResponse> {
        val exchanges = exchangeService.getAvailableExchanges(principal.id)
        return ResponseEntity.ok(ExchangeListResponse(exchanges = exchanges))
    }

    @PutMapping("/{id}/accept")
    fun acceptExchange(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: String
    ): ResponseEntity<ExchangeResponse> {
        val response = exchangeService.acceptExchange(id, principal.id)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{id}/reject")
    fun rejectExchange(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: String
    ): ResponseEntity<ExchangeResponse> {
        val response = exchangeService.rejectExchange(id, principal.id)
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{id}/complete")
    fun completeExchange(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: String
    ): ResponseEntity<ExchangeResponse> {
        val response = exchangeService.completeExchange(id, principal.id)
        return ResponseEntity.ok(response)
    }
}
