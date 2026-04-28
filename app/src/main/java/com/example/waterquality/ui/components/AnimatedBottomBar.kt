package com.example.waterquality.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Style
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.waterquality.navigation.BottomNavTab
import com.example.waterquality.ui.utils.LocalAppLanguage
import com.example.waterquality.ui.utils.appStr

private data class NavItem(
    val tab:         BottomNavTab,
    val iconFilled:  ImageVector,
    val iconOutline: ImageVector
)

private val navItems = listOf(
    NavItem(BottomNavTab.HOME,       Icons.Filled.Home,          Icons.Outlined.Home),
    NavItem(BottomNavTab.MAP,        Icons.Filled.Map,           Icons.Outlined.Map),
    NavItem(BottomNavTab.ADVISORIES, Icons.Filled.Style,         Icons.Outlined.Style),
    NavItem(BottomNavTab.ALERTS,     Icons.Filled.Notifications, Icons.Outlined.Notifications),
    NavItem(BottomNavTab.PROFILE,    Icons.Filled.Person,        Icons.Outlined.Person),
)

/**
 * Animated bottom navigation bar.
 *
 * Selected icon bounces with a spring scale effect.
 * Active tab gets a small coloured pill indicator above the icon.
 * Works on API 24+.
 */
@Composable
fun AnimatedBottomBar(
    currentRoute: String,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = LocalAppLanguage.current
    // Localized labels per tab
    val labelFor: (BottomNavTab) -> String = { tab ->
        appStr(lang, when (tab) {
            BottomNavTab.HOME       -> "nav_home"
            BottomNavTab.MAP        -> "nav_map"
            BottomNavTab.ADVISORIES -> "nav_advisories"
            BottomNavTab.ALERTS     -> "nav_alerts"
            BottomNavTab.PROFILE    -> "nav_profile"
        })
    }
    NavigationBar(
        modifier          = modifier,
        containerColor    = MaterialTheme.colorScheme.surface,
        contentColor      = MaterialTheme.colorScheme.onSurface,
        tonalElevation    = 8.dp
    ) {
        navItems.forEach { item ->
            val selected  = currentRoute == item.tab.route
            val iconScale by animateFloatAsState(
                targetValue   = if (selected) 1.18f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness    = Spring.StiffnessMedium
                ),
                label = "icon_scale"
            )
            val iconColor by animateColorAsState(
                targetValue   = if (selected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "icon_color"
            )

            NavigationBarItem(
                selected = selected,
                onClick  = { onTabSelected(item.tab) },
                icon = {
                    Box(contentAlignment = Alignment.Center) {
                        // Pill indicator
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .offset(y = (-18).dp)
                                    .size(width = 28.dp, height = 3.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape
                                    )
                            )
                        }
                        Icon(
                            imageVector = if (selected) item.iconFilled else item.iconOutline,
                            contentDescription = item.tab.label,
                            tint       = iconColor,
                            modifier   = Modifier.scale(iconScale)
                        )
                    }
                },
                label = {
                    Text(
                        text  = labelFor(item.tab),
                        style = MaterialTheme.typography.labelSmall,
                        color = iconColor
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
