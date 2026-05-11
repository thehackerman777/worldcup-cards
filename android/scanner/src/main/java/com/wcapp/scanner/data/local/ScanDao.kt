package com.wcapp.scanner.data.local

import androidx.room.*
import com.wcapp.scanner.data.model.ScannedCard
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanDao {
    @Query("SELECT * FROM scanned_cards ORDER BY scanTimestamp DESC")
    fun getAllScans(): Flow<List<ScannedCard>>

    @Query("SELECT * FROM scanned_cards WHERE synced = 0 ORDER BY scanTimestamp ASC")
    suspend fun getUnsyncedScans(): List<ScannedCard>

    @Query("SELECT * FROM scanned_cards WHERE cardCode = :code LIMIT 1")
    suspend fun getByCardCode(code: String): ScannedCard?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(card: ScannedCard): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<ScannedCard>)

    @Query("UPDATE scanned_cards SET synced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Long>)

    @Query("SELECT COUNT(*) FROM scanned_cards")
    fun totalScanned(): Flow<Int>

    @Query("SELECT COUNT(*) FROM scanned_cards WHERE isDuplicate = 1")
    fun totalDuplicates(): Flow<Int>

    @Query("DELETE FROM scanned_cards WHERE synced = 1")
    suspend fun clearSynced()
}
