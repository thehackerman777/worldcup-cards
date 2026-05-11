package com.wcapp.backend.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "cards")
data class Card(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(nullable = false, length = 100)
    var name: String,

    @Column(name = "card_number", nullable = false)
    var cardNumber: Int,

    @Column(nullable = false, length = 100)
    var team: String,

    @Column(length = 50)
    var position: String? = null,

    @Column(length = 500)
    var imageUrl: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var rarity: Rarity = Rarity.COMMON,

    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    @Column(nullable = false)
    var year: Int = 2026,

    @Column(nullable = false)
    var edition: String = "Mundial 2026",

    @Column(nullable = false)
    var active: Boolean = true,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class Rarity {
    COMMON,    // Cartas normales
    UNCOMMON,  // Menos comunes
    RARE,      // Estrellas / jugadores destacados
    LEGENDARY  // Leyendas / edición especial
}
