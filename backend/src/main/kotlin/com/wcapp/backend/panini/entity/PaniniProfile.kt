package com.wcapp.backend.panini.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "panini_profiles")
data class PaniniProfile(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "nickname", nullable = false, unique = true, length = 100)
    var nickname: String,

    @Column(name = "display_name", length = 150)
    var displayName: String? = null,

    @Column(name = "avatar_url", length = 500)
    var avatarUrl: String? = null,

    @Column(name = "is_public", nullable = false)
    var isPublic: Boolean = false,

    @Column(name = "completion_percentage", nullable = false)
    var completionPercentage: Int = 0,

    @Column(name = "total_cards", nullable = false)
    var totalCards: Int = 0,

    @Column(name = "total_collection", nullable = false)
    var totalCollection: Int = 0,

    @Column(name = "last_sync", nullable = false)
    var lastSync: LocalDateTime = LocalDateTime.now(),

    @Column(name = "sync_count", nullable = false)
    var syncCount: Int = 0,

    @Column(name = "active", nullable = false)
    var active: Boolean = true,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
