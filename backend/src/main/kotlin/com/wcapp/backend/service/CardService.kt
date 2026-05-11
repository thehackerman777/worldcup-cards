package com.wcapp.backend.service

import com.wcapp.backend.dto.CardListResponse
import com.wcapp.backend.dto.CardResponse
import com.wcapp.backend.repository.CardRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class CardService(
    private val cardRepository: CardRepository
) {
    @Transactional(readOnly = true)
    fun getAllCards(page: Int = 0, size: Int = 50, team: String? = null): CardListResponse {
        val pageable = PageRequest.of(page, size)

        val cardPage = if (team != null) {
            cardRepository.findByTeam(team, pageable)
        } else {
            cardRepository.findByActiveTrue(pageable)
        }

        return CardListResponse(
            cards = cardPage.content.map { it.toResponse() },
            totalPages = cardPage.totalPages,
            totalElements = cardPage.totalElements,
            currentPage = page,
            pageSize = size
        )
    }

    @Transactional(readOnly = true)
    fun getCardById(cardId: String): CardResponse {
        val card = cardRepository.findById(UUID.fromString(cardId))
            .orElseThrow { IllegalArgumentException("Carta no encontrada") }
        return card.toResponse()
    }

    private fun com.wcapp.backend.entity.Card.toResponse() = CardResponse(
        id = this.id.toString(),
        name = this.name,
        cardNumber = this.cardNumber,
        team = this.team,
        position = this.position,
        imageUrl = this.imageUrl,
        rarity = this.rarity.name,
        description = this.description,
        year = this.year,
        edition = this.edition
    )
}
