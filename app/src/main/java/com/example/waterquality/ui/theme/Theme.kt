package com.example.waterquality.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ─── Light Colour Scheme ─────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary                = OceanBlue,
    onPrimary              = Color.White,
    primaryContainer       = CleanContainer,
    onPrimaryContainer     = NavyDeep,
    secondary              = TealGlow,
    onSecondary            = Color.White,
    secondaryContainer     = Color(0xFFCCF5F5),
    onSecondaryContainer   = TealDeep,
    tertiary               = CoralAmber,
    onTertiary             = Color.White,
    tertiaryContainer      = ModerateContainer,
    onTertiaryContainer    = CoralDeep,
    error                  = ErrorRed,
    onError                = OnErrorRed,
    errorContainer         = ErrorContainer,
    onErrorContainer       = OnErrorContainer,
    background             = BackgroundLight,
    onBackground           = OnSurfaceLight,
    surface                = SurfaceLight,
    onSurface              = OnSurfaceLight,
    surfaceVariant         = SurfaceVariantLight,
    onSurfaceVariant       = OnSurfaceVariantLight,
    outline                = OutlineLight,
    inverseSurface         = SurfaceDark,
    inverseOnSurface       = OnSurfaceDark,
    inversePrimary         = OceanBlueLight
)

// ─── Dark Colour Scheme ──────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary                = OceanBlueLight,
    onPrimary              = NavyDeep,
    primaryContainer       = Color(0xFF00497D),
    onPrimaryContainer     = Color(0xFFD1E4FF),
    secondary              = TealGlowDark,
    onSecondary            = TealDeep,
    secondaryContainer     = Color(0xFF004F4F),
    onSecondaryContainer   = Color(0xFF00FBFB),
    tertiary               = CoralAmberDark,
    onTertiary             = CoralDeep,
    tertiaryContainer      = Color(0xFF6E3900),
    onTertiaryContainer    = Color(0xFFFFDCC0),
    error                  = ErrorRedDark,
    onError                = OnErrorRedDark,
    errorContainer         = ErrorContainerDark,
    onErrorContainer       = OnErrorContainerDark,
    background             = BackgroundDark,
    onBackground           = OnSurfaceDark,
    surface                = SurfaceDark,
    onSurface              = OnSurfaceDark,
    surfaceVariant         = SurfaceVariantDark,
    onSurfaceVariant       = OnSurfaceVariantDark,
    outline                = OutlineDark,
    inverseSurface         = SurfaceLight,
    inverseOnSurface       = OnSurfaceLight,
    inversePrimary         = OceanBlue
)

/**
 * Root theme composable for Sahyadri-Siri.
 *
 * Dynamic color is intentionally disabled so our brand palette is always shown —
 * the water-quality status colours (blue/amber/red) must be consistent.
 * Edge-to-edge is enabled; status/nav bars are transparent.
 */
@Composable
fun WaterQualityTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content:   @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor     = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars     = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = SahyadriTypography,
        shapes      = SahyadriShapes,
        content     = content
    )
}
