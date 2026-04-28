package com.example.waterquality.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView

/**
 * BlurScrim — draws a blurred/dimmed overlay behind a bottom sheet.
 *
 * API 31+ (Android 12+): Applies RenderEffect blur to the ComposeView's parent
 * window so the content behind the sheet is physically blurred.
 *
 * API < 31 fallback: Renders a dark semi-transparent overlay that approximates
 * the frosted-glass look without GPU blur.
 *
 * [progress] is the sheet expand fraction: 0f = hidden, 1f = fully expanded.
 * The blur/alpha animates proportionally to [progress].
 *
 * USAGE: Place this composable at the root of your screen BEFORE the BottomSheet.
 * It will only be visible while [progress] > 0f.
 */
@Composable
fun BlurScrim(
    progress: Float,
    modifier: Modifier = Modifier
) {
    if (progress <= 0f) return

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // API 31+: Real RenderEffect blur on the window
        val view = LocalView.current
        DisposableEffect(progress) {
            val sigma = (progress * 20f).coerceIn(0f, 20f)
            view.setRenderEffect(
                android.graphics.RenderEffect.createBlurEffect(
                    sigma, sigma,
                    android.graphics.Shader.TileMode.MIRROR
                )
            )
            onDispose {
                // Clear blur when sheet is dismissed
                view.setRenderEffect(null)
            }
        }
        // Minimal tinted overlay on top
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.15f * progress))
        )
    } else {
        // API < 31: Translucent dark overlay as frosted-glass approximation
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f * progress))
        )
    }
}
