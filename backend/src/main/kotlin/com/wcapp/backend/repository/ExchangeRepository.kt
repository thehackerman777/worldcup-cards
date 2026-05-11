package com.wcapp.backend.repository

import com.wcapp.backend.entity.Exchange
import com.wcapp.backend.entity.ExchangeStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ExchangeRepository : JpaRepository<Exchange, UUID> {
    fun findByRequesterId(userId: UUID): List<Exchange>
    fun findByReceiverId(userId: UUID): List<Exchange>
    fun findByRequesterIdOrReceiverId(requesterId: UUID, receiverId: UUID): List<Exchange>
    fun findByReceiverIdAndStatus(userId: UUID, status: ExchangeStatus): List<Exchange>
    fun findByRequesterIdAndStatus(userId: UUID, status: ExchangeStatus): List<Exchange>
    fun findByStatus(status: ExchangeStatus): List<Exchange>
}
