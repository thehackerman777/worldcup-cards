package com.wcapp.backend.controller

import com.wcapp.backend.dto.CardListResponse
import com.wcapp.backend.dto.CardResponse
import com.wcapp.backend.service.CardService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/cards")
class CardController(
    private val cardService: CardService
) {
    @GetMapping
    fun getAllCards(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int,
        @RequestParam(required = false) team: String?
    ): ResponseEntity<CardListResponse> {
        val response = cardService.getAllCards(page, size, team)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    fun getCardById(@PathVariable id: String): ResponseEntity<CardResponse> {
        val response = cardService.getCardById(id)
        return ResponseEntity.ok(response)
    }
}
