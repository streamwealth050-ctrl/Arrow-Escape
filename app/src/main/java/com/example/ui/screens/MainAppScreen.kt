package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.dialogs.LevelSelectDialog
import com.example.ui.dialogs.SettingsDialog
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.GameViewModel

@Composable
fun MainAppScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allProgress by viewModel.allProgress.collectAsStateWithLifecycle()

    // BackHandler for secondary screens
    when (uiState.currentScreen) {
        AppScreen.GAME,
        AppScreen.LEADERBOARD,
        AppScreen.MISSIONS -> {
            BackHandler {
                viewModel.navigateTo(AppScreen.HOME)
            }
        }
        else -> Unit
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAFBFD))
    ) {
        AnimatedContent(
            targetState = uiState.currentScreen,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "screen_transition"
        ) { screen ->
            when (screen) {
                AppScreen.SPLASH -> {
                    SplashScreen()
                }
                AppScreen.AUTH -> {
                    AuthScreen(
                        onGoogleLogin = { viewModel.loginWithGoogle() },
                        onFacebookLogin = { viewModel.loginWithFacebook() },
                        onGuestLogin = { viewModel.loginAsGuest() }
                    )
                }
                AppScreen.HOME -> {
                    HomeScreen(
                        uiState = uiState,
                        onPlayClick = { viewModel.loadLevel(uiState.currentLevel.levelNumber) },
                        onLeaderboardClick = { viewModel.navigateTo(AppScreen.LEADERBOARD) },
                        onLevelSelectClick = { viewModel.openLevelSelect(true) },
                        onMissionsClick = { viewModel.navigateTo(AppScreen.MISSIONS) },
                        onSettingsClick = { viewModel.openSettings(true) },
                        onClaimMission = { missionId -> viewModel.claimMissionReward(missionId) }
                    )
                }
                AppScreen.GAME -> {
                    GameScreen(viewModel = viewModel)
                }
                AppScreen.LEADERBOARD -> {
                    LeaderboardScreen(
                        entries = uiState.leaderboardEntries,
                        onBackClick = { viewModel.navigateTo(AppScreen.HOME) }
                    )
                }
                AppScreen.MISSIONS -> {
                    MissionsScreen(
                        missions = uiState.dailyMissions,
                        onClaimMission = { missionId -> viewModel.claimMissionReward(missionId) },
                        onBackClick = { viewModel.navigateTo(AppScreen.HOME) }
                    )
                }
            }
        }

        // Global Dialogs accessible across screens (e.g. from Home or Game)
        if (uiState.showSettingsDialog) {
            SettingsDialog(
                soundEnabled = uiState.soundEnabled,
                hapticsEnabled = uiState.hapticsEnabled,
                onToggleSound = { viewModel.toggleSound() },
                onToggleHaptics = { viewModel.toggleHaptics() },
                onResetProgress = { viewModel.resetAllProgress() },
                onDismiss = { viewModel.openSettings(false) }
            )
        }

        if (uiState.showLevelSelectDialog) {
            LevelSelectDialog(
                maxUnlockedLevel = uiState.maxUnlockedLevel,
                currentLevelNumber = uiState.currentLevel.levelNumber,
                progressList = allProgress,
                onSelectLevel = { selectedLevel ->
                    viewModel.loadLevel(selectedLevel)
                },
                onDismiss = { viewModel.openLevelSelect(false) }
            )
        }
    }
}
