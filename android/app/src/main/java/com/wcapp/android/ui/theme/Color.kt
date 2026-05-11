package com.wcapp.android.ui.theme

import androidx.compose.ui.graphics.Color

// ── Light Palette ─────────────────────────────────────
val md_theme_light_primary = Color(0xFF1A6B52)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFA7F2D3)
val md_theme_light_onPrimaryContainer = Color(0xFF002117)
val md_theme_light_secondary = Color(0xFF4C6359)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFCEE9DB)
val md_theme_light_onSecondaryContainer = Color(0xFF092017)
val md_theme_light_tertiary = Color(0xFF3F6373)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFC3E8FB)
val md_theme_light_onTertiaryContainer = Color(0xFF001F29)
val md_theme_light_error = Color(0xFFBA1A1A)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_background = Color(0xFFFBFDF8)
val md_theme_light_onBackground = Color(0xFF191C1A)
val md_theme_light_surface = Color(0xFFFBFDF8)
val md_theme_light_onSurface = Color(0xFF191C1A)
val md_theme_light_surfaceVariant = Color(0xFFDBE5DD)
val md_theme_light_onSurfaceVariant = Color(0xFF404943)
val md_theme_light_outline = Color(0xFF707973)
val md_theme_light_outlineVariant = Color(0xFFBFC9C2)

// ── Dark Palette ──────────────────────────────────────
val md_theme_dark_primary = Color(0xFF8BD6B8)
val md_theme_dark_onPrimary = Color(0xFF00382A)
val md_theme_dark_primaryContainer = Color(0xFF00503D)
val md_theme_dark_onPrimaryContainer = Color(0xFFA7F2D3)
val md_theme_dark_secondary = Color(0xFFB3CCBF)
val md_theme_dark_onSecondary = Color(0xFF1F352C)
val md_theme_dark_secondaryContainer = Color(0xFF354B42)
val md_theme_dark_onSecondaryContainer = Color(0xFFCEE9DB)
val md_theme_dark_tertiary = Color(0xFFA7CCDF)
val md_theme_dark_onTertiary = Color(0xFF083543)
val md_theme_dark_tertiaryContainer = Color(0xFF254B5A)
val md_theme_dark_onTertiaryContainer = Color(0xFFC3E8FB)
val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_background = Color(0xFF191C1A)
val md_theme_dark_onBackground = Color(0xFFE1E3DF)
val md_theme_dark_surface = Color(0xFF191C1A)
val md_theme_dark_onSurface = Color(0xFFE1E3DF)
val md_theme_dark_surfaceVariant = Color(0xFF404943)
val md_theme_dark_onSurfaceVariant = Color(0xFFBFC9C2)
val md_theme_dark_outline = Color(0xFF89938C)
val md_theme_dark_outlineVariant = Color(0xFF404943)

// ── Card Rarity Colors ────────────────────────────────
object RarityColors {
    val common = Color(0xFF9E9E9E)
    val uncommon = Color(0xFF4CAF50)
    val rare = Color(0xFF2196F3)
    val legendary = Color(0xFFFF9800)

    fun forRarity(rarity: String): Color = when (rarity.uppercase()) {
        "COMMON" -> common
        "UNCOMMON" -> uncommon
        "RARE" -> rare
        "LEGENDARY" -> legendary
        else -> common
    }
}
