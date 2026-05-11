package com.wcapp.backend.service

import com.wcapp.backend.dto.*
import com.wcapp.backend.entity.UserCard
import com.wcapp.backend.repository.CardRepository
import com.wcapp.backend.repository.UserCardRepository
import com.wcapp.backend.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class AlbumService(
    private val userRepository: UserRepository,
    private val cardRepository: CardRepository,
    private val userCardRepository: UserCardRepository
) {
    @Transactional(readOnly = true)
    fun getAlbum(userId: String): AlbumResponse {
        val user = userRepository.findById(UUID.fromString(userId))
            .orElseThrow { IllegalArgumentException("Usuario no encontrado") }

        val albumCards = userCardRepository.findByUserIdAndIsInAlbumTrue(user.id!!)
        val repeatedCards = userCardRepository.findByUserIdAndIsRepeatedTrue(user.id!!)
        val totalDefinitions = cardRepository.count()

        return AlbumResponse(
            userId = user.id.toString(),
            username = user.username,
            totalCards = totalDefinitions.toInt(),
            albumCards = albumCards.size.toLong(),
            repeatedCards = repeatedCards.size,
            completionPercentage = if (totalDefinitions > 0) {
                (albumCards.size.toDouble() / totalDefinitions) * 100
            } else 0.0,
            cards = albumCards.map { it.toResponse() }
        )
    }

    @Transactional
    fun addCardToAlbum(userId: String, request: AddCardRequest): UserCardResponse {
        val user = userRepository.findById(UUID.fromString(userId))
            .orElseThrow { IllegalArgumentException("Usuario no encontrado") }
        val card = cardRepository.findById(UUID.fromString(request.cardId))
            .orElseThrow { IllegalArgumentException("Carta no encontrada") }

        var userCard = userCardRepository.findByUserIdAndCardId(user.id!!, card.id!!)

        if (userCard != null) {
            userCard.quantity += request.quantity
            userCard.isRepeated = userCard.quantity > 1
            userCard.isInAlbum = true
        } else {
            userCard = UserCard(
                user = user,
                card = card,
                quantity = request.quantity,
                isInAlbum = true,
                isRepeated = request.quantity > 1,
                tradeable = request.quantity > 1
            )
        }

        return userCardRepository.save(userCard).toResponse()
    }

    @Transactional(readOnly = true)
    fun getRepeatedCards(userId: String): List<UserCardResponse> {
        return userCardRepository
            .findByUserIdAndIsRepeatedTrue(UUID.fromString(userId))
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getTradeableCards(userId: String): List<UserCardResponse> {
        return userCardRepository
            .findTradeableRepeatedByUserId(UUID.fromString(userId))
            .map { it.toResponse() }
    }

    @Transactional
    fun removeCardFromAlbum(userId: String, userCardId: String) {
        val userCard = userCardRepository.findById(UUID.fromString(userCardId))
            .orElseThrow { IllegalArgumentException("Carta no encontrada en tu colección") }

        if (userCard.user.id.toString() != userId) {
            throw IllegalStateException("No puedes modificar cartas de otro usuario")
        }

        if (userCard.quantity > 1) {
            userCard.quantity -= 1
            userCard.isRepeated = userCard.quantity > 1
            userCard.isInAlbum = userCard.quantity > 0
            userCardRepository.save(userCard)
        } else {
            userCardRepository.delete(userCard)
        }
    }

    private fun UserCard.toResponse() = UserCardResponse(
        id = this.id.toString(),
        card = CardResponse(
            id = this.card.id.toString(),
            name = this.card.name,
            cardNumber = this.card.cardNumber,
            team = this.card.team,
            position = this.card.position,
            imageUrl = this.card.imageUrl,
            rarity = this.card.rarity.name,
            description = this.card.description,
            year = this.card.year,
            edition = this.card.edition
        ),
        quantity = this.quantity,
        isInAlbum = this.isInAlbum,
        isRepeated = this.isRepeated,
        tradeable = this.tradeable
    )
}
