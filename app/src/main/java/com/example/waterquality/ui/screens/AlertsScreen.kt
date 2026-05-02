package com.example.waterquality.ui.screens

import com.example.waterquality.ui.utils.LocalAppLanguage
import com.example.waterquality.ui.utils.appStr
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.waterquality.ui.components.EmptyState
import com.example.waterquality.ui.components.GlassCard
import com.example.waterquality.ui.theme.ModerateAmber
import com.example.waterquality.ui.theme.PollutedRed
import com.example.waterquality.ui.theme.CleanBlue
import com.example.waterquality.ui.theme.SahyadriTheme
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
    val alerts       by viewModel.filteredAlerts.collectAsStateWithLifecycle()
    val activeFilter by viewModel.activeFilter.collectAsStateWithLifecycle()
    val haptic        = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(glass.oceanGradient))
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, null,
                        tint = Color.White, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(appStr(lang, "al_title"),
                        style      = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold, color = Color.White)
                }
                Text(appStr(lang, "al_hint"),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f))
            }
        }

        // Severity filter chips
        LazyRow(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentPadding      = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                SeverityFilterChip(
                    label    = appStr(lang, "al_all"),
                    selected = activeFilter == null,
                    onClick  = { viewModel.setFilter(null) }
                )
            }
            items(AlertSeverity.values().toList()) { severity ->
                SeverityFilterChip(
                    label    = severityLabel(severity, lang),
                    selected = activeFilter == severity,
                    color    = severityColor(severity),
                    onClick  = { viewModel.setFilter(severity) }
                )
            }
        }

        if (alerts.isEmpty()) {
            EmptyState(
                modifier = Modifier.fillMaxSize(),
                icon     = Icons.Default.Notifications,
                title    = appStr(lang, "al_empty"),
                subtitle = appStr(lang, "al_empty_sub")
            )
        } else {
            LazyColumn(
                modifier       = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(alerts, key = { _, a -> a.id }) { index, alert ->
                    // Staggered slide-in
                    var itemVisible by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(index * 80L)
                        itemVisible = true
                    }

                    AnimatedVisibility(
                        visible = itemVisible,
                        enter   = fadeIn(tween(250)) + slideInVertically(
                            initialOffsetY = { it },
                            animationSpec  = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness    = Spring.StiffnessLow
                            )
                        )
                    ) {
                        // Swipe-to-dismiss wrapper
                        var dismissed by remember { mutableStateOf(false) }

                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.StartToEnd ||
                                    value == SwipeToDismissBoxValue.EndToStart) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    dismissed = true
                                    viewModel.dismissAlert(alert.id)
                                    true
                                } else false
                            },
                            positionalThreshold = { totalDistance -> totalDistance * 0.40f }
                        )

                        AnimatedVisibility(
                            visible = !dismissed,
                            exit    = shrinkVertically(spring(stiffness = Spring.StiffnessMedium))
                        ) {
                            SwipeToDismissBox(
                                state            = dismissState,
                                enableDismissFromEndToStart = true,
                                enableDismissFromStartToEnd = false,
                                backgroundContent = {
                                    AlertDismissBackground()
                                },
                                content = {
                                    AlertCard(alert = alert)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertDismissBackground() {
    val lang = LocalAppLanguage.current
    Box(
        modifier          = Modifier
            .fillMaxSize()
            .background(PollutedRed.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .padding(horizontal = 20.dp),
        contentAlignment  = Alignment.CenterEnd
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Delete, appStr(lang, "al_dismiss"), tint = PollutedRed, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(4.dp))
            Text(appStr(lang, "al_dismiss"), color = PollutedRed,
                style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun AlertCard(alert: AlertItem) {
    val lang = LocalAppLanguage.current
    val (_, accentColor, icon) = when (alert.severity) {
        AlertSeverity.CRITICAL -> Triple(
            PollutedRed.copy(alpha = 0.08f), PollutedRed, Icons.Default.Error
        )
        AlertSeverity.WARNING  -> Triple(
            ModerateAmber.copy(alpha = 0.08f), ModerateAmber, Icons.Default.Warning
        )
        AlertSeverity.INFO     -> Triple(
            CleanBlue.copy(alpha = 0.08f), CleanBlue, Icons.Default.Info
        )
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier          = Modifier
                    .size(42.dp)
                    .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment  = Alignment.Center
            ) {
                Icon(icon, null, tint = accentColor, modifier = Modifier.size(22.dp))
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text       = alert.title,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = accentColor,
                        modifier   = Modifier.weight(1f)
                    )
                    SeverityBadge(alert.severity)
                }

                Spacer(Modifier.height(4.dp))
                Text(
                    text  = alert.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(6.dp))
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        alert.locationTag,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        formatTimestamp(alert.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SeverityBadge(severity: AlertSeverity) {
    val lang = LocalAppLanguage.current
    val (color, label) = when (severity) {
        AlertSeverity.CRITICAL -> PollutedRed to appStr(lang, "al_sev_critical")
        AlertSeverity.WARNING  -> ModerateAmber to appStr(lang, "al_sev_warning")
        AlertSeverity.INFO     -> CleanBlue to appStr(lang, "al_sev_info")
    }
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = color, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SeverityFilterChip(
    label:    String,
    selected: Boolean,
    color:    Color = MaterialTheme.colorScheme.primary,
    onClick:  () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick  = onClick,
        label    = { Text(label) },
        shape    = RoundedCornerShape(50),
        colors   = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color.copy(alpha = 0.15f),
            selectedLabelColor     = color
        )
    )
}

private fun severityColor(severity: AlertSeverity) = when (severity) {
    AlertSeverity.CRITICAL -> PollutedRed
    AlertSeverity.WARNING  -> ModerateAmber
    AlertSeverity.INFO     -> CleanBlue
}

private fun severityLabel(severity: AlertSeverity, lang: String): String = when (severity) {
    AlertSeverity.CRITICAL -> appStr(lang, "al_sev_critical")
    AlertSeverity.WARNING  -> appStr(lang, "al_sev_warning")
    AlertSeverity.INFO     -> appStr(lang, "al_sev_info")
}
