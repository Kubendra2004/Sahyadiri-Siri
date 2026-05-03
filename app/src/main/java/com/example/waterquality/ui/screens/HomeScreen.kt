package com.example.waterquality.ui.screens

import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.waterquality.R
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToMap: () -> Unit,
    onNavigateToReportDetails: (String) -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToAdvisories: () -> Unit,
    viewModel: WaterViewModel = hiltViewModel()
) {
    val lang = LocalAppLanguage.current
    val glass = SahyadriTheme.glassColors
    val reports by viewModel.reports.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Redesigned Futuristic Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                .background(Brush.linearGradient(glass.oceanGradient))
                .statusBarsPadding()
                .padding(bottom = 32.dp, top = 16.dp, start = 24.dp, end = 24.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(4.dp)
                    )
                    IconButton(onClick = { /* Handle Search/Action */ }, colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(0.2f))) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "Current Status",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "82",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 72.sp
                    )
                    Text(
                        text = "WQI",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = glass.accent,
                        modifier = Modifier.padding(bottom = 12.dp, start = 8.dp)
                    )
                }
                Text(
                    text = "Water quality is generally safe across major points.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }

        // Futuristic Scrollable Dashboard Grid
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    QuickActionCard(
                        icon = Icons.Default.Map,
                        title = "Live Map",
                        subtitle = "View Points",
                        glass = glass,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToMap
                    )
                    QuickActionCard(
                        icon = Icons.Default.AddLocation,
                        title = "Report",
                        subtitle = "New Finding",
                        glass = glass,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToReport
                    )
                }
            }
            
            item {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            items(reports.take(5)) { report ->
                RedesignedReportCard(report = report, lang = lang, glass = glass, onClick = { onNavigateToReportDetails(report.id) })
            }
        }
    }
}

@Composable
fun QuickActionCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, glass: GlassColors, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(glass.glassSurfaceStrong, glass.glassSurface)
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(glass.accent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = glass.accent)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun RedesignedReportCard(report: WaterReport, lang: String, glass: GlassColors, onClick: () -> Unit) {
    val quality = waterQualityFromReport(report.clarity, report.smell)
    val cardColor = when (quality) {
        WaterQuality.CLEAN -> CleanBlue
        WaterQuality.MODERATE -> ModerateAmber
        WaterQuality.POLLUTED -> PollutedRed
    }
    
    val wqiScore = report.clarity * 20
    val locationName = "Lat: %.4f, Lng: %.4f".format(report.latitude, report.longitude)
    
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(cardColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$wqiScore",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = cardColor
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = locationName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(report.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
