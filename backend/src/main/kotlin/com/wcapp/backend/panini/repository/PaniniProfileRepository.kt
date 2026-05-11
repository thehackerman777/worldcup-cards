package com.wcapp.backend.panini.repository

import com.wcapp.backend.panini.entity.PaniniProfile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PaniniProfileRepository : JpaRepository<PaniniProfile, UUID> {
    fun findByNicknameIgnoreCase(nickname: String): PaniniProfile?
    fun findByActiveTrue(): List<PaniniProfile>
    fun existsByNicknameIgnoreCase(nickname: String): Boolean
}
