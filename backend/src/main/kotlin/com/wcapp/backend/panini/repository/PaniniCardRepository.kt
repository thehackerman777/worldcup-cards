package com.wcapp.backend.panini.repository

import com.wcapp.backend.panini.entity.PaniniCard
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PaniniCardRepository : JpaRepository<PaniniCard, UUID> {
    fun findByProfileId(profileId: UUID): List<PaniniCard>
    fun findByProfileIdAndIsDuplicateTrue(profileId: UUID): List<PaniniCard>
    fun findByProfileIdAndIsMissingTrue(profileId: UUID): List<PaniniCard>
    fun findByProfileIdAndCardCode(profileId: UUID, cardCode: String): PaniniCard?
    fun findByProfileIdAndCardCodeIn(profileId: UUID, cardCodes: List<String>): List<PaniniCard>
}
