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
    val pagerState = rememberPagerState(pageCount = { advisories.size })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(glass.oceanGradient))
    ) {
        if (advisories.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.White)
            }
        } else {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp) // Leave space for bottom nav
            ) { page ->
                val advisory = advisories[page]
                
                // 3D Parallax Effect
                val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                val alphaAnim = 1f - (pageOffset.absoluteValue * 0.5f).coerceIn(0f, 1f)
                val scaleAnim = 1f - (pageOffset.absoluteValue * 0.15f).coerceIn(0f, 1f)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .graphicsLayer {
                            alpha = alphaAnim
                            scaleX = scaleAnim
                            scaleY = scaleAnim
                            translationY = pageOffset * 200f
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AdvisoryCard(advisory = advisory, glass = glass)
                }
            }
        }
        
        // Header overlaid on top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.TipsAndUpdates, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(12.dp))
            Text("AI Advisories", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun AdvisoryCard(advisory: AdvisoryItem, glass: com.example.waterquality.ui.theme.GlassColors) {
    val icon = when (advisory.severity) {
        AlertSeverity.INFO -> Icons.Default.WaterDrop
        AlertSeverity.WARNING -> Icons.Default.Warning
        AlertSeverity.CRITICAL -> Icons.Default.Warning
    }
    
    val color = when (advisory.severity) {
        AlertSeverity.INFO -> CleanBlue
        AlertSeverity.WARNING -> ModerateAmber
        AlertSeverity.CRITICAL -> PollutedRed
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
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
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = advisory.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = advisory.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            // Action Button
            Button(
                onClick = { /* Action */ },
                colors = ButtonDefaults.buttonColors(containerColor = color),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Take Action", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
