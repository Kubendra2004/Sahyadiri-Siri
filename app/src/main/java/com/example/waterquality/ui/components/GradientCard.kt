package com.example.waterquality.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * GradientCard — reusable card with a subtle gradient overlay behind its content.
 *
 * The gradient sits on the container colour so it's always on-brand.
 * Rounds to [cornerRadius], casts a [shadowElevation] shadow.
 */
@Composable
fun GradientCard(
    modifier:        Modifier  = Modifier,
    cornerRadius:    Dp        = 20.dp,
    shadowElevation: Dp        = 4.dp,
    gradientColors:  List<Color> = listOf(
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.surface
    ),
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    if (onClick != null) {
        Card(
            modifier  = modifier,
            shape     = shape,
            elevation = CardDefaults.cardElevation(defaultElevation = shadowElevation),
            onClick   = onClick
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(gradientColors))
                    .padding(20.dp),
                content = content
            )
        }
    } else {
        Card(
            modifier  = modifier,
            shape     = shape,
            elevation = CardDefaults.cardElevation(defaultElevation = shadowElevation)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(gradientColors))
                    .padding(20.dp),
                content = content
            )
        }
    }
}
