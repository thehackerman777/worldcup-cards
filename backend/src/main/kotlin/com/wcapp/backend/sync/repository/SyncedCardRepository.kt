package com.wcapp.backend.sync.repository

import com.wcapp.backend.sync.entity.SyncedCard
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface SyncedCardRepository : JpaRepository<SyncedCard, UUID> {
    fun findByUserId(userId: UUID): List<SyncedCard>
    fun findByUserIdAndIsDuplicateTrue(userId: UUID): List<SyncedCard>
    fun findByCardCodeAndUserId(cardCode: String, userId: UUID): SyncedCard?
    fun findByCardCodeIn(cardCodes: List<String>): List<SyncedCard>

    @Query("SELECT DISTINCT sc.userId FROM SyncedCard sc WHERE sc.cardCode IN :neededCodes")
    fun findUsersWithCards(neededCodes: List<String>): List<UUID>

    @Query("SELECT sc.userId, sc.cardCode FROM SyncedCard sc WHERE sc.isDuplicate = true AND sc.cardCode IN :neededCodes")
    fun findDuplicatesForCards(neededCodes: List<String>): List<Array<Any>>
}
