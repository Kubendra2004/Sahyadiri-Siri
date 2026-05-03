package com.example.waterquality.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.waterquality.ui.components.GlassCard
import com.example.waterquality.ui.theme.SahyadriTheme
import com.example.waterquality.ui.theme.CleanBlue
import com.example.waterquality.ui.theme.ModerateAmber
import com.example.waterquality.ui.theme.PollutedRed
import com.example.waterquality.ui.utils.LocalAppLanguage
import com.example.waterquality.ui.utils.appStr
import com.example.waterquality.ui.viewmodel.AlertItem
import com.example.waterquality.ui.viewmodel.AlertSeverity
import com.example.waterquality.ui.viewmodel.AlertsViewModel
import com.example.waterquality.ui.utils.formatTimestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    viewModel: AlertsViewModel = hiltViewModel()
) {
    val lang = LocalAppLanguage.current
    val glass = SahyadriTheme.glassColors
    val alerts by viewModel.filteredAlerts.collectAsStateWithLifecycle()
    val activeFilter by viewModel.activeFilter.collectAsStateWithLifecycle()

    // Push current language into the ViewModel so seed/dynamic content re-localizes
    LaunchedEffect(lang) { viewModel.setLanguage(lang) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Redesigned Header with notch clearance
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(glass.glassSurfaceStrong)
                .statusBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Column {
                Text(
                    appStr(lang, "nav_alerts"),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    appStr(lang, "al_hint"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Timeline Feed
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
        ) {
            itemsIndexed(alerts) { index, alert ->
                TimelineAlertItem(
                    alert = alert,
                    isLast = index == alerts.size - 1,
                    glass = glass
                )
            }
        }
    }
}

@Composable
fun TimelineAlertItem(alert: AlertItem, isLast: Boolean, glass: com.example.waterquality.ui.theme.GlassColors) {
    val color = when (alert.severity) {
        AlertSeverity.CRITICAL -> PollutedRed
        AlertSeverity.WARNING -> ModerateAmber
        AlertSeverity.INFO -> CleanBlue
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(IntrinsicSize.Min)
    ) {
        // Timeline Column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxHeight()
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(color)
                    .padding(4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color.White))
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(color.copy(alpha = 0.3f))
                )
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))

        // Content Card
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = formatTimestamp(alert.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = alert.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.WaterDrop, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(alert.locationTag, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
