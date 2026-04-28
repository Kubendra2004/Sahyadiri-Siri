package com.example.waterquality.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Modifier extension that performs a LIGHT haptic tick on click.
 * Use for chips, toggles, FAB, navigation items.
 */
fun Modifier.hapticClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val haptic = LocalHapticFeedback.current
    val source = remember { MutableInteractionSource() }
    this.clickable(
        interactionSource = source,
        indication        = null,
        enabled           = enabled
    ) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        onClick()
    }
}

/**
 * Standalone helper — call from any composable scope when you need
 * haptic feedback without a Modifier (e.g. inside a LaunchedEffect or callback).
 *
 * Usage:
 *   val haptic = LocalHapticFeedback.current
 *   Button(onClick = { haptic.heavyTick() }) { … }
 */
fun androidx.compose.ui.hapticfeedback.HapticFeedback.lightTick() =
    performHapticFeedback(HapticFeedbackType.TextHandleMove)

fun androidx.compose.ui.hapticfeedback.HapticFeedback.heavyTick() =
    performHapticFeedback(HapticFeedbackType.LongPress)
