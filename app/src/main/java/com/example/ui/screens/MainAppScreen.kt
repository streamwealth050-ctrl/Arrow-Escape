package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.dialogs.QuitConfirmDialog
import com.example.ui.dialogs.SettingsDialog
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.GameViewModel

@Composable
fun MainAppScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedHomeTab by remember { mutableStateOf(HomeBottomTab.MAIN) }
    var showQuitDialog by remember { mutableStateOf(false) }
    var showHomeSettingsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(selectedHomeTab, uiState.currentScreen) {
        viewModel.refreshDailyChallengeDate()
    }

    // Back handling matching native behavior
    when {
        uiState.currentScreen == AppScreen.GAME -> {
            BackHandler {
                viewModel.navigateTo(AppScreen.HOME)
            }
        }
        uiState.currentScreen == AppScreen.HOME && selectedHomeTab != HomeBottomTab.MAIN -> {
            BackHandler {
                selectedHomeTab = HomeBottomTab.MAIN
            }
        }
        uiState.currentScreen == AppScreen.HOME && selectedHomeTab == HomeBottomTab.MAIN -> {
            // Ask: "Do you want to quit the game? Yes / No"
            BackHandler {
                showQuitDialog = true
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF030712))
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 560.dp),
            contentAlignment = Alignment.Center
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
                AppScreen.WELCOME -> {
                    WelcomeScreen(
                        onAccept = {
                            viewModel.acceptTerms()
                        }
                    )
                }
                AppScreen.HOME,
                AppScreen.AUTH,
                AppScreen.LEADERBOARD,
                AppScreen.MISSIONS -> {
                    when (selectedHomeTab) {
                        HomeBottomTab.MAIN -> {
                            MainHomeScreen(
                                uiState = uiState,
                                selectedTab = selectedHomeTab,
                                onTabSelected = { selectedHomeTab = it },
                                onNewGameClick = {
                                    viewModel.loadLevel(uiState.campaignLevelNumber)
                                },
                                onDailyPlayClick = {
                                    viewModel.loadDailyChallenge()
                                },
                                onSettingsClick = {
                                    showHomeSettingsDialog = true
                                },
                                onToggleSound = {
                                    viewModel.toggleSound()
                                }
                            )
                        }
                        HomeBottomTab.DAILY -> {
                            DailyScreen(
                                leaderboardEntries = uiState.leaderboardEntries,
                                onPlayDaily = { day ->
                                    viewModel.loadDailyChallenge(day)
                                }
                            )
                        }
                        HomeBottomTab.ME -> {
                            MeScreen(
                                uiState = uiState,
                                onSelectLevel = { selectedLevel ->
                                    viewModel.loadLevel(selectedLevel)
                                },
                                onToggleSound = { viewModel.toggleSound() },
                                onToggleHaptics = { viewModel.toggleHaptics() },
                                onUpdateProfile = { name, avatar ->
                                    viewModel.updatePlayerProfile(name, avatar)
                                }
                            )
                        }
                    }
                }
                AppScreen.GAME -> {
                    GamePlayScreen(
                        uiState = uiState,
                        onArrowTapped = { arrow ->
                            viewModel.onArrowTapped(arrow)
                        },
                        onArrowAnimationFinished = { arrow ->
                            viewModel.onArrowAnimationFinished(arrow)
                        },
                        onHintClick = {
                            viewModel.onHintRequested()
                        },
                        onEraserClick = {
                            viewModel.clearSingleArrowWithEraser()
                        },
                        onBackClick = {
                            viewModel.navigateTo(AppScreen.HOME)
                        },
                        onRestartClick = {
                            viewModel.restartCurrentLevel()
                        },
                        onNextLevelClick = {
                            viewModel.nextLevel()
                        },
                        onToggleSound = {
                            viewModel.toggleSound()
                        },
                        onToggleHaptics = {
                            viewModel.toggleHaptics()
                        },
                        onAddLives = {
                            viewModel.addLives()
                        },
                        onPlayClick = {}
                    )
                }
            }
        }
    }

        // Quit Game Confirmation Dialog (User requested: "ask while the player is exit do you want to quit the game yes no")
        if (showQuitDialog) {
            QuitConfirmDialog(
                onConfirmQuit = {
                    viewModel.playGameExit()
                    showQuitDialog = false
                    (context as? Activity)?.finish()
                },
                onDismiss = {
                    showQuitDialog = false
                }
            )
        }

        // Settings Dialog triggered from Top-Right Gear on Main Home Screen
        if (showHomeSettingsDialog) {
            SettingsDialog(
                soundEnabled = uiState.soundEnabled,
                hapticsEnabled = uiState.hapticsEnabled,
                onToggleSound = { viewModel.toggleSound() },
                onToggleHaptics = { viewModel.toggleHaptics() },
                onResetProgress = { viewModel.resetAllProgress() },
                onDismiss = { showHomeSettingsDialog = false }
            )
        }
    }
}
