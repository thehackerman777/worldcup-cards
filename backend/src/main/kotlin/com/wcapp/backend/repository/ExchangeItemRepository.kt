package com.wcapp.backend.repository

import com.wcapp.backend.entity.ExchangeItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ExchangeItemRepository : JpaRepository<ExchangeItem, UUID> {
    fun findByExchangeId(exchangeId: UUID): List<ExchangeItem>
    fun findByExchangeIdAndOfferedBy(exchangeId: UUID, offeredBy: String): List<ExchangeItem>
}
