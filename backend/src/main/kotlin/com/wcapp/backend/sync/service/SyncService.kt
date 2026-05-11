package com.wcapp.backend.sync.service

import com.wcapp.backend.sync.dto.*
import com.wcapp.backend.sync.entity.SyncedCard
import com.wcapp.backend.sync.repository.SyncedCardRepository
import com.wcapp.backend.entity.User
import com.wcapp.backend.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class SyncService(
    private val syncedCardRepository: SyncedCardRepository,
    private val userRepository: UserRepository
) {
    @Transactional
    fun syncCards(userId: String, request: SyncRequest): SyncResponse {
        val uuid = UUID.fromString(userId)

        var duplicatesFound = 0
        var syncedCount = 0

        for (entry in request.cards) {
            var card = syncedCardRepository.findByCardCodeAndUserId(entry.cardCode, uuid)

            if (card != null) {
                card.quantity = entry.quantity
                card.isDuplicate = entry.isDuplicate
            } else {
                card = SyncedCard(
                    userId = uuid,
                    cardCode = entry.cardCode,
                    quantity = entry.quantity,
                    isDuplicate = entry.isDuplicate,
                    scanTimestamp = entry.scanTimestamp
                )
            }

            syncedCardRepository.save(card)
            syncedCount++
            if (entry.isDuplicate) duplicatesFound++
        }

        return SyncResponse(
            syncedCount = syncedCount,
            duplicatesFound = duplicatesFound
        )
    }

    fun getMatches(userId: String, neededCards: List<String>): MatchResponse {
        val uuid = UUID.fromString(userId)
        val matches = mutableListOf<MatchResult>()

        // Find users who have duplicates of cards we need
        val usersWithCards = syncedCardRepository.findUsersWithCards(neededCards)
            .filter { it.toString() != userId }  // Exclude self

        for (otherUserId in usersWithCards) {
            val otherUser = userRepository.findById(otherUserId).orElse(null) ?: continue
            val duplicates = syncedCardRepository.findByUserIdAndIsDuplicateTrue(otherUserId)
                .filter { it.cardCode in neededCards }

            if (duplicates.isEmpty()) continue

            // Find cards we have that they might need
            val ourCards = syncedCardRepository.findByUserIdAndIsDuplicateTrue(uuid)
            val theirCards = syncedCardRepository.findByUserId(otherUserId)
            val theirNeeds = theirCards.filter { !it.isDuplicate && it.quantity == 0 }
                .map { it.cardCode }

            val offeredCards = duplicates.map { it.cardCode }
            val mutualNeeds = ourCards.map { it.cardCode }.intersect(theirNeeds.toSet())

            val score = calculateScore(offeredCards.size, mutualNeeds.size)

            matches.add(MatchResult(
                userId = otherUserId.toString(),
                username = otherUser.username,
                displayName = otherUser.displayName,
                offeredCards = offeredCards,
                neededCards = mutualNeeds.toList(),
                matchScore = score
            ))
        }

        return MatchResponse(matches = matches.sortedByDescending { it.matchScore })
    }

    private fun calculateScore(offeredCount: Int, mutualCount: Int): Int {
        val base = offeredCount * 20
        val mutualBonus = mutualCount * 10
        return (base + mutualBonus).coerceIn(0, 100)
    }
}
