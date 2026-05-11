package com.wcapp.backend.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "exchanges")
data class Exchange(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    var requester: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    var receiver: User,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var status: ExchangeStatus = ExchangeStatus.PENDING,

    @Column(name = "message", columnDefinition = "TEXT")
    var message: String? = null,

    @Column(name = "completed_at")
    var completedAt: LocalDateTime? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class ExchangeStatus {
    PENDING,    // Solicitud enviada
    ACCEPTED,   // Aceptado, pendiente de concretar
    COMPLETED,  // Intercambio completado
    REJECTED,   // Rechazado
    CANCELLED   // Cancelado por el solicitante
}
