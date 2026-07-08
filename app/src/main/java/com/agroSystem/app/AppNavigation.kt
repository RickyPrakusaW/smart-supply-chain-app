package com.agroSystem.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.agroSystem.app.ui.screens.HomeScreen
import com.agroSystem.app.ui.screens.OnboardingScreen

sealed interface Screen {
    object Onboarding : Screen
    object Home : Screen
}

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Onboarding) }

    when (currentScreen) {
        is Screen.Onboarding -> {
            OnboardingScreen(
                onFinished = {
                    currentScreen = Screen.Home
                }
            )
        }
        is Screen.Home -> {
            HomeScreen(
                onResetOnboarding = {
                    currentScreen = Screen.Onboarding
                }
            )
        }
    }
}
