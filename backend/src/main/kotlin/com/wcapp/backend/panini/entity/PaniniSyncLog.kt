package com.wcapp.backend.panini.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "panini_sync_logs")
data class PaniniSyncLog(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "nickname", nullable = false, length = 100)
    var nickname: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: SyncStatus = SyncStatus.SUCCESS,

    @Column(name = "cards_found")
    var cardsFound: Int = 0,

    @Column(name = "duplicates_found")
    var duplicatesFound: Int = 0,

    @Column(name = "error_message", columnDefinition = "TEXT")
    var errorMessage: String? = null,

    @Column(name = "response_time_ms")
    var responseTimeMs: Long = 0,

    @Column(name = "from_cache", nullable = false)
    var fromCache: Boolean = false,

    @CreationTimestamp
    @Column(name = "synced_at", nullable = false, updatable = false)
    val syncedAt: LocalDateTime = LocalDateTime.now()
)

enum class SyncStatus {
    SUCCESS, PARTIAL, FAILED, FROM_CACHE
}
