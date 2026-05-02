package com.example.waterquality.ui.screens

import android.content.Intent
import android.graphics.Paint
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.waterquality.data.model.WaterReport
import com.example.waterquality.ui.components.GlassCard
import com.example.waterquality.ui.components.waterQualityFromReport
import com.example.waterquality.ui.components.WaterQuality
import com.example.waterquality.ui.theme.*
import com.example.waterquality.ui.utils.LocalAppLanguage
import com.example.waterquality.ui.utils.appStr
import com.example.waterquality.ui.viewmodel.WaterViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Wave Path generation
private fun Path.wavePath(width: Float, height: Float, phase: Float, fillPercentage: Float) {
    reset()
    val waterLevel = height * (1f - fillPercentage)
    moveTo(0f, height)
    lineTo(0f, waterLevel)

    val waveFrequency = 1.5f
    val waveAmplitude = height * 0.05f

    for (x in 0..width.toInt() step 5) {
        val y = waterLevel + Math.sin((x * waveFrequency * Math.PI / width) + phase).toFloat() * waveAmplitude
        lineTo(x.toFloat(), y)
    }

    lineTo(width, height)
    close()
}

@Composable
private fun LiquidFillWqiMeter(score: Float, lang: String, modifier: Modifier = Modifier) {
    val glass = SahyadriTheme.glassColors
    val fillPercentage = (score / 100f).coerceIn(0f, 1f)
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart),
        label = "phase"
    )

    Box(
        modifier = modifier
            .size(160.dp)
            .clip(CircleShape)
            .background(glass.glassSurface.copy(alpha = 0.6f))
            .border(2.dp, glass.accent.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val wavePath = Path().apply {
                wavePath(canvasWidth, canvasHeight, phase, fillPercentage)
            }
            
            // Draw background liquid
            val bgPhase = phase + Math.PI.toFloat() / 2f
            val bgPath = Path().apply { wavePath(canvasWidth, canvasHeight, bgPhase, fillPercentage) }
            drawPath(bgPath, color = glass.oceanMedium.copy(alpha = 0.6f))
            
            // Draw foreground liquid
            drawPath(wavePath, color = glass.accent.copy(alpha = 0.8f))
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = score.toInt().toString(),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = appStr(lang, "home_wqi_short"),
                style = MaterialTheme.typography.labelMedium,
                color = glass.surfaceTint,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = SahyadriTheme.glassColors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(glass.oceanMedium.copy(alpha = 0.6f), glass.oceanDeep.copy(alpha = 0.9f))
                )
            )
            .border(1.dp, glass.accent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = label, tint = glass.accent, modifier = Modifier.size(28.dp))
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: WaterViewModel = hiltViewModel(),
    onNavigateToReport: () -> Unit = {},
    onNavigateToMap: () -> Unit = {},
    onNavigateToAdvisories: () -> Unit = {},
    onNavigateToReportDetails: (String) -> Unit = {}
) {
    val lang = LocalAppLanguage.current
    val glass = SahyadriTheme.glassColors
    val reports by viewModel.reports.collectAsStateWithLifecycle()
    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle(initialValue = true)
    
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val total = reports.size
    val clean = reports.count { waterQualityFromReport(it.clarity, it.smell) == WaterQuality.CLEAN }
    val polluted = reports.count { waterQualityFromReport(it.clarity, it.smell) == WaterQuality.POLLUTED }
    
    val overallScore = if (total == 0) 0f else {
        ((clean.toFloat() / total) * 100f).coerceIn(0f, 100f)
    }

    val safePct = if (total == 0) 0 else (clean * 100) / total
    val regionStr = appStr(lang, "home_region")
    val geminiInsightRaw = appStr(lang, "home_gemini_insight")
    val insightText = String.format(geminiInsightRaw, regionStr, "$safePct%")

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                viewModel.refresh()
                delay(1000)
                isRefreshing = false
            }
        },
        modifier = modifier.fillMaxSize().background(glass.oceanDeep)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 100.dp) // Bottom nav padding
        ) {
            // -- Header & WQI Section --
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(glass.oceanGradient)
                    )
            ) {
                // Ambient background glow
                Canvas(Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(glass.accent.copy(alpha = 0.2f), Color.Transparent),
                            center = Offset(size.width / 2, 0f),
                            radius = size.width * 0.8f
                        )
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            val greeting = appStr(lang, "greet_morning") // Dynamic in real app
                            Text(
                                "$greeting, ${appStr(lang, "home_user")}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                appStr(lang, "home_subtitle"),
                                style = MaterialTheme.typography.bodyMedium,
                                color = glass.surfaceTint
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(glass.glassSurface)
                                .border(1.dp, glass.accent.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = glass.accent)
                        }
                    }

                    Spacer(Modifier.height(40.dp))
                    
                    LiquidFillWqiMeter(score = overallScore, lang = lang)
                    
                    Spacer(Modifier.height(16.dp))
                    Text(
                        appStr(lang, "home_wqi").uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }

            // -- Offline Banner --
            if (!isOnline) {
                Row(
                    Modifier.fillMaxWidth().background(Color(0xFF8B0000)).padding(12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.WifiOff, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(appStr(lang, "home_offline"), color = Color.White, style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.height(24.dp))

            // -- Gemini AI Insights --
            Column(Modifier.padding(horizontal = 24.dp)) {
                GlassCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, null, tint = glass.accent, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            appStr(lang, "home_gemini_title").uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.sp,
                            color = glass.accent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = insightText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        lineHeight = 24.sp
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // -- Quick Actions --
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                QuickActionButton(
                    icon = Icons.Default.AddCircleOutline,
                    label = appStr(lang, "home_new_report"),
                    onClick = onNavigateToReport,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = Icons.Default.Map,
                    label = appStr(lang, "home_view_map"),
                    onClick = onNavigateToMap,
                    modifier = Modifier.weight(1f)
                )
                QuickActionButton(
                    icon = Icons.Default.Info,
                    label = appStr(lang, "home_advisories"),
                    onClick = onNavigateToAdvisories,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(32.dp))

            // -- Recent Reports --
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    appStr(lang, "home_recent"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    appStr(lang, "home_see_all"),
                    style = MaterialTheme.typography.labelMedium,
                    color = glass.accent,
                    modifier = Modifier.clickable { onNavigateToMap() }
                )
            }

            Spacer(Modifier.height(16.dp))

            if (reports.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        appStr(lang, "home_empty"),
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(reports.take(5)) { report ->
                        RecentReportGlassCard(report, lang) {
                            onNavigateToReportDetails(report.id)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentReportGlassCard(report: WaterReport, lang: String, onClick: () -> Unit) {
    val glass = SahyadriTheme.glassColors
    val format = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    val dateStr = format.format(Date(report.timestamp))
    val quality = waterQualityFromReport(report.clarity, report.smell)

    Card(
        modifier = Modifier
            .width(220.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = glass.glassSurfaceStrong),
        border = BorderStroke(1.dp, glass.glassBorder)
    ) {
        Column {
            // Fake Image Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(glass.oceanMedium.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Image,
                    null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f),
                    modifier = Modifier.size(40.dp)
                )
                
                // Status Badge
                val badgeColor = when (quality) {
                    WaterQuality.CLEAN -> CleanBlue
                    WaterQuality.MODERATE -> ModerateAmber
                    WaterQuality.POLLUTED -> PollutedRed
                }
                val labelKey = when (quality) {
                    WaterQuality.CLEAN -> "status_clean"
                    WaterQuality.MODERATE -> "status_moderate"
                    WaterQuality.POLLUTED -> "status_polluted"
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeColor.copy(alpha = 0.9f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        appStr(lang, labelKey).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Column(Modifier.padding(16.dp)) {
                Text(
                    "%.4f, %.4f".format(report.latitude, report.longitude),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = glass.surfaceTint.copy(alpha = 0.8f)
                )
            }
        }
    }
}
