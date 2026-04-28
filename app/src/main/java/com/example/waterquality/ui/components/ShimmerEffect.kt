package com.example.waterquality.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Shimmer loading placeholder.
 *
 * Wraps any composable with a sweeping shimmer gradient.
 * Pass [isLoading] = true to show shimmer, false to show [content].
 *
 * Works on API 24+ — uses pure Compose canvas, no hardware-specific APIs.
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    isLoading: Boolean = true,
    shimmerBaseColor: Color = Color(0xFFE0E8F0),
    shimmerHighlightColor: Color = Color(0xFFF8FAFF),
    cornerRadius: Int = 16,
    content: @Composable () -> Unit = {}
) {
    if (!isLoading) {
        content()
        return
    }

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue   = 0f,
        targetValue    = 1000f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_anim"
    )

    val shimmerBrush = Brush.linearGradient(
        colors      = listOf(shimmerBaseColor, shimmerHighlightColor, shimmerBaseColor),
        start       = Offset(translateAnim - 300f, 0f),
        end         = Offset(translateAnim, 0f)
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius.dp))
            .background(shimmerBrush)
            .fillMaxSize()
    )
}

/**
 * Row-based shimmer skeleton for a list item.
 * Use inside a LazyColumn while data is loading.
 */
@Composable
fun ShimmerListItem(modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.Column(modifier = modifier) {
        ShimmerBox(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    Modifier.clip(RoundedCornerShape(16.dp))
                ),
            cornerRadius = 16
        )
    }
}
