package com.brewkery.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Exact colours from the live prototype at vivekshah138.github.io/Brewkery ──
// CSS variables:  --espresso:#140b07  --terracotta:#d9532f
//                 --crema:#fdfaf7     --border:#ebd8cb
// Amber accent (#f59e0b) used for highlights and "Proceed to Checkout" text.

val Espresso        = Color(0xFF140B07)   // deepest dark-background coffee
val Terracotta      = Color(0xFFD9532F)   // primary brand red-orange
val TerracottaDark  = Color(0xFF8C2B12)   // darker shade for pressed states
val Crema           = Color(0xFFFDFAF7)   // app background (off-white cream)
val Border          = Color(0xFFEBD8CB)   // dividers and chip borders
val BannerFrom      = Color(0xFFFAEEE5)   // gradient start on store banner
val BannerTo        = Color(0xFFF7EBE1)   // gradient end on store banner
val Amber           = Color(0xFFF59E0B)   // accent — "Proceed to Checkout", stars
val MutedBrown      = Color(0xFF786457)   // secondary text / captions
val DarkText        = Color(0xFF231710)   // primary on-light text (slightly richer than espresso)
val ErrorRed        = Color(0xFFB00020)

private val BrewkeryColorScheme = lightColorScheme(
    // Primary = Terracotta (#D9532F) — used for buttons, badges, active chips
    primary              = Terracotta,
    onPrimary            = Color.White,
    primaryContainer     = BannerFrom,         // banner / soft-terracotta tint
    onPrimaryContainer   = Espresso,

    // Secondary = Espresso (#140B07) — used for selected chips, cart bar background
    secondary            = Espresso,
    onSecondary          = Color.White,
    secondaryContainer   = Color(0xFFFFEDD5),  // soft amber tint for tags
    onSecondaryContainer = Espresso,

    // Tertiary = Amber (#F59E0B) — used for accent text and highlights
    tertiary             = Amber,
    onTertiary           = Espresso,
    tertiaryContainer    = Color(0xFFFFF3CD),
    onTertiaryContainer  = Espresso,

    background           = Crema,             // #FDFAF7
    onBackground         = DarkText,          // #231710

    surface              = Color.White,
    onSurface            = DarkText,
    surfaceVariant       = BannerTo,          // slightly warmer off-white
    onSurfaceVariant     = MutedBrown,        // #786457

    outline              = Border,            // #EBD8CB
    error                = ErrorRed,
)

@Composable
fun BrewkeryTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BrewkeryColorScheme,
        content     = content,
    )
}
