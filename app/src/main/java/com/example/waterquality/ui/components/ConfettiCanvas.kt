package com.example.waterquality.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class ConfettiParticle(
    val x: Float, val y: Float,
    val vx: Float, val vy: Float,
    val rotation: Float, val rotationSpeed: Float,
    val color: Color, val size: Float
)

/**
 * Full-screen confetti burst that plays for [durationMs] and then triggers [onFinished].
 */
@Composable
fun ConfettiCanvas(
    modifier: Modifier = Modifier,
    durationMs: Int = 2500,
    onFinished: () -> Unit = {}
) {
    val colors = listOf(
        Color(0xFF4CC9F0), Color(0xFF4361EE), Color(0xFF7209B7),
        Color(0xFF3A0CA3), Color(0xFFF72585), Color(0xFF06D6A0),
        Color(0xFFFFD60A), Color(0xFFFF6B6B)
    )
    val particles = remember {
        List(80) {
            val angle = Random.nextFloat() * 360f
            val speed = Random.nextFloat() * 12f + 4f
            ConfettiParticle(
                x = 0.5f, y = 0.4f,
                vx = cos(Math.toRadians(angle.toDouble())).toFloat() * speed,
                vy = sin(Math.toRadians(angle.toDouble())).toFloat() * speed,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 10f,
                color = colors.random(),
                size = Random.nextFloat() * 12f + 6f
            )
        }
    }

    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMs, easing = LinearEasing),
        finishedListener = { onFinished() },
        label = "confetti"
    )

    Canvas(modifier.fillMaxSize()) {
        particles.forEach { p ->
            val t  = progress
            val px = (p.x + p.vx * t * 0.04f) * size.width
            val py = (p.y + p.vy * t * 0.04f + t * t * 18f) * size.height
            val alpha = (1f - t).coerceIn(0f, 1f)
            if (py < size.height) {
                drawCircle(p.color.copy(alpha), p.size / 2f, Offset(px, py))
            }
        }
    }
}
