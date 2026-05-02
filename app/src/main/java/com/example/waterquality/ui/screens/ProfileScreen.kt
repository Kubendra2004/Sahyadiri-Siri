package com.example.waterquality.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush

import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.waterquality.ui.components.GlassCard
import com.example.waterquality.ui.theme.CleanBlue
import com.example.waterquality.ui.theme.ModerateAmber
import com.example.waterquality.ui.theme.PollutedRed
import com.example.waterquality.ui.theme.SahyadriTheme
import com.example.waterquality.ui.utils.LocalAppLanguage
import com.example.waterquality.ui.utils.appStr
import com.example.waterquality.ui.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(viewModel: ProfileViewModel) {
    val lang         = LocalAppLanguage.current
    val glass        = SahyadriTheme.glassColors
    val themeMode    by viewModel.themeMode.collectAsStateWithLifecycle()
    val notifEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val language     by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val stats        by viewModel.profileStats.collectAsStateWithLifecycle()
    val haptic       = LocalHapticFeedback.current

    val themeOptions = listOf(
        com.example.waterquality.ui.viewmodel.ThemeMode.LIGHT to appStr(lang, "pro_light"),
        com.example.waterquality.ui.viewmodel.ThemeMode.DARK to appStr(lang, "pro_dark"),
        com.example.waterquality.ui.viewmodel.ThemeMode.SYSTEM to "System"
    )

    val languageOptions = listOf(
        "English" to appStr(lang, "lang_english"),
        "ಕನ್ನಡ" to appStr(lang, "lang_kannada")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // â”€â”€ Gradient Header â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(glass.oceanGradient))
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 28.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.waterquality.R.drawable.logo),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(stats.username,
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White)
                Spacer(Modifier.height(4.dp))
                Text(appStr(lang, "pro_member_for") + " ${stats.joinedDays} " + appStr(lang, "pro_days"),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(0.7f))
            }
        }

        // â”€â”€ Stats Row â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProfileStatCard(Modifier.weight(1f),
                value = "${stats.totalSubmitted}",
                label = appStr(lang, "pro_reports"),
                icon  = Icons.Default.Assignment,
                color = CleanBlue)
            ProfileStatCard(Modifier.weight(1f),
                value = "${stats.streak}",
                label = appStr(lang, "pro_streak"),
                icon  = Icons.Default.LocalFireDepartment,
                color = ModerateAmber)
            ProfileStatCard(Modifier.weight(1f),
                value = "${stats.badgeCount}",
                label = appStr(lang, "pro_badges"),
                icon  = Icons.Default.Star,
                color = PollutedRed)
        }

        Spacer(Modifier.height(4.dp))

        // â”€â”€ Preferences â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        SectionCard(title = appStr(lang, "pro_preferences")) {
            // Theme Mode chips
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.DarkMode, null,
                    tint     = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(14.dp))
                Text("Theme",
                    style    = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    themeOptions.forEach { (value, label) ->
                        val selected = themeMode == value
                        val bgColor by animateColorAsState(
                            targetValue   = if (selected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant,
                            animationSpec = spring(),
                            label         = "theme_color"
                        )
                        val textColor by animateColorAsState(
                            targetValue   = if (selected) Color.White
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            animationSpec = spring(),
                            label         = "theme_text"
                        )
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.setThemeMode(value)
                            },
                            colors         = ButtonDefaults.buttonColors(containerColor = bgColor),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            shape          = RoundedCornerShape(10.dp),
                            elevation      = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Text(label, color = textColor,
                                style      = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 4.dp))

            // Notifications
            SettingsSwitchRow(
                icon    = Icons.Default.Notifications,
                label   = appStr(lang, "pro_notif"),
                checked = notifEnabled,
                onToggle = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.toggleNotifications()
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 4.dp))

            // Language chips
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Translate, null,
                    tint     = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(14.dp))
                Text(appStr(lang, "pro_language"),
                    style    = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    languageOptions.forEach { (value, label) ->
                        val selected = language == value
                        val bgColor by animateColorAsState(
                            targetValue   = if (selected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.surfaceVariant,
                            animationSpec = spring(),
                            label         = "lang_color"
                        )
                        val textColor by animateColorAsState(
                            targetValue   = if (selected) Color.White
                                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            animationSpec = spring(),
                            label         = "lang_text"
                        )
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                viewModel.setLanguage(value)
                            },
                            colors         = ButtonDefaults.buttonColors(containerColor = bgColor),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            shape          = RoundedCornerShape(10.dp),
                            elevation      = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Text(label, color = textColor,
                                style      = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }

            }
        }

        Spacer(Modifier.height(12.dp))

        // â”€â”€ About â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        SectionCard(title = appStr(lang, "pro_about")) {
            AboutRow(Icons.Default.Info,        appStr(lang, "pro_version"),  appStr(lang, "pro_version_value"))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 4.dp))
            AboutRow(Icons.Default.Cloud,       appStr(lang, "pro_data"),     appStr(lang, "pro_data_value"))
            HorizontalDivider(modifier = Modifier.padding(horizontal = 4.dp))
            AboutRow(Icons.Default.AutoAwesome, appStr(lang, "pro_ai"),       appStr(lang, "pro_ai_value"))
        }

        Spacer(Modifier.height(32.dp))
    }
}

// â”€â”€ Reusable composables â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
@Composable
private fun ProfileStatCard(
    modifier: Modifier,
    value: String,
    label: String,
    icon: ImageVector,
    color: Color
) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(0.1f)),
        elevation = CardDefaults.cardElevation(0.dp)) {
        Column(modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp))
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(0.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector, label: String,
    checked: Boolean, onToggle: () -> Unit
) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f))
        Switch(
            checked = checked, onCheckedChange = { onToggle() },
            colors  = SwitchDefaults.colors(
                checkedThumbColor  = Color.White,
                checkedTrackColor  = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun AboutRow(icon: ImageVector, label: String, value: String) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
