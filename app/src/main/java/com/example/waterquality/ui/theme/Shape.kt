package com.example.waterquality.ui.theme

import androidx.compose.ui.unit.dp
import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape

/**
 * Custom shape scale for Sahyadri-Siri.
 * Rounded corners everywhere — from tight chips (8dp) to full cards (28dp).
 */
val SahyadriShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),   // tiny chips, tooltips
    small      = RoundedCornerShape(10.dp),  // small buttons, tags
    medium     = RoundedCornerShape(16.dp),  // cards, inputs, dialogs
    large      = RoundedCornerShape(24.dp),  // bottom sheet handle, flash cards
    extraLarge = RoundedCornerShape(32.dp)   // FAB, hero elements
)
