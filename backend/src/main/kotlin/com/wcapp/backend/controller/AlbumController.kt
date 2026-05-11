package com.wcapp.backend.controller

import com.wcapp.backend.dto.*
import com.wcapp.backend.security.UserPrincipal
import com.wcapp.backend.service.AlbumService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/album")
class AlbumController(
    private val albumService: AlbumService
) {
    @GetMapping
    fun getAlbum(@AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<AlbumResponse> {
        val response = albumService.getAlbum(principal.id)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/cards")
    fun addCard(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: AddCardRequest
    ): ResponseEntity<UserCardResponse> {
        val response = albumService.addCardToAlbum(principal.id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/repeated")
    fun getRepeated(@AuthenticationPrincipal principal: UserPrincipal): ResponseEntity<List<UserCardResponse>> {
        val response = albumService.getRepeatedCards(principal.id)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/cards/{id}")
    fun removeCard(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: String
    ): ResponseEntity<Void> {
        albumService.removeCardFromAlbum(principal.id, id)
        return ResponseEntity.noContent().build()
    }
}
