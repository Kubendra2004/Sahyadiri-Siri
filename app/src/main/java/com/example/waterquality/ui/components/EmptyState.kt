package com.example.waterquality.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.WaterDrop

/**
 * Full-screen empty / error state.
 *
 * Slides up with a spring animation on first composition.
 * Shows an [icon], [title], optional [subtitle], and an optional [onRetry] button.
 */
@Composable
fun EmptyState(
    modifier:  Modifier     = Modifier,
    icon:      ImageVector  = Icons.Outlined.WaterDrop,
    title:     String       = "No data yet",
    subtitle:  String?      = null,
    retryText: String?      = null,
    onRetry:   (() -> Unit)?= null
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible    = visible,
        enter      = fadeIn() + slideInVertically(
            initialOffsetY = { it / 3 },
            animationSpec  = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness    = Spring.StiffnessLow
            )
        )
    ) {
        Column(
            modifier            = modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = null,
                modifier           = Modifier.size(80.dp),
                tint               = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text      = title,
                style     = MaterialTheme.typography.titleLarge,
                color     = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            if (subtitle != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text      = subtitle,
                    style     = MaterialTheme.typography.bodyMedium,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            if (onRetry != null && retryText != null) {
                Spacer(Modifier.height(32.dp))
                Button(onClick = onRetry, shape = MaterialTheme.shapes.medium) {
                    Text(retryText)
                }
            }
        }
    }
}

/** Convenience: offline / network error variant */
@Composable
fun OfflineState(modifier: Modifier = Modifier, onRetry: () -> Unit) {
    EmptyState(
        modifier  = modifier,
        icon      = Icons.Outlined.CloudOff,
        title     = "No Connection",
        subtitle  = "Check your internet and try again.",
        retryText = "Retry",
        onRetry   = onRetry
    )
}
