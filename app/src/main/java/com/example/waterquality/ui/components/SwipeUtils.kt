package com.example.waterquality.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlin.math.abs
import kotlin.math.sign

/**
 * Applies horizontal drag resistance to any composable.
 *
 * The composable translates while dragged, with exponential resistance
 * (it feels like dragging through water — slows down the further you go).
 * On release it springs back to 0.
 *
 * [onDismiss] is triggered when [dismissThreshold] is exceeded.
 * Pass null to disable dismiss (pure spring-back rubber-band).
 *
 * Works on API 24+ — pure Compose, no platform-specific APIs.
 */
@Composable
fun Modifier.swipeWithResistance(
    enabled:          Boolean     = true,
    dismissThreshold: Float       = 300f,    // px
    resistanceFactor: Float       = 0.018f,  // higher = more resistance
    onDismiss:        (() -> Unit)?= null
): Modifier {
    if (!enabled) return this

    var rawOffset by remember { mutableFloatStateOf(0f) }

    // Apply exponential resistance: offset decelerates the further you drag
    fun resist(delta: Float): Float {
        val newRaw = rawOffset + delta
        return newRaw / (1f + abs(newRaw) * resistanceFactor)
    }

    val draggableState = rememberDraggableState { delta ->
        rawOffset += delta
        if (onDismiss != null && abs(rawOffset) > dismissThreshold) {
            onDismiss()
            rawOffset = 0f
        }
    }

    val resistedOffset by animateFloatAsState(
        targetValue   = if (rawOffset == 0f) 0f
                        else rawOffset / (1f + abs(rawOffset) * resistanceFactor),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "swipe_resistance",
        finishedListener = { rawOffset = 0f }
    )

    return this
        .draggable(
            state       = draggableState,
            orientation = Orientation.Horizontal,
            onDragStopped = { rawOffset = 0f }   // trigger spring-back
        )
        .graphicsLayer { translationX = resistedOffset }
}

/**
 * Vertical drag resistance — used on the Report bottom sheet
 * to add weighted resistance when pulling down to dismiss.
 *
 * [onDismiss] fires when the user drags down past [dismissThreshold].
 */
@Composable
fun Modifier.verticalSwipeResistance(
    enabled:          Boolean     = true,
    dismissThreshold: Float       = 250f,
    resistanceFactor: Float       = 0.012f,
    onDismiss:        (() -> Unit)?= null
): Modifier {
    if (!enabled) return this

    var rawOffset by remember { mutableFloatStateOf(0f) }

    val draggableState = rememberDraggableState { delta ->
        // Only allow downward drag (positive delta)
        if (delta > 0f) {
            rawOffset += delta
            if (onDismiss != null && rawOffset > dismissThreshold) {
                onDismiss()
                rawOffset = 0f
            }
        }
    }

    val resistedOffset by animateFloatAsState(
        targetValue   = rawOffset / (1f + abs(rawOffset) * resistanceFactor),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "vertical_resistance",
        finishedListener = { rawOffset = 0f }
    )

    return this
        .draggable(
            state       = draggableState,
            orientation = Orientation.Vertical,
            onDragStopped = { rawOffset = 0f }
        )
        .graphicsLayer { translationY = resistedOffset }
}
