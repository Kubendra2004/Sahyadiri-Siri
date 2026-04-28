package com.example.waterquality.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.waterquality.ui.theme.CleanBlue
import com.example.waterquality.ui.theme.ModerateAmber
import com.example.waterquality.ui.theme.PollutedRed

/**
 * Animated circular score meter.
 *
 * Draws an arc from 0 → [score]/100 using Compose Canvas.
 * Arc colour transitions from PollutedRed → ModerateAmber → CleanBlue.
 * Animates in on first composition.
 *
 * API 24+ compatible — pure Canvas, no hardware-specific APIs.
 */
@Composable
fun WaterScoreMeter(
    score:     Float,          // 0f – 100f
    modifier:  Modifier = Modifier,
    size:      Dp       = 120.dp,
    strokeWidth: Dp     = 10.dp
) {
    var animTarget by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(score) { animTarget = score }

    val animatedScore by animateFloatAsState(
        targetValue   = animTarget,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label         = "score_meter"
    )

    val arcColor = when {
        animatedScore >= 70f -> CleanBlue
        animatedScore >= 40f -> ModerateAmber
        else                 -> PollutedRed
    }

    val trackColor = arcColor.copy(alpha = 0.15f)

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val padding = strokeWidth.toPx() / 2
            val arcSize = Size(
                this.size.width  - strokeWidth.toPx(),
                this.size.height - strokeWidth.toPx()
            )
            val topLeft = Offset(padding, padding)

            // Track (background arc)
            drawArc(
                color       = trackColor,
                startAngle  = 135f,
                sweepAngle  = 270f,
                useCenter   = false,
                topLeft     = topLeft,
                size        = arcSize,
                style       = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )

            // Progress arc
            drawArc(
                brush       = Brush.sweepGradient(
                    colors     = listOf(arcColor.copy(alpha = 0.4f), arcColor),
                    center     = Offset(this.size.width / 2, this.size.height / 2)
                ),
                startAngle  = 135f,
                sweepAngle  = 270f * (animatedScore / 100f),
                useCenter   = false,
                topLeft     = topLeft,
                size        = arcSize,
                style       = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }

        // Score label
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text       = "${animatedScore.toInt()}",
                style      = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color      = arcColor
            )
            Text(
                text  = "/ 100",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
