package com.wcapp.backend.panini.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(
    name = "panini_cards",
    uniqueConstraints = [UniqueConstraint(columnNames = ["profile_id", "card_code"])]
)
data class PaniniCard(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    var profile: PaniniProfile,

    @Column(name = "card_code", nullable = false, length = 50)
    var cardCode: String,

    @Column(name = "card_name", length = 200)
    var cardName: String? = null,

    @Column(name = "team", length = 100)
    var team: String? = null,

    @Column(name = "is_duplicate", nullable = false)
    var isDuplicate: Boolean = false,

    @Column(name = "quantity", nullable = false)
    var quantity: Int = 1,

    @Column(name = "is_missing", nullable = false)
    var isMissing: Boolean = false,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
