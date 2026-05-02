package com.example.waterquality.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.waterquality.ui.theme.CleanBlue
import com.example.waterquality.ui.theme.CleanContainer
import com.example.waterquality.ui.theme.ModerateAmber
import com.example.waterquality.ui.theme.ModerateContainer
import com.example.waterquality.ui.theme.PollutedContainer
import com.example.waterquality.ui.theme.PollutedRed
import com.example.waterquality.ui.utils.LocalAppLanguage
import com.example.waterquality.ui.utils.appStr

enum class WaterQuality { CLEAN, MODERATE, POLLUTED }

/**
 * Pill-shaped status chip that shows the water quality level.
 *
 * Colour animates with a spring when [quality] changes.
 * Sized for inline use inside cards and list items.
 */
@Composable
fun WaterStatusChip(
    quality:  WaterQuality,
    modifier: Modifier = Modifier
) {
    val lang = LocalAppLanguage.current
    val (label, fg, bg) = when (quality) {
        WaterQuality.CLEAN    -> Triple(appStr(lang, "status_clean"),    CleanBlue,    CleanContainer)
        WaterQuality.MODERATE -> Triple(appStr(lang, "status_moderate"), ModerateAmber, ModerateContainer)
        WaterQuality.POLLUTED -> Triple(appStr(lang, "status_polluted"), PollutedRed,  PollutedContainer)
    }

    val animatedBg by animateColorAsState(
        targetValue   = bg,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label         = "chip_bg"
    )
    val animatedFg by animateColorAsState(
        targetValue   = fg,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label         = "chip_fg"
    )

    Surface(
        modifier = modifier,
        color    = animatedBg,
        shape    = RoundedCornerShape(50)    // pill
    ) {
        Text(
            text      = label,
            modifier  = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style     = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color     = animatedFg
        )
    }
}

/** Map a report's clarity + smell to a [WaterQuality] level. */
fun waterQualityFromReport(clarity: Int, smell: String): WaterQuality = when {
    clarity >= 4 && smell == "Normal" -> WaterQuality.CLEAN
    clarity >= 2 && smell == "Normal" -> WaterQuality.MODERATE
    else                              -> WaterQuality.POLLUTED
}

/** Map a 0–100 score to [WaterQuality]. */
fun waterQualityFromScore(score: Float): WaterQuality = when {
    score >= 65f -> WaterQuality.CLEAN
    score >= 35f -> WaterQuality.MODERATE
    else         -> WaterQuality.POLLUTED
}
