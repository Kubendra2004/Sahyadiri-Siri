package com.example.waterquality.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.waterquality.navigation.BottomNavTab
import com.example.waterquality.navigation.Routes
import com.example.waterquality.ui.components.AnimatedBottomBar
import com.example.waterquality.ui.viewmodel.ProfileViewModel
import com.example.waterquality.ui.viewmodel.WaterViewModel
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun MainScaffold(
    profileViewModel: ProfileViewModel,
    onNavigateToReport: () -> Unit = {}
) {
    val navController  = rememberNavController()
    val navBackStack   by navController.currentBackStackEntryAsState()
    val currentRoute   = navBackStack?.destination?.route ?: Routes.HOME
    val bottomBarVisible = BottomNavTab.values().any { it.route == currentRoute }
    val waterViewModel: WaterViewModel = hiltViewModel()
    val alertBadge by waterViewModel.alertBadgeCount.collectAsStateWithLifecycle()
    val backendBaseUrl by waterViewModel.backendBaseUrl.collectAsStateWithLifecycle()
    val isBackendReachable by waterViewModel.isBackendReachable.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            AnimatedVisibility(
                visible = bottomBarVisible,
                enter   = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec  = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness    = Spring.StiffnessMedium
                    )
                ),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                AnimatedBottomBar(
                    currentRoute    = currentRoute,
                    alertBadgeCount = alertBadge,
                    onTabSelected   = { tab ->
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            BackendStatusBanner(
                baseUrl = backendBaseUrl,
                isConnected = isBackendReachable
            )

            NavHost(
                navController    = navController,
                startDestination = Routes.HOME,
                modifier         = Modifier.fillMaxSize()
            ) {
                composable(Routes.HOME) {
                    HomeScreen(
                        onNavigateToReportDetails = { id -> navController.navigate(Routes.reportDetail(id)) },
                        onNavigateToReport  = onNavigateToReport,
                        onNavigateToMap        = { navController.navigate(Routes.MAP) { launchSingleTop = true } },
                        onNavigateToAdvisories = { navController.navigate(Routes.ADVISORIES) { launchSingleTop = true } }
                    )
                }
                composable(Routes.MAP)         { MapScreen() }
                composable(Routes.ADVISORIES)  { AdvisoriesScreen() }
                composable(Routes.ALERTS)      { AlertsScreen() }
                composable(Routes.PROFILE)     { ProfileScreen(viewModel = profileViewModel) }
                composable(Routes.REPORT_DETAIL) { back ->
                    val reportId = back.arguments?.getString("reportId") ?: ""
                    ReportDetailsScreen(
                        reportId       = reportId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
private fun BackendStatusBanner(baseUrl: String, isConnected: Boolean) {
    val background = if (isConnected) Color(0xFFDFF6E6) else Color(0xFFFFE3E3)
    val textColor = if (isConnected) Color(0xFF0F6A33) else Color(0xFF8A1F1F)
    val label = if (isConnected) "Backend connected" else "Backend unreachable"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label: $baseUrl",
            style = MaterialTheme.typography.labelMedium,
            color = textColor
        )
    }
}
