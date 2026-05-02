package com.example.waterquality.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Brand Core ────────────────────────────────────────────────────────────────
val OceanBlue       = Color(0xFF006EBE)   // primary – deep, saturated water blue
val OceanBlueLight  = Color(0xFF4DA6FF)   // primary dark theme
val NavyDeep        = Color(0xFF003566)   // on-primary dark text on light bg

val TealGlow        = Color(0xFF00B4B4)   // secondary – teal shimmer
val TealGlowDark    = Color(0xFF00E5FF)
val TealDeep        = Color(0xFF003737)

val CoralAmber      = Color(0xFFE87722)   // tertiary – warm contrast accent
val CoralAmberDark  = Color(0xFFFFB86A)
val CoralDeep       = Color(0xFF4A1900)

// ─── Water Quality Status ────────────────────────────────────────────────────
val CleanBlue       = Color(0xFF0D90FF)   // good water
val ModerateAmber   = Color(0xFFF5A623)   // moderate
val PollutedRed     = Color(0xFFE53935)   // polluted
val CleanContainer  = Color(0xFFD6EEFF)
val ModerateContainer = Color(0xFFFFF3CD)
val PollutedContainer = Color(0xFFFFDED4)

// ─── Deep Ocean / Glass Tokens ─────────────────────────────────────────────
val DeepOceanDark        = Color(0xFF001A33)
val DeepOceanMediumDark  = Color(0xFF003366)
val DeepOceanLight       = Color(0xFFEAF4FF)
val DeepOceanMediumLight = Color(0xFFD6E9FF)

val VibrantCyanDark  = Color(0xFF00FFFF)
val VibrantCyanLight = Color(0xFF00A3C7)

val DarkGlassBase    = Color(0xFF19202C)
val SurfaceTintDark  = Color(0xFFA7C8FF)
val SurfaceTintLight = Color(0xFF2B4C6F)

// ─── Surfaces / Background ──────────────────────────────────────────────────
// Light
val SurfaceLight        = Color(0xFFF6FAFF)
val SurfaceVariantLight = Color(0xFFE2F0FF)
val BackgroundLight     = DeepOceanLight
val OnSurfaceLight      = Color(0xFF111827)
val OnSurfaceVariantLight = Color(0xFF3D5068)
val OutlineLight        = Color(0xFFB0C8E8)

// Dark
val SurfaceDark         = Color(0xFF0D1B2A)
val SurfaceVariantDark  = Color(0xFF152435)
val BackgroundDark      = DeepOceanDark
val OnSurfaceDark       = Color(0xFFE8F1FF)
val OnSurfaceVariantDark = Color(0xFFA8C4E0)
val OutlineDark         = Color(0xFF2D4A66)

// ─── Glass / Overlay ─────────────────────────────────────────────────────────
val GlassLight      = Color(0xCCFFFFFF)   // 80% white glass
val GlassDark       = Color(0x33FFFFFF)   // 20% white glass

// ─── Error ───────────────────────────────────────────────────────────────────
val ErrorRed        = Color(0xFFBA1A1A)
val ErrorContainer  = Color(0xFFFFDAD6)
val OnErrorRed      = Color(0xFFFFFFFF)
val OnErrorContainer = Color(0xFF410002)

val ErrorRedDark        = Color(0xFFFFB4AB)
val ErrorContainerDark  = Color(0xFF93000A)
val OnErrorRedDark      = Color(0xFF690005)
val OnErrorContainerDark = Color(0xFFFFDAD6)

// ─── Gradients (for use with Brush) ─────────────────────────────────────────
// Use these in screens directly: Brush.linearGradient(GradientOceanColors)
val GradientOceanColors      = listOf(Color(0xFF0A4B8C), Color(0xFF006EBE), Color(0xFF00A8CC))
val GradientOceanLightColors = listOf(Color(0xFFEAF6FF), Color(0xFFCFE6FF), Color(0xFF99CCFF))
val GradientSunsetColors  = listOf(Color(0xFFE87722), Color(0xFFF5A623), Color(0xFFFFD166))
val GradientAlertColors   = listOf(Color(0xFFBA1A1A), Color(0xFFE53935))
val GradientCardDark      = listOf(Color(0xFF152435), Color(0xFF0D1B2A))
