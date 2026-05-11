package com.wcapp.backend.repository

import com.wcapp.backend.entity.Card
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CardRepository : JpaRepository<Card, UUID> {
    fun findByTeam(team: String, pageable: Pageable): Page<Card>
    fun findByRarity(rarity: String, pageable: Pageable): Page<Card>
    fun findByYear(year: Int, pageable: Pageable): Page<Card>
    fun findByTeamAndYear(team: String, year: Int, pageable: Pageable): Page<Card>
    fun findByCardNumber(cardNumber: Int): Card?
    fun findByActiveTrue(pageable: Pageable): Page<Card>
}
