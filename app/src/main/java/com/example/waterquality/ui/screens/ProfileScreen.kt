package com.example.waterquality.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.waterquality.R
import com.example.waterquality.ui.components.GlassCard
import com.example.waterquality.ui.theme.SahyadriTheme
import com.example.waterquality.ui.utils.LocalAppLanguage
import com.example.waterquality.ui.utils.appStr
import com.example.waterquality.ui.viewmodel.ProfileViewModel
import com.example.waterquality.ui.viewmodel.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: ProfileViewModel) {
    val lang = LocalAppLanguage.current
    val glass = SahyadriTheme.glassColors
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val notifEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val language by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val stats by viewModel.profileStats.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    // Theme options with localized labels
    val themeOptions = listOf(
        ThemeMode.LIGHT  to appStr(lang, "pro_light"),
        ThemeMode.DARK   to appStr(lang, "pro_dark"),
        ThemeMode.SYSTEM to (if (lang == "ಕನ್ನಡ") "ಸಿಸ್ಟಮ್" else "System")
    )

    // Language options — key + display label
    val languageOptions = listOf(
        "English" to appStr(lang, "lang_english"),
        "ಕನ್ನಡ"   to appStr(lang, "lang_kannada")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Hero Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(glass.oceanGradient))
                .statusBarsPadding()
                .padding(bottom = 32.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        appStr(lang, "nav_profile"),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    IconButton(
                        onClick = { },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(Icons.Default.Settings, null, tint = Color.White)
                    }
                }

                Spacer(Modifier.height(24.dp))

                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Profile Logo",
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(8.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = stats.username,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${appStr(lang, "pro_member_for")} ${stats.joinedDays} ${appStr(lang, "pro_days")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // Dashboard Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Stats
            item {
                StatCard(
                    icon = Icons.Default.WaterDrop,
                    value = "${stats.totalSubmitted}",
                    label = appStr(lang, "pro_reports"),
                    glass = glass
                )
            }
            item {
                StatCard(
                    icon = Icons.Default.EmojiEvents,
                    value = "${stats.badgeCount}",
                    label = appStr(lang, "pro_badges"),
                    glass = glass
                )
            }

            // Theme Section (full width)
            item(span = { GridItemSpan(2) }) {
                GlassCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(glass.accent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Palette, null, tint = glass.accent)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            if (lang == "ಕನ್ನಡ") "ಥೀಮ್ ಆಯ್ಕೆ" else "Theme",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    // Segmented button row — fills full width, text uses labelSmall to fit
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        themeOptions.forEachIndexed { index, (value, label) ->
                            SegmentedButton(
                                selected = themeMode == value,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.setThemeMode(value)
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = themeOptions.size),
                                icon = { }   // suppress checkmark icon to save space
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }
            }

            // Language Section (full width) — button-style, not dropdown
            item(span = { GridItemSpan(2) }) {
                GlassCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(glass.accent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Language, null, tint = glass.accent)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            appStr(lang, "pro_language"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    // Two-button row for language selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        languageOptions.forEach { (langKey, label) ->
                            val isSelected = language == langKey
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.setLanguage(langKey)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) glass.accent else MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = if (isSelected) 4.dp else 0.dp
                                )
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // Notifications (full width)
            item(span = { GridItemSpan(2) }) {
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, null, tint = glass.accent)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                appStr(lang, "pro_notif"),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Switch(
                            checked = notifEnabled,
                            onCheckedChange = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.toggleNotifications()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    value: String,
    label: String,
    glass: com.example.waterquality.ui.theme.GlassColors
) {
    GlassCard(contentPadding = PaddingValues(16.dp)) {
        Icon(icon, null, tint = glass.accent, modifier = Modifier.size(28.dp))
        Spacer(Modifier.height(10.dp))
        Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground)
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
