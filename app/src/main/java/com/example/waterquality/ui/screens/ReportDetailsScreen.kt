package com.example.waterquality.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.waterquality.ui.theme.GradientOceanColors
import com.example.waterquality.ui.utils.formatTimestamp

@Composable
fun ReportDetailsScreen(
    reportId: String,
    onNavigateBack: () -> Unit = {}
) {
    // In production, this would be driven by a ViewModel lookup.
    // For now it shows a structured detail layout ready for data injection.
    val placeholder = WaterReport(
        id        = reportId,
        clarity   = 4,
        smell     = "Normal",
        flow      = "Medium",
        latitude  = 12.9716,
        longitude = 77.5946,
        imagePath = null,
        timestamp = System.currentTimeMillis()
    )

    ReportDetailContent(report = placeholder, onBack = onNavigateBack)
}

@Composable
fun ReportDetailContent(report: WaterReport, onBack: () -> Unit = {}) {
    val quality = waterQualityFromReport(report.clarity, report.smell)
    val score   = when (quality) {
        WaterQuality.CLEAN    -> 70f + report.clarity * 6f
        WaterQuality.MODERATE -> 35f + report.clarity * 5f
        WaterQuality.POLLUTED -> report.clarity * 6f
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(GradientOceanColors))
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                }
                Text("Report Details",
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
        Card(
            modifier  = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape     = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WaterDrop, null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Water Analysis",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(16.dp))
                WaterStatusChip(quality)
                Spacer(Modifier.height(16.dp))

                DetailRow("Clarity",   "${report.clarity} / 5")
                DetailRow("Smell",     report.smell)
                DetailRow("Flow Rate", report.flow)
                DetailRow("Location",  "%.4f, %.4f".format(report.latitude, report.longitude))
                DetailRow("Reported",  formatTimestamp(report.timestamp))
                DetailRow("Status",    report.status)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Advisory card
        val advisory = when (quality) {
            WaterQuality.CLEAN    -> "✅ This water source appears to be in good condition. Safe for non-potable use such as irrigation. Continue routine monitoring."
            WaterQuality.MODERATE -> "⚠️ Reduced water clarity detected. Avoid drinking without proper treatment. Boil or filter before potable use."
            WaterQuality.POLLUTED -> "🚨 Possible contamination detected. Avoid all direct contact. Notify your local water authority immediately."
        }

        Card(
            modifier  = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("AI Advisory",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                Text(advisory, style = MaterialTheme.typography.bodyMedium)
            }
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
