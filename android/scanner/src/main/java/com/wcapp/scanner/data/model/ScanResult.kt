package com.wcapp.scanner.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scanned_cards")
data class ScannedCard(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cardCode: String,           // e.g. "FWC-001"
    val cardName: String? = null,   // Detected name if available
    val quantity: Int = 1,          // Detected quantity
    val isDuplicate: Boolean = false, // quantity > 1
    val scanTimestamp: Long = System.currentTimeMillis(),
    val synced: Boolean = false     // Whether sent to backend
)

data class ScanSession(
    val totalScanned: Int = 0,
    val duplicatesFound: Int = 0,
    val lastScanTime: Long = System.currentTimeMillis()
)
