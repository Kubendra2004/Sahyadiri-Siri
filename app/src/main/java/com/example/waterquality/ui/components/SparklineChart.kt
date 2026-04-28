package com.example.waterquality.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.waterquality.ui.theme.CleanBlue

/**
 * A mini sparkline chart for the last-N data points.
 * Draws a smooth path with gradient fill and animated entrance.
 */
@Composable
fun SparklineChart(
    points: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = CleanBlue,
    fillAlpha: Float = 0.15f
) {
    if (points.size < 2) return

    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1000, easing = EaseInOutCubic),
        label = "spark"
    )

    val minVal = points.min()
    val maxVal = points.max().coerceAtLeast(minVal + 1f)

    Canvas(modifier.fillMaxWidth().height(60.dp)) {
        val step  = size.width / (points.size - 1)
        val range = maxVal - minVal

        // Build path up to animProgress
        val visibleCount = (points.size * animProgress).toInt().coerceAtLeast(2)
        val path = Path()
        val fillPath = Path()

        points.take(visibleCount).forEachIndexed { i, v ->
            val x = i * step
            val y = size.height - ((v - minVal) / range) * size.height * 0.85f
            if (i == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, size.height)
                fillPath.lineTo(x, y)
            } else {
                // Smooth cubic bezier
                val prevX = (i - 1) * step
                val prevY = size.height - ((points[i - 1] - minVal) / range) * size.height * 0.85f
                val cx = (prevX + x) / 2f
                path.cubicTo(cx, prevY, cx, y, x, y)
                fillPath.cubicTo(cx, prevY, cx, y, x, y)
            }
        }
        fillPath.lineTo((visibleCount - 1) * step, size.height)
        fillPath.close()

        // Fill gradient under line
        drawPath(fillPath, Brush.verticalGradient(
            listOf(lineColor.copy(fillAlpha), lineColor.copy(0f))))

        // Line
        drawPath(path, lineColor, style = Stroke(width = 3f, cap = StrokeCap.Round,
            join = StrokeJoin.Round))

        // End dot
        val lastX = (visibleCount - 1) * step
        val lastY = size.height - ((points[visibleCount - 1] - minVal) / range) * size.height * 0.85f
        drawCircle(lineColor, 5f, Offset(lastX, lastY))
        drawCircle(lineColor.copy(.25f), 10f, Offset(lastX, lastY))
    }
}
