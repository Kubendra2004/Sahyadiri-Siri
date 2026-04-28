package com.example.waterquality.ui.screens

import android.graphics.Paint
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.waterquality.data.model.WaterReport
import com.example.waterquality.ui.components.WaterQuality
import com.example.waterquality.ui.components.waterQualityFromReport
import com.example.waterquality.ui.theme.*
import com.example.waterquality.ui.utils.*
import com.example.waterquality.ui.viewmodel.WaterViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import java.util.Calendar

@Composable
fun HomeScreen(
    viewModel: WaterViewModel = hiltViewModel(),
    onReportClick: (String) -> Unit = {},
    onAddReportClick: () -> Unit = {},
    onMapClick: () -> Unit = {},
    onAdvisoriesClick: () -> Unit = {}
) {
    val lang    = LocalAppLanguage.current
    val reports by viewModel.reports.collectAsStateWithLifecycle()

    val cleanCount    = reports.count { waterQualityFromReport(it.clarity, it.smell) == WaterQuality.CLEAN }
    val moderateCount = reports.count { waterQualityFromReport(it.clarity, it.smell) == WaterQuality.MODERATE }
    val pollutedCount = reports.count { waterQualityFromReport(it.clarity, it.smell) == WaterQuality.POLLUTED }
    val avgScore = if (reports.isEmpty()) 68f else reports.map { r ->
        when (waterQualityFromReport(r.clarity, r.smell)) {
            WaterQuality.CLEAN    -> 70f + r.clarity * 6f
            WaterQuality.MODERATE -> 35f + r.clarity * 5f
            WaterQuality.POLLUTED -> r.clarity * 6f
        }
    }.average().toFloat().coerceIn(0f, 100f)

    LazyColumn(
        modifier       = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        item {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Brush.verticalGradient(GradientOceanColors))
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 16.dp, top = 16.dp, bottom = 80.dp)
            ) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Column {
                        Text(greetingFor(lang), style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold, color = Color.White)
                        Text(appStr(lang, "home_subtitle"), style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(.7f))
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Notifications, null, tint = Color.White,
                            modifier = Modifier.size(26.dp))
                    }
                }
            }
        }

        // ── WQI Hero card (overlaps header) ──────────────────────────────────
        item {
            Box(Modifier.fillMaxWidth().offset(y = (-64).dp).padding(horizontal = 16.dp)) {
                WqiHeroCard(avgScore, cleanCount, moderateCount, pollutedCount, lang)
            }
            Spacer(Modifier.height((-48).dp))
        }

        // ── AI Insights card ──────────────────────────────────────────────────
        item {
            Spacer(Modifier.height(12.dp))
            AiInsightsCard(avgScore, reports.size, cleanCount, pollutedCount, lang,
                modifier = Modifier.padding(horizontal = 16.dp))
        }

        // ── Water quality distribution chart ──────────────────────────────────
        item {
            Spacer(Modifier.height(16.dp))
            SectionHeader(appStr(lang, "home_wqi") + " Distribution", lang)
            Spacer(Modifier.height(10.dp))
            WaterQualityChart(cleanCount, moderateCount, pollutedCount,
                modifier = Modifier.padding(horizontal = 16.dp))
        }

        // ── Mini map ──────────────────────────────────────────────────────────
        item {
            Spacer(Modifier.height(16.dp))
            SectionHeader(appStr(lang, "home_region"), lang)
            Spacer(Modifier.height(10.dp))
            MiniMapCard(onMapClick = onMapClick,
                modifier = Modifier.padding(horizontal = 16.dp))
        }

        // ── Quick Actions (Report + Advisories only) ──────────────────────────
        item {
            Spacer(Modifier.height(16.dp))
            SectionHeader(appStr(lang, "home_actions"), lang)
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickCard(Modifier.weight(1f), Icons.Default.Add,
                    appStr(lang, "home_new_report"),
                    listOf(Color(0xFF023E8A), Color(0xFF0096C7)), onAddReportClick)
                QuickCard(Modifier.weight(1f), Icons.Default.AutoAwesome,
                    appStr(lang, "home_advisories"),
                    listOf(Color(0xFF6A0572), Color(0xFFB5179E)), onAdvisoriesClick)
            }
        }

        // ── Recent reports ────────────────────────────────────────────────────
        item {
            Spacer(Modifier.height(16.dp))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically
            ) {
                SectionHeader(appStr(lang, "home_recent"), lang)
                if (reports.isNotEmpty())
                    Text(appStr(lang, "home_see_all"),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(onClick = onMapClick))
            }
            Spacer(Modifier.height(8.dp))
        }

        if (reports.isEmpty()) {
            item {
                Column(Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.WaterDrop, null, tint = CleanBlue.copy(.4f),
                        modifier = Modifier.size(56.dp))
                    Spacer(Modifier.height(12.dp))
                    Text(appStr(lang, "home_empty"), style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(6.dp))
                    Text(appStr(lang, "home_empty_sub"), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onAddReportClick, shape = RoundedCornerShape(14.dp)) {
                        Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(appStr(lang, "home_new_report"))
                    }
                }
            }
        } else {
            itemsIndexed(reports.take(5), key = { _, r -> r.id }) { index, report ->
                var vis by remember { mutableStateOf(false) }
                val alpha by animateFloatAsState(if (vis) 1f else 0f,
                    tween(300, index * 70), label = "a$index")
                val ty by animateFloatAsState(if (vis) 0f else 40f,
                    spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium), label = "t$index")
                LaunchedEffect(Unit) { vis = true }
                Box(Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                    .graphicsLayer { this.alpha = alpha; translationY = ty }) {
                    ReportCard(report, lang) { onReportClick(report.id) }
                }
            }
        }
    }
}

// ── WQI Hero Card ─────────────────────────────────────────────────────────────
@Composable
private fun WqiHeroCard(
    score: Float, clean: Int, moderate: Int, polluted: Int, lang: String
) {
    val quality = when { score >= 70 -> WaterQuality.CLEAN; score >= 35 -> WaterQuality.MODERATE; else -> WaterQuality.POLLUTED }
    val (g1, g2) = when (quality) {
        WaterQuality.CLEAN    -> Color(0xFF03045E) to Color(0xFF0096C7)
        WaterQuality.MODERATE -> Color(0xFF5C3811) to Color(0xFFCA6702)
        WaterQuality.POLLUTED -> Color(0xFF6B0011) to Color(0xFFAE2012)
    }
    val statusLabel = when (quality) {
        WaterQuality.CLEAN -> appStr(lang, "status_clean"); WaterQuality.MODERATE -> appStr(lang, "status_moderate"); else -> appStr(lang, "status_polluted")
    }

    val animScore by animateFloatAsState(score, tween(1200), label = "score")
    val sweep = (animScore / 100f) * 240f

    Card(Modifier.fillMaxWidth().shadow(16.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp), elevation = CardDefaults.cardElevation(0.dp)) {
        Box(Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(g1, g2))).padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                // Arc Score
                Box(Modifier.size(110.dp), Alignment.Center) {
                    Canvas(Modifier.fillMaxSize()) {
                        val stroke = Stroke(10f, cap = StrokeCap.Round)
                        val start = 150f
                        drawArc(Color.White.copy(.2f), start, 240f, false, style = stroke)
                        drawArc(Color.White, start, sweep, false, style = stroke)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(animScore.toInt().toString(), style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Text("/100", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(.6f))
                    }
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(appStr(lang, "home_wqi"), style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(.7f))
                    Box(Modifier.background(Color.White.copy(.2f), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp)) {
                        Text(statusLabel, color = Color.White,
                            style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MiniStatPill("✅ $clean", Color.White)
                        MiniStatPill("⚠️ $moderate", Color.White)
                        MiniStatPill("🚨 $polluted", Color.White)
                    }
                }
            }
        }
    }
}

@Composable private fun MiniStatPill(text: String, color: Color) {
    Box(Modifier.background(color.copy(.18f), RoundedCornerShape(50))
        .border(1.dp, color.copy(.3f), RoundedCornerShape(50))
        .padding(horizontal = 8.dp, vertical = 3.dp)) {
        Text(text, style = MaterialTheme.typography.labelSmall, color = color,
            fontWeight = FontWeight.SemiBold)
    }
}

// ── AI Insights Card ─────────────────────────────────────────────────────────
@Composable
private fun AiInsightsCard(score: Float, total: Int, clean: Int, polluted: Int, lang: String, modifier: Modifier) {
    val quality = when { score >= 70 -> WaterQuality.CLEAN; score >= 35 -> WaterQuality.MODERATE; else -> WaterQuality.POLLUTED }

    // Bilingual insights
    val insight = if (lang == "ಕನ್ನಡ") {
        when (quality) {
            WaterQuality.CLEAN    -> "✅ ಬೆಂಗಳೂರು ಪ್ರದೇಶದಲ್ಲಿ ನೀರಿನ ಗುಣಮಟ್ಟ ಪ್ರಸ್ತುತ ಉತ್ತಮ ಸ್ಥಿತಿಯಲ್ಲಿದೆ. $total ಸಮುದಾಯ ವರದಿಗಳಲ್ಲಿ $clean ಶುದ್ಧ ನೀರನ್ನು ತೋರಿಸುತ್ತವೆ. ನಿಯಮಿತ ಮೇಲ್ವಿಚಾರಣೆ ಮುಂದುವರಿಸಿ."
            WaterQuality.MODERATE -> "⚠️ ಬೆಂಗಳೂರು ಪ್ರದೇಶದಲ್ಲಿ ನೀರಿನ ಗುಣಮಟ್ಟ ಮಧ್ಯಮ ಮಟ್ಟದ ಸ್ಥಿತಿ ತೋರಿಸುತ್ತಿದೆ. ಕೆಲವು ಮೂಲಗಳಿಗೆ ಬಳಕೆಗೆ ಮುನ್ನ ಶುದ್ಧೀಕರಣ ಅಗತ್ಯ. ಸಮುದಾಯ ಜಾಗರೂಕತೆ ಸೂಚಿಸಲಾಗಿದೆ."
            WaterQuality.POLLUTED -> "🚨 $polluted ವರದಿಗಳು ಕಲುಷಿತ ನೀರಿನ ಮೂಲಗಳನ್ನು ಸೂಚಿಸುತ್ತವೆ. ನೇರ ಸಂಪರ್ಕವನ್ನು ತಪ್ಪಿಸಿ. ಸ್ಥಳೀಯ ಅಧಿಕಾರಿಗಳಿಗೆ ತಕ್ಷಣ ತಿಳಿಸಿ."
        }
    } else {
        when (quality) {
            WaterQuality.CLEAN    -> "✅ Water quality in the Bengaluru region is currently in good condition. Out of $total community reports, $clean show clean water sources. Continue routine monitoring and report any sudden changes promptly to keep the community safe."
            WaterQuality.MODERATE -> "⚠️ Water quality shows moderate conditions across the Bengaluru region. Some water sources may require treatment before use. Community vigilance, regular testing, and boiling water before consumption is strongly advised."
            WaterQuality.POLLUTED -> "🚨 $polluted of $total reports indicate contaminated water sources in this region. Avoid direct contact with affected water bodies. Alert local municipal authorities and health departments immediately."
        }
    }

    val regionLabel = if (lang == "ಕನ್ನಡ") "ಬೆಂಗಳೂರು ಪ್ರದೇಶ" else "Bengaluru Region"
    val updatedLabel = if (lang == "ಕನ್ನಡ") "$total ವರದಿಗಳ ಆಧಾರದ ಮೇಲೆ · ಈಗಷ್ಟೇ ನವೀಕರಿಸಲಾಗಿದೆ" else "Based on $total community reports · Updated just now"
    val geminiLabel = if (lang == "ಕನ್ನಡ") "Gemini AI ಒಳನೋಟಗಳು" else "Gemini AI Insights"

    // Pulsing LIVE dot
    val pulse = rememberInfiniteTransition(label = "pulse")
    val dotAlpha by pulse.animateFloat(0.3f, 1f,
        infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "dot")
    val dotScale by pulse.animateFloat(0.8f, 1.2f,
        infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "dotScale")

    Card(modifier, shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(8.dp)) {
        Box(Modifier.fillMaxWidth().background(
            Brush.linearGradient(listOf(Color(0xFF0D1B2A), Color(0xFF1B2838), Color(0xFF0A3A5C))))) {
            // Subtle star-like background dots
            Canvas(Modifier.fillMaxWidth().height(160.dp).align(Alignment.TopEnd).alpha(0.12f)) {
                val dots = listOf(Offset(size.width*0.8f,20f), Offset(size.width*0.6f,60f),
                    Offset(size.width*0.9f,90f), Offset(size.width*0.7f,130f),
                    Offset(size.width*0.5f,30f), Offset(size.width*0.95f,50f))
                dots.forEach { drawCircle(Color.White, 2f, it) }
            }
            Column(Modifier.padding(18.dp)) {
                // Header row
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(36.dp).background(
                        Brush.radialGradient(listOf(Color(0xFF4CC9F0).copy(.4f), Color.Transparent)),
                        CircleShape), Alignment.Center) {
                        Icon(Icons.Default.AutoAwesome, null,
                            modifier = Modifier.size(20.dp), tint = Color(0xFF4CC9F0))
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(geminiLabel, style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF4CC9F0), fontWeight = FontWeight.Bold)
                    Spacer(Modifier.weight(1f))
                    // Pulsing LIVE pill
                    Row(Modifier.background(Color(0xFF4CC9F0).copy(.12f), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(7.dp).graphicsLayer { scaleX = dotScale; scaleY = dotScale }
                            .background(Color(0xFF4CC9F0).copy(dotAlpha), CircleShape))
                        Spacer(Modifier.width(5.dp))
                        Text("LIVE", style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4CC9F0), fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(12.dp))
                // Region label
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null,
                        modifier = Modifier.size(13.dp), tint = Color.White.copy(.4f))
                    Spacer(Modifier.width(4.dp))
                    Text(regionLabel, style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(.4f))
                }
                Spacer(Modifier.height(10.dp))
                // Insight divider line
                Box(Modifier.fillMaxWidth().height(1.dp)
                    .background(Brush.horizontalGradient(
                        listOf(Color(0xFF4CC9F0).copy(.5f), Color.Transparent))))
                Spacer(Modifier.height(10.dp))
                // Insight text
                Text(insight, style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                    color = Color.White.copy(.9f))
                Spacer(Modifier.height(14.dp))
                // Footer
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).background(Color(0xFF4CC9F0).copy(dotAlpha), CircleShape))
                    Spacer(Modifier.width(6.dp))
                    Text(updatedLabel, style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(.35f))
                }
            }
        }
    }
}


// ── Water Quality Distribution Chart ─────────────────────────────────────────
@Composable
private fun WaterQualityChart(clean: Int, moderate: Int, polluted: Int, modifier: Modifier) {
    val total = (clean + moderate + polluted).coerceAtLeast(1)
    val bars = listOf(
        Triple("Clean",    clean,    CleanBlue),
        Triple("Moderate", moderate, ModerateAmber),
        Triple("Polluted", polluted, PollutedRed)
    )
    val animProgress by animateFloatAsState(1f, tween(1000), label = "chart")

    Card(modifier, shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(4.dp)) {
        Column(Modifier.padding(18.dp)) {
            Text("Report Breakdown", style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(16.dp))
            bars.forEach { (label, count, color) ->
                val fraction = (count.toFloat() / total) * animProgress
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(label, Modifier.width(70.dp), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Box(Modifier.weight(1f).height(14.dp).background(
                        MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(7.dp))) {
                        Box(Modifier.fillMaxHeight().fillMaxWidth(fraction.coerceIn(0f,1f))
                            .background(Brush.horizontalGradient(listOf(color, color.copy(.6f))),
                                RoundedCornerShape(7.dp)))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("$count", Modifier.width(24.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold, color = color, textAlign = TextAlign.End)
                }
                Spacer(Modifier.height(10.dp))
            }
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                Text("Total Reports: $total",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${((clean.toFloat()/total)*100).toInt()}% Clean",
                    style = MaterialTheme.typography.labelSmall,
                    color = CleanBlue, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── Mini Map Preview ──────────────────────────────────────────────────────────
@Composable
private fun MiniMapCard(onMapClick: () -> Unit, modifier: Modifier) {
    Card(modifier.clickable(onClick = onMapClick),
        shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(6.dp)) {
        Box {
            // Non-interactive mini MapView
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(false)
                        isClickable = false
                        zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                        controller.setZoom(12.0)
                        controller.setCenter(GeoPoint(12.9716, 77.5946))
                    }
                },
                modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(20.dp))
            )
            // Tap overlay
            Box(Modifier.fillMaxWidth().height(180.dp)
                .background(Color.Transparent)
                .clip(RoundedCornerShape(20.dp))
                .clickable(onClick = onMapClick)) {
                Box(Modifier.align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(.6f))),
                        RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp)
                    )
                    .padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Map, null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("Tap to open full map", style = MaterialTheme.typography.labelMedium,
                            color = Color.White)
                        Spacer(Modifier.weight(1f))
                        Text("Bengaluru Region", style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(.7f))
                    }
                }
            }
        }
    }
}

// ── Quick Action Card ─────────────────────────────────────────────────────────
@Composable
private fun QuickCard(modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector,
                      label: String, gradient: List<Color>, onClick: () -> Unit) {
    Card(modifier.clickable(onClick = onClick), shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp)) {
        Box(Modifier.fillMaxWidth().background(Brush.verticalGradient(gradient))
            .padding(vertical = 20.dp), Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(48.dp).background(Color.White.copy(.2f), CircleShape),
                    Alignment.Center) {
                    Icon(icon, null, modifier = Modifier.size(24.dp), tint = Color.White)
                }
                Spacer(Modifier.height(10.dp))
                Text(label, style = MaterialTheme.typography.labelMedium, color = Color.White,
                    fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
            }
        }
    }
}

// ── Report Card ───────────────────────────────────────────────────────────────
@Composable
private fun ReportCard(report: WaterReport, lang: String, onClick: () -> Unit) {
    val q = waterQualityFromReport(report.clarity, report.smell)
    val (accent, bg) = when (q) {
        WaterQuality.CLEAN    -> CleanBlue to CleanBlue.copy(.07f)
        WaterQuality.MODERATE -> ModerateAmber to ModerateAmber.copy(.07f)
        WaterQuality.POLLUTED -> PollutedRed to PollutedRed.copy(.07f)
    }
    val score = when (q) {
        WaterQuality.CLEAN -> 70f + report.clarity * 6f
        WaterQuality.MODERATE -> 35f + report.clarity * 5f
        WaterQuality.POLLUTED -> report.clarity * 6f
    }
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(52.dp).background(accent.copy(.15f), CircleShape), Alignment.Center) {
                Text(score.toInt().toString(), style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold, color = accent)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("%.4f°N, %.4f°E".format(report.latitude, report.longitude),
                    style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(3.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${appStr(lang,"clarity")}: ${report.clarity}/5",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("•", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(report.flow, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(3.dp))
                Text(timeAgo(report.timestamp), style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(.6f))
            }
            Icon(Icons.Default.ChevronRight, null, modifier = Modifier.size(18.dp), tint = accent.copy(.5f))
        }
    }
}

// ── Section Header ────────────────────────────────────────────────────────────
@Composable
private fun SectionHeader(title: String, lang: String) {
    Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(4.dp).height(18.dp)
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)))
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

// ── Greeting ──────────────────────────────────────────────────────────────────
private fun greetingFor(lang: String): String {
    val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return appStr(lang, when { h < 12 -> "greet_morning"; h < 17 -> "greet_afternoon"; h < 21 -> "greet_evening"; else -> "greet_night" })
}
