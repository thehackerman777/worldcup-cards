package com.wcapp.backend.repository

import com.wcapp.backend.entity.UserCard
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserCardRepository : JpaRepository<UserCard, UUID> {
    fun findByUserId(userId: UUID): List<UserCard>
    fun findByUserIdAndCardId(userId: UUID, cardId: UUID): UserCard?
    fun findByUserIdAndIsInAlbumTrue(userId: UUID): List<UserCard>
    fun findByUserIdAndIsRepeatedTrue(userId: UUID): List<UserCard>
    fun findByUserIdAndTradeableTrue(userId: UUID): List<UserCard>
    fun findByCardId(cardId: UUID): List<UserCard>

    @Query("SELECT uc FROM UserCard uc WHERE uc.user.id = :userId AND uc.isRepeated = true AND uc.tradeable = true")
    fun findTradeableRepeatedByUserId(userId: UUID): List<UserCard>

    @Query("SELECT COUNT(uc) FROM UserCard uc WHERE uc.user.id = :userId AND uc.isInAlbum = true")
    fun countAlbumCardsByUserId(userId: UUID): Long

    fun existsByUserIdAndCardId(userId: UUID, cardId: UUID): Boolean
}
