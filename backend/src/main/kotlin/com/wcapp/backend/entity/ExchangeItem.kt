package com.wcapp.backend.entity

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "exchange_items")
data class ExchangeItem(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exchange_id", nullable = false)
    var exchange: Exchange,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    var card: Card,

    @Column(name = "offered_by", nullable = false)
    @Enumerated(EnumType.STRING)
    var offeredBy: ExchangeRole,

    @Column(nullable = false)
    var quantity: Int = 1
)

enum class ExchangeRole {
    REQUESTER,  // Carta ofrecida por quien solicita
    RECEIVER    // Carta ofrecida por quien recibe la solicitud
}
