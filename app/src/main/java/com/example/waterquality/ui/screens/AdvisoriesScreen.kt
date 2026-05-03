package com.example.waterquality.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.waterquality.ui.components.GlassCard
import com.example.waterquality.ui.theme.SahyadriTheme
import com.example.waterquality.ui.theme.CleanBlue
import com.example.waterquality.ui.theme.ModerateAmber
import com.example.waterquality.ui.theme.PollutedRed
import com.example.waterquality.ui.utils.LocalAppLanguage
import com.example.waterquality.ui.utils.appStr
import com.example.waterquality.ui.viewmodel.AdvisoriesViewModel
import com.example.waterquality.ui.viewmodel.AdvisoryItem
import com.example.waterquality.ui.viewmodel.AlertSeverity
import kotlin.math.absoluteValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvisoriesScreen(
    viewModel: AdvisoriesViewModel = hiltViewModel()
) {
    val lang = LocalAppLanguage.current
    val glass = SahyadriTheme.glassColors
    val advisories by viewModel.advisories.collectAsStateWithLifecycle()

    // Push current language into the ViewModel every time it changes
    LaunchedEffect(lang) { viewModel.setLanguage(lang) }

    val pagerState = rememberPagerState(pageCount = { advisories.size.coerceAtLeast(1) })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(glass.oceanGradient))
    ) {
        // Pager content
        if (advisories.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        } else {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 100.dp, bottom = 80.dp)
            ) { page ->
                val advisory = advisories[page]
                val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                val alphaAnim = 1f - (pageOffset.absoluteValue * 0.5f).coerceIn(0f, 1f)
                val scaleAnim = 1f - (pageOffset.absoluteValue * 0.15f).coerceIn(0f, 1f)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .graphicsLayer {
                            alpha = alphaAnim
                            scaleX = scaleAnim
                            scaleY = scaleAnim
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AdvisoryCard(advisory = advisory, lang = lang, glass = glass)
                }
            }
        }

        // Floating header with notch clearance
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 8.dp, start = 24.dp, end = 24.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.3f))
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TipsAndUpdates, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = appStr(lang, "adv_title"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // Swipe hint at bottom
        if (advisories.size > 1) {
            Text(
                text = appStr(lang, "adv_subtitle"),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 90.dp)
            )
        }
    }
}

@Composable
fun AdvisoryCard(
    advisory: AdvisoryItem,
    lang: String,
    glass: com.example.waterquality.ui.theme.GlassColors
) {
    val (icon, color) = when (advisory.severity) {
        AlertSeverity.INFO     -> Icons.Default.CheckCircle to CleanBlue
        AlertSeverity.WARNING  -> Icons.Default.Warning     to ModerateAmber
        AlertSeverity.CRITICAL -> Icons.Default.Warning     to PollutedRed
    }

    val actionLabel = when (advisory.severity) {
        AlertSeverity.INFO     -> appStr(lang, "adv_status_safe")
        AlertSeverity.WARNING  -> appStr(lang, "adv_status_caution")
        AlertSeverity.CRITICAL -> appStr(lang, "adv_status_critical")
    }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(40.dp))
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = advisory.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = advisory.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { },
                colors = ButtonDefaults.buttonColors(containerColor = color),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(actionLabel, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
