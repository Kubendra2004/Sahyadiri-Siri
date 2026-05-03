package com.example.waterquality.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.shape.CircleShape
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
    ) {
        // Massive Hero Header Redesign
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(glass.oceanGradient))
                .statusBarsPadding()
                .padding(bottom = 32.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
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
                        onClick = { /* Settings */ },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(Icons.Default.Settings, null, tint = Color.White)
                    }
                }
                
                Spacer(Modifier.height(32.dp))
                
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Profile Logo",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(8.dp),
                    contentScale = ContentScale.Fit
                )
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    text = stats.username,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Joined ${stats.joinedDays} days ago",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // Modern Dashboard Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Stats Row
            item {
                StatCard(icon = Icons.Default.WaterDrop, value = "${stats.totalSubmitted}", label = "Reports", glass)
            }
            item {
                StatCard(icon = Icons.Default.EmojiEvents, value = "${stats.badgeCount}", label = "Badges", glass)
            }

            // Theme Preferences (Full Width)
            item(span = { GridItemSpan(2) }) {
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(glass.accent.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Palette, null, tint = glass.accent)
                        }
                        Spacer(Modifier.width(16.dp))
                        Text(
                            "Theme Preference",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Fixed horizontal wrapping using SingleChoiceSegmentedButtonRow
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        themeOptions.forEachIndexed { index, (value, label) ->
                            SegmentedButton(
                                selected = themeMode == value,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    viewModel.setThemeMode(value)
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = themeOptions.size)
                            ) {
                                Text(label, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            // Language & Notifications (Full Width)
            item(span = { GridItemSpan(2) }) {
                GlassCard {
                    Text(
                        "App Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(16.dp))
                    
                    // Language
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(16.dp))
                            Text(appStr(lang, "pro_lang"), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
                        }
                        
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            FilterChip(
                                selected = true,
                                onClick = { expanded = true },
                                label = { Text(language) },
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }
                            )
                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                languageOptions.forEach { (langKey, label) ->
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            viewModel.setLanguage(langKey)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    HorizontalDivider(Modifier.padding(vertical = 12.dp))
                    
                    // Notifications
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(16.dp))
                            Text(appStr(lang, "pro_notif"), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
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
private fun StatCard(icon: ImageVector, value: String, label: String, glass: com.example.waterquality.ui.theme.GlassColors) {
    GlassCard(contentPadding = PaddingValues(16.dp)) {
        Icon(icon, null, tint = glass.accent, modifier = Modifier.size(28.dp))
        Spacer(Modifier.height(12.dp))
        Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onBackground)
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
