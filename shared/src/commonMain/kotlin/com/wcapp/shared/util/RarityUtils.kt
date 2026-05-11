package com.wcapp.shared.util

object RarityUtils {
    fun rarityColor(rarity: String): String {
        return when (rarity.uppercase()) {
            "COMMON" -> "#9E9E9E"    // Gray
            "UNCOMMON" -> "#4CAF50"   // Green
            "RARE" -> "#2196F3"       // Blue
            "LEGENDARY" -> "#FF9800"  // Orange / Gold
            else -> "#9E9E9E"
        }
    }

    fun rarityLabel(rarity: String): String {
        return when (rarity.uppercase()) {
            "COMMON" -> "Común"
            "UNCOMMON" -> "Poco Común"
            "RARE" -> "Rara"
            "LEGENDARY" -> "Legendaria"
            else -> rarity
        }
    }

    fun rarityStars(rarity: String): Int {
        return when (rarity.uppercase()) {
            "COMMON" -> 1
            "UNCOMMON" -> 2
            "RARE" -> 3
            "LEGENDARY" -> 5
            else -> 1
        }
    }
}
