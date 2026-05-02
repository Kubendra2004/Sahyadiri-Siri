package com.example.waterquality.ui.screens

import com.example.waterquality.ui.utils.LocalAppLanguage
import com.example.waterquality.ui.utils.advisoryStatusLabel
import com.example.waterquality.ui.utils.appStr
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.waterquality.data.model.Advisory
import com.example.waterquality.ui.components.EmptyState
import com.example.waterquality.ui.components.GlassCard
import com.example.waterquality.ui.components.pagerParallaxOffset
import com.example.waterquality.ui.theme.CleanBlue
import com.example.waterquality.ui.theme.ModerateAmber
import com.example.waterquality.ui.theme.PollutedRed
import com.example.waterquality.ui.theme.SahyadriTheme
import com.example.waterquality.ui.viewmodel.WaterViewModel
import com.example.waterquality.ui.utils.formatTimestamp
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AdvisoriesScreen(
    viewModel: WaterViewModel = hiltViewModel()
) {
    val lang = LocalAppLanguage.current
    val glass = SahyadriTheme.glassColors
    val advisories by viewModel.advisories.collectAsStateWithLifecycle()
    val haptic     = LocalHapticFeedback.current

    val pagerState = rememberPagerState(pageCount = { advisories.size })

    // Haptic on page change
    val currentPage = pagerState.currentPage
    androidx.compose.runtime.LaunchedEffect(currentPage) {
        if (currentPage > 0) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    Column(
        modifier            = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(glass.oceanGradient))
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column {
                Text(appStr(lang, "adv_title"),
                    style      = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold, color = Color.White)
                Text(appStr(lang, "adv_subtitle"),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f))
            }
        }

        Spacer(Modifier.height(24.dp))

        if (advisories.isEmpty()) {
            EmptyState(
                modifier = Modifier.fillMaxSize(),
                title    = appStr(lang, "adv_empty"),
                subtitle = appStr(lang, "adv_empty_sub")
            )
        } else {
            // Pager with scale + parallax effect
            HorizontalPager(
                state           = pagerState,
                contentPadding  = PaddingValues(horizontal = 36.dp),
                pageSpacing     = 16.dp,
                modifier        = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                val pageOffset = pagerState.currentPageOffsetFraction +
                        (page - pagerState.currentPage)

                // Scale: center card is 1f, neighbours are smaller
                val scaleAnim by animateFloatAsState(
                    targetValue   = 1f - (pageOffset.absoluteValue * 0.08f).coerceIn(0f, 0.1f),
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness    = Spring.StiffnessMedium
                    ),
                    label = "card_scale"
                )
                // Slight elevation for active card
                val elevationAnim by animateFloatAsState(
                    targetValue   = if (pageOffset.absoluteValue < 0.5f) 16f else 4f,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label         = "card_elevation"
                )

                // Parallax offset on inner content
                val parallax = pagerParallaxOffset(pagerState.currentPageOffsetFraction,
                    page - pagerState.currentPage, depth = 32f)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scaleAnim
                            scaleY = scaleAnim
                            shadowElevation = elevationAnim
                            shape = androidx.compose.ui.graphics.RectangleShape
                            clip  = false
                        }
                ) {
                    AdvisoryFlashCard(
                        advisory      = advisories[page],
                        parallaxOffset = parallax,
                        lang = lang
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Animated pill pager indicator
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(advisories.size) { index ->
                    val isActive  = pagerState.currentPage == index
                    val widthAnim by animateFloatAsState(
                        targetValue   = if (isActive) 24f else 8f,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium),
                        label         = "dot_width"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(8.dp)
                            .width(widthAnim.dp)
                            .clip(CircleShape)
                            .background(
                                if (isActive) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                            )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AdvisoryFlashCard(
    advisory:      Advisory,
    parallaxOffset: Float = 0f,
    lang: String
) {
    val (bgColors, iconTint, icon) = when (advisory.status) {
        "Critical" -> Triple(
            listOf(Color(0xFF1A0010), Color(0xFF2D0018)),
            PollutedRed,
            Icons.Default.Warning
        )
        "Caution"  -> Triple(
            listOf(Color(0xFF1A1200), Color(0xFF2D2000)),
            ModerateAmber,
            Icons.Default.Shield
        )
        else       -> Triple(
            listOf(Color(0xFF001A2D), Color(0xFF002540)),
            CleanBlue,
            Icons.Default.Info
        )
    }
    val statusLabel = advisoryStatusLabel(lang, advisory.status)

    GlassCard(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(bgColors))
        ) {
            // Parallax background orb
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.TopEnd)
                    .graphicsLayer { translationX = parallaxOffset; translationY = -parallaxOffset * 0.5f }
                    .background(
                        Brush.radialGradient(
                            listOf(iconTint.copy(alpha = 0.15f), Color.Transparent)
                        ),
                        CircleShape
                    )
            )

            Column(
                modifier            = Modifier
                    .fillMaxSize()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Status badge
                Box(
                    modifier = Modifier
                        .background(iconTint.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.material3.Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier    = Modifier.size(14.dp),
                            tint        = iconTint
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text  = statusLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = iconTint,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Main icon with parallax
                androidx.compose.material3.Icon(
                    imageVector        = icon,
                    contentDescription = null,
                    modifier           = Modifier
                        .size(72.dp)
                        .graphicsLayer { translationY = parallaxOffset * 0.6f },
                    tint               = iconTint
                )

                // Content
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text       = advisory.title,
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign  = TextAlign.Center,
                        color      = Color.White
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text      = advisory.description,
                        style     = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color     = Color.White.copy(alpha = 0.8f),
                        lineHeight = 24.sp
                    )
                }

                // Timestamp
                Text(
                    text  = formatTimestamp(advisory.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.45f)
                )
            }
        }
    }
}
