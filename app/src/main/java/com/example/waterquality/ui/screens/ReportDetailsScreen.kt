package com.example.waterquality.ui.screens

import com.example.waterquality.ui.utils.LocalAppLanguage
import com.example.waterquality.ui.utils.appStr
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.waterquality.data.model.WaterReport
import com.example.waterquality.ui.components.WaterQuality
import com.example.waterquality.ui.components.WaterScoreMeter
import com.example.waterquality.ui.components.WaterStatusChip
import com.example.waterquality.ui.components.waterQualityFromReport
import com.example.waterquality.ui.components.GlassCard
import com.example.waterquality.ui.theme.SahyadriTheme
import com.example.waterquality.ui.utils.formatTimestamp
import com.example.waterquality.ui.utils.flowLabel
import com.example.waterquality.ui.utils.reportStatusLabel
import com.example.waterquality.ui.utils.smellLabel

@Composable
fun ReportDetailsScreen(
    reportId: String,
    onNavigateBack: () -> Unit = {}
) {
    val lang = LocalAppLanguage.current
    // In production, this would be driven by a ViewModel lookup.
    // For now it shows a structured detail layout ready for data injection.
    val placeholder = WaterReport(
        id        = reportId,
        userId    = "",
        clarity   = 4,
        smell     = "Normal",
        flow      = "Medium",
        latitude  = 12.9716,
        longitude = 77.5946,
        imagePath = null,
        timestamp = System.currentTimeMillis()
    )

    ReportDetailContent(report = placeholder, onBack = onNavigateBack, lang = lang)
}

@Composable
fun ReportDetailContent(report: WaterReport, onBack: () -> Unit = {}, lang: String) {
    val glass = SahyadriTheme.glassColors
    val quality = waterQualityFromReport(report.clarity, report.smell)
    val score   = when (quality) {
        WaterQuality.CLEAN    -> 70f + report.clarity * 6f
        WaterQuality.MODERATE -> 35f + report.clarity * 5f
        WaterQuality.POLLUTED -> report.clarity * 6f
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(glass.oceanDeep)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(glass.oceanGradient))
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, appStr(lang, "rep_back"), tint = Color.White)
                }
                Text(appStr(lang, "det_title"),
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Spacer(Modifier.height(20.dp))

        // Score
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            WaterScoreMeter(score = score, size = 140.dp)
        }

        Spacer(Modifier.height(24.dp))

        // Status + Details card
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.WaterDrop,
                    null,
                    tint = glass.accent,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(appStr(lang, "det_analysis"),
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(16.dp))
            WaterStatusChip(quality)
            Spacer(Modifier.height(16.dp))

            DetailRow(appStr(lang, "clarity"),   "${report.clarity} / 5")
            DetailRow(appStr(lang, "smell"),     smellLabel(lang, report.smell))
            DetailRow(appStr(lang, "flow"),      flowLabel(lang, report.flow))
            DetailRow(appStr(lang, "location"),  "%.4f, %.4f".format(report.latitude, report.longitude))
            DetailRow(appStr(lang, "reported"),  formatTimestamp(report.timestamp))
            DetailRow(appStr(lang, "status"),    reportStatusLabel(lang, report.status))
        }

        Spacer(Modifier.height(16.dp))

        // Advisory card
        val advisory = when (quality) {
            WaterQuality.CLEAN    -> appStr(lang, "det_advisory_clean")
            WaterQuality.MODERATE -> appStr(lang, "det_advisory_moderate")
            WaterQuality.POLLUTED -> appStr(lang, "det_advisory_polluted")
        }

        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(20.dp)
        ) {
            Text(appStr(lang, "det_ai_advisory"),
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color      = glass.accent)
            Spacer(Modifier.height(8.dp))
            Text(advisory, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
    ) {
        Text(label,
            style  = MaterialTheme.typography.bodyMedium,
            color  = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium)
        Text(value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface)
    }
}
