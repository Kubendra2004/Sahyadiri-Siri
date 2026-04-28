package com.example.waterquality

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.waterquality.ui.screens.MainScaffold
import com.example.waterquality.ui.screens.ReportSubmissionScreen
import com.example.waterquality.ui.screens.SplashScreen
import com.example.waterquality.ui.theme.WaterQualityTheme
import com.example.waterquality.ui.utils.LocalAppLanguage
import com.example.waterquality.ui.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Hoist ProfileViewModel to Activity scope so theme + language are
    // available before any composable runs.
    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDark    by profileViewModel.isDarkMode.collectAsStateWithLifecycle()
            val language  by profileViewModel.selectedLanguage.collectAsStateWithLifecycle()

            WaterQualityTheme(darkTheme = isDark) {
                CompositionLocalProvider(LocalAppLanguage provides language) {
                    AppRoot(profileViewModel = profileViewModel)
                }
            }
        }
    }
}

private enum class AppScreen { SPLASH, MAIN, REPORT }

@Composable
private fun AppRoot(profileViewModel: ProfileViewModel) {
    var screen by remember { mutableStateOf(AppScreen.SPLASH) }

    // Splash
    AnimatedVisibility(
        visible = screen == AppScreen.SPLASH,
        enter   = fadeIn(),
        exit    = fadeOut(tween(400)) + scaleOut(targetScale = 0.95f, animationSpec = tween(400)),
        modifier = Modifier.fillMaxSize()
    ) {
        SplashScreen(onNavigateToHome = { screen = AppScreen.MAIN })
    }

    // Main shell
    AnimatedVisibility(
        visible  = screen == AppScreen.MAIN,
        enter    = fadeIn(tween(350)),
        modifier = Modifier.fillMaxSize()
    ) {
        MainScaffold(
            profileViewModel   = profileViewModel,
            onNavigateToReport = { screen = AppScreen.REPORT }
        )
    }

    // Report submission overlay
    AnimatedVisibility(
        visible  = screen == AppScreen.REPORT,
        enter    = fadeIn(tween(200)),
        exit     = fadeOut(tween(200)),
        modifier = Modifier.fillMaxSize()
    ) {
        ReportSubmissionScreen(onNavigateBack = { screen = AppScreen.MAIN })
    }
}
