package com.wcapp.backend.sync.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "synced_cards")
data class SyncedCard(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "user_id", nullable = false)
    var userId: UUID,

    @Column(name = "card_code", nullable = false, length = 50)
    var cardCode: String,

    @Column(nullable = false)
    var quantity: Int = 1,

    @Column(name = "is_duplicate", nullable = false)
    var isDuplicate: Boolean = false,

    @Column(name = "scan_timestamp")
    var scanTimestamp: Long? = null,

    @CreationTimestamp
    @Column(name = "synced_at", nullable = false, updatable = false)
    val syncedAt: LocalDateTime = LocalDateTime.now()
)
