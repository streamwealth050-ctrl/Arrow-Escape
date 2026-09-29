package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.GameBoard
import com.example.ui.components.GameControls
import com.example.ui.components.GameHeader
import com.example.ui.dialogs.GameOverDialog
import com.example.ui.dialogs.LevelCompletedDialog
import com.example.ui.dialogs.LevelSelectDialog
import com.example.ui.dialogs.SettingsDialog
import com.example.ui.viewmodel.GameViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allProgress by viewModel.allProgress.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAFBFD))
            .testTag("game_screen")
    ) { innerPadding ->
        val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topPadding, bottom = bottomPadding)
                .background(Color(0xFFFAFBFD)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp), // Responsive tablet constraint
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Top Header: Title, Hearts, Remaining count, Timer
                GameHeader(
                    uiState = uiState,
                    onBackOrSelectClick = { viewModel.openLevelSelect(true) },
                    onSettingsClick = { viewModel.openSettings(true) }
                )

                // 2. Center GameBoard with Canvas
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    GameBoard(
                        uiState = uiState,
                        onArrowTapped = { arrow ->
                            viewModel.onArrowTapped(arrow)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                // 3. Bottom Controls: Hint, Restart, Level Select
                GameControls(
                    onHintClick = { viewModel.onHintRequested() },
                    onRestartClick = { viewModel.restartCurrentLevel() },
                    onLevelSelectClick = { viewModel.openLevelSelect(true) },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Dialogs
            if (uiState.isLevelComplete) {
                val timeTaken = uiState.totalTimeSeconds - uiState.timeRemainingSeconds
                LevelCompletedDialog(
                    levelNumber = uiState.currentLevel.levelNumber,
                    stars = uiState.starsEarned,
                    timeTakenSeconds = timeTaken,
                    movesCount = uiState.movesCount,
                    onNextLevel = { viewModel.nextLevel() },
                    onReplay = { viewModel.restartCurrentLevel() },
                    onDismiss = { /* Non-dismissible without choosing action */ }
                )
            }

            if (uiState.isGameOver) {
                GameOverDialog(
                    reason = uiState.gameOverReason,
                    onRetry = { viewModel.restartCurrentLevel() },
                    onDismiss = { viewModel.restartCurrentLevel() }
                )
            }

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
}
