package com.wcapp.backend.service

import com.wcapp.backend.dto.*
import com.wcapp.backend.entity.*
import com.wcapp.backend.repository.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
class ExchangeService(
    private val exchangeRepository: ExchangeRepository,
    private val exchangeItemRepository: ExchangeItemRepository,
    private val userRepository: UserRepository,
    private val cardRepository: CardRepository,
    private val userCardRepository: UserCardRepository
) {
    @Transactional
    fun createExchange(requesterId: String, request: CreateExchangeRequest): ExchangeResponse {
        val requester = userRepository.findById(UUID.fromString(requesterId))
            .orElseThrow { IllegalArgumentException("Usuario no encontrado") }
        val receiver = userRepository.findById(UUID.fromString(request.receiverId))
            .orElseThrow { IllegalArgumentException("Destinatario no encontrado") }

        if (requesterId == request.receiverId) {
            throw IllegalArgumentException("No puedes intercambiar contigo mismo")
        }

        // Validate requester has the offered cards
        validateUserCards(requesterId, request.offeredCards)

        val exchange = Exchange(
            requester = requester,
            receiver = receiver,
            status = ExchangeStatus.PENDING,
            message = request.message
        )
        val savedExchange = exchangeRepository.save(exchange)

        // Create exchange items for offered cards
        val offeredItems = request.offeredCards.map { entry ->
            val card = cardRepository.findById(UUID.fromString(entry.cardId))
                .orElseThrow { IllegalArgumentException("Carta no encontrada: ${entry.cardId}") }
            ExchangeItem(
                exchange = savedExchange,
                card = card,
                offeredBy = ExchangeRole.REQUESTER,
                quantity = entry.quantity
            )
        }
        exchangeItemRepository.saveAll(offeredItems)

        // Create exchange items for requested cards
        val requestedItems = request.requestedCards.map { entry ->
            val card = cardRepository.findById(UUID.fromString(entry.cardId))
                .orElseThrow { IllegalArgumentException("Carta no encontrada: ${entry.cardId}") }
            ExchangeItem(
                exchange = savedExchange,
                card = card,
                offeredBy = ExchangeRole.RECEIVER,
                quantity = entry.quantity
            )
        }
        exchangeItemRepository.saveAll(requestedItems)

        // Update requester stats
        requester.totalExchanges += 1
        userRepository.save(requester)

        return buildExchangeResponse(savedExchange)
    }

    @Transactional(readOnly = true)
    fun getUserExchanges(userId: String): List<ExchangeResponse> {
        val uuid = UUID.fromString(userId)
        val exchanges = exchangeRepository.findByRequesterIdOrReceiverId(uuid, uuid)
        return exchanges.map { buildExchangeResponse(it) }
    }

    @Transactional(readOnly = true)
    fun getAvailableExchanges(userId: String): List<ExchangeResponse> {
        val uuid = UUID.fromString(userId)
        val pending = exchangeRepository.findByStatus(ExchangeStatus.PENDING)
        return pending
            .filter { it.requester.id.toString() != userId }
            .map { buildExchangeResponse(it) }
    }

    @Transactional
    fun acceptExchange(exchangeId: String, userId: String): ExchangeResponse {
        val exchange = exchangeRepository.findById(UUID.fromString(exchangeId))
            .orElseThrow { IllegalArgumentException("Intercambio no encontrado") }

        if (exchange.receiver.id.toString() != userId) {
            throw IllegalStateException("Solo el destinatario puede aceptar este intercambio")
        }
        if (exchange.status != ExchangeStatus.PENDING) {
            throw IllegalStateException("El intercambio ya no está pendiente")
        }

        // Validate receiver has the requested cards
        val items = exchangeItemRepository.findByExchangeId(exchange.id!!)
        val requestedCards = items.filter { it.offeredBy == ExchangeRole.RECEIVER }
        validateUserCards(userId, requestedCards.map {
            ExchangeCardEntry(it.card.id.toString(), it.quantity)
        })

        exchange.status = ExchangeStatus.ACCEPTED
        val updated = exchangeRepository.save(exchange)

        return buildExchangeResponse(updated)
    }

    @Transactional
    fun rejectExchange(exchangeId: String, userId: String): ExchangeResponse {
        val exchange = exchangeRepository.findById(UUID.fromString(exchangeId))
            .orElseThrow { IllegalArgumentException("Intercambio no encontrado") }

        if (exchange.receiver.id.toString() != userId) {
            throw IllegalStateException("Solo el destinatario puede rechazar este intercambio")
        }
        if (exchange.status != ExchangeStatus.PENDING) {
            throw IllegalStateException("El intercambio ya no está pendiente")
        }

        exchange.status = ExchangeStatus.REJECTED
        val updated = exchangeRepository.save(exchange)

        return buildExchangeResponse(updated)
    }

    @Transactional
    fun completeExchange(exchangeId: String, userId: String): ExchangeResponse {
        val exchange = exchangeRepository.findById(UUID.fromString(exchangeId))
            .orElseThrow { IllegalArgumentException("Intercambio no encontrado") }

        val isRequester = exchange.requester.id.toString() == userId
        val isReceiver = exchange.receiver.id.toString() == userId

        if (!isRequester && !isReceiver) {
            throw IllegalStateException("No eres parte de este intercambio")
        }
        if (exchange.status != ExchangeStatus.ACCEPTED) {
            throw IllegalStateException("El intercambio debe estar aceptado para completarse")
        }

        // Execute the exchange: swap cards between users
        val items = exchangeItemRepository.findByExchangeId(exchange.id!!)

        for (item in items) {
            when (item.offeredBy) {
                ExchangeRole.REQUESTER -> {
                    // Remove from requester, add to receiver
                    removeAndAddCard(exchange.requester.id.toString(), exchange.receiver.id.toString(), item)
                }
                ExchangeRole.RECEIVER -> {
                    // Remove from receiver, add to requester
                    removeAndAddCard(exchange.receiver.id.toString(), exchange.requester.id.toString(), item)
                }
            }
        }

        exchange.status = ExchangeStatus.COMPLETED
        exchange.completedAt = LocalDateTime.now()

        // Update stats
        exchange.requester.completedExchanges += 1
        exchange.receiver.completedExchanges += 1
        userRepository.save(exchange.requester)
        userRepository.save(exchange.receiver)

        val updated = exchangeRepository.save(exchange)
        return buildExchangeResponse(updated)
    }

    private fun removeAndAddCard(fromUserId: String, toUserId: String, item: ExchangeItem) {
        val fromUuid = UUID.fromString(fromUserId)
        val toUuid = UUID.fromString(toUserId)

        // Decrease from owner
        val fromCard = userCardRepository.findByUserIdAndCardId(fromUuid, item.card.id!!)
            ?: throw IllegalStateException("El usuario ya no tiene la carta ofrecida")

        if (fromCard.quantity < item.quantity) {
            throw IllegalStateException("Cantidad insuficiente de la carta: ${item.card.name}")
        }

        fromCard.quantity -= item.quantity
        fromCard.isRepeated = fromCard.quantity > 1
        if (fromCard.quantity == 0) {
            userCardRepository.delete(fromCard)
        } else {
            userCardRepository.save(fromCard)
        }

        // Add to receiver
        var toCard = userCardRepository.findByUserIdAndCardId(toUuid, item.card.id!!)
        if (toCard != null) {
            toCard.quantity += item.quantity
            toCard.isRepeated = toCard.quantity > 1
        } else {
            toCard = UserCard(
                user = userRepository.getReferenceById(toUuid),
                card = item.card,
                quantity = item.quantity,
                isInAlbum = false,
                isRepeated = false,
                tradeable = false
            )
        }
        userCardRepository.save(toCard)
    }

    private fun validateUserCards(userId: String, cards: List<ExchangeCardEntry>) {
        val uuid = UUID.fromString(userId)
        for (entry in cards) {
            val userCard = userCardRepository.findByUserIdAndCardId(uuid, UUID.fromString(entry.cardId))
                ?: throw IllegalArgumentException("No tienes la carta: ${entry.cardId}")
            if (userCard.quantity < entry.quantity) {
                throw IllegalArgumentException("No tienes suficientes copias de: ${entry.cardId}")
            }
        }
    }

    private fun buildExchangeResponse(exchange: Exchange): ExchangeResponse {
        val items = exchangeItemRepository.findByExchangeId(exchange.id!!)
        val offeredCards = items.filter { it.offeredBy == ExchangeRole.REQUESTER }
        val requestedCards = items.filter { it.offeredBy == ExchangeRole.RECEIVER }

        return ExchangeResponse(
            id = exchange.id.toString(),
            requester = ExchangeUserInfo(
                id = exchange.requester.id.toString(),
                username = exchange.requester.username,
                displayName = exchange.requester.displayName
            ),
            receiver = ExchangeUserInfo(
                id = exchange.receiver.id.toString(),
                username = exchange.receiver.username,
                displayName = exchange.receiver.displayName
            ),
            status = exchange.status.name,
            message = exchange.message,
            offeredCards = offeredCards.map { it.toItemResponse() },
            requestedCards = requestedCards.map { it.toItemResponse() },
            createdAt = exchange.createdAt.toString(),
            updatedAt = exchange.updatedAt.toString()
        )
    }

    private fun ExchangeItem.toItemResponse() = ExchangeItemResponse(
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
        offeredBy = this.offeredBy.name
    )
}
