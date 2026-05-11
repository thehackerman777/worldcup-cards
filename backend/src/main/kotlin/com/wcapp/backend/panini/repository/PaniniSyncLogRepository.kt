package com.wcapp.backend.panini.repository

import com.wcapp.backend.panini.entity.PaniniSyncLog
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PaniniSyncLogRepository : JpaRepository<PaniniSyncLog, UUID> {
    fun findByNicknameOrderBySyncedAtDesc(nickname: String): List<PaniniSyncLog>
    fun findTop10ByNicknameOrderBySyncedAtDesc(nickname: String): List<PaniniSyncLog>
}
