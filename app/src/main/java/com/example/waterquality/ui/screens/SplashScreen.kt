package com.example.waterquality.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waterquality.ui.theme.GradientOceanColors
import com.example.waterquality.ui.theme.OceanBlue
import com.example.waterquality.ui.theme.TealGlow
import kotlinx.coroutines.delay

/**
 * Splash Screen
 *
 * Displays an animated water-ripple canvas, app logo text, and tagline.
 * Fades in → holds 1.5s → navigates to Home.
 *
 * Fully API 24+ compatible — no Lottie, pure Canvas + Compose animation.
 */
@Composable
fun SplashScreen(onNavigateToHome: () -> Unit) {

    // ── Animate in state ──────────────────────────────────────────────────────
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
        delay(2500L)
        onNavigateToHome()
    }

    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label         = "splash_alpha"
    )
    val scale by animateFloatAsState(
        targetValue   = if (visible) 1f else 0.85f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label         = "splash_scale"
    )

    // ── Ripple animation ──────────────────────────────────────────────────────
    val rippleTransition = rememberInfiniteTransition(label = "ripple")
    val ripple1 by rippleTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            tween(2000, easing = FastOutSlowInEasing), RepeatMode.Restart
        ), label = "r1"
    )
    val ripple2 by rippleTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            tween(2000, 400, easing = FastOutSlowInEasing), RepeatMode.Restart
        ), label = "r2"
    )
    val ripple3 by rippleTransition.animateFloat(
        initialValue  = 0f,
        targetValue   = 1f,
        animationSpec = infiniteRepeatable(
            tween(2000, 800, easing = FastOutSlowInEasing), RepeatMode.Restart
        ), label = "r3"
    )

    // ── Root container ────────────────────────────────────────────────────────
    Box(
        modifier          = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(GradientOceanColors)),
        contentAlignment  = Alignment.Center
    ) {
        // Ripple circles drawn on Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = size.minDimension * 0.7f
            listOf(ripple1, ripple2, ripple3).forEach { fraction ->
                drawCircle(
                    color  = Color.White.copy(alpha = 0.08f * (1f - fraction)),
                    radius = maxRadius * fraction,
                    center = center
                )
            }
        }

        // Logo + tagline
        Column(
            modifier              = Modifier.graphicsLayer {
                this.alpha  = alpha
                scaleX      = scale
                scaleY      = scale
            },
            horizontalAlignment   = Alignment.CenterHorizontally,
            verticalArrangement   = Arrangement.Center
        ) {
            // Drop icon (pure Canvas water drop)
            Canvas(modifier = Modifier.size(72.dp)) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                // Draw a simple water drop: filled circle + small top teardrop
                drawCircle(color = Color.White, radius = size.width * 0.35f, center = Offset(cx, cy + 4f))
                drawOval(
                    color  = Color.White,
                    topLeft = Offset(cx - size.width * 0.15f, 0f),
                    size   = androidx.compose.ui.geometry.Size(size.width * 0.3f, size.height * 0.55f)
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text       = "Sahyadri-Siri",
                fontSize   = 34.sp,
                fontWeight = FontWeight.Bold,
                color      = Color.White,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text  = "Community Water Quality Monitor",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.75f),
                fontWeight = FontWeight.Light,
                letterSpacing = 0.5.sp
            )

            Spacer(Modifier.height(48.dp))

            // Animated loading dots
            LoadingDots()
        }
    }
}

@Composable
private fun LoadingDots() {
    val transition = rememberInfiniteTransition(label = "dots")
    val dot1 by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(600), RepeatMode.Reverse), "d1")
    val dot2 by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(600, 200), RepeatMode.Reverse), "d2")
    val dot3 by transition.animateFloat(0f, 1f, infiniteRepeatable(tween(600, 400), RepeatMode.Reverse), "d3")

    androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(dot1, dot2, dot3).forEach { alpha ->
            Canvas(Modifier.size(8.dp)) {
                drawCircle(Color.White.copy(alpha = 0.4f + 0.6f * alpha))
            }
        }
    }
}
