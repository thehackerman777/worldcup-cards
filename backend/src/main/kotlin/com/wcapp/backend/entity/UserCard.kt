package com.wcapp.backend.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(
    name = "user_cards",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "card_id"])]
)
data class UserCard(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    var card: Card,

    @Column(nullable = false)
    var quantity: Int = 1,

    @Column(name = "is_in_album", nullable = false)
    var isInAlbum: Boolean = false,

    @Column(name = "is_repeated", nullable = false)
    var isRepeated: Boolean = false,

    @Column(nullable = false)
    var tradeable: Boolean = false,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
