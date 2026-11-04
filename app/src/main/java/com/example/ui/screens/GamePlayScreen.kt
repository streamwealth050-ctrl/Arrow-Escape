package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.Arrow
import com.example.ui.components.ArrowPuzzleBoard
import com.example.ui.viewmodel.GameUiState
import com.example.util.DateHelper
import kotlinx.coroutines.delay

@Composable
fun GamePlayScreen(
    uiState: GameUiState,
    onArrowTapped: (Arrow) -> Unit,
    onArrowAnimationFinished: (Arrow) -> Unit,
    onHintClick: () -> Unit,
    onEraserClick: () -> Unit,
    onBackClick: () -> Unit,
    onRestartClick: () -> Unit,
    onNextLevelClick: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleHaptics: () -> Unit,
    onAddLives: () -> Unit,
    onPlayClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showOptionsDialog by remember { mutableStateOf(false) }

    // "Perfect!" celebration badge matching video frames 23-24 at level victory
    var showPerfectBadge by remember { mutableStateOf(false) }
    var showVictoryDialog by remember { mutableStateOf(false) }
    var emojiFace by remember { mutableStateOf("😊") }
    val perfectScale = remember { Animatable(0f) }

    // Blocked red edge vignette flash matching video frame 4
    val blockedFlash = remember { Animatable(0f) }
    LaunchedEffect(uiState.blockedArrowIds) {
        if (uiState.blockedArrowIds.isNotEmpty()) {
            blockedFlash.snapTo(1f)
            blockedFlash.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 280, easing = LinearEasing)
            )
        }
    }

    LaunchedEffect(uiState.isLevelComplete) {
        if (uiState.isLevelComplete) {
            showPerfectBadge = true
            emojiFace = "😊"
            perfectScale.snapTo(0.2f)
            perfectScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            delay(400L)
            emojiFace = "😌"
            delay(1400L)
            showPerfectBadge = false
            showVictoryDialog = true
        } else {
            showPerfectBadge = false
            showVictoryDialog = false
        }
    }

    // 50% milestone notification banner matching video 00:53
    var showHalfwayToast by remember { mutableStateOf(false) }
    var halfwayShownForLevel by remember { mutableStateOf(-1) }

    val total = uiState.totalArrowsCount
    val remaining = uiState.activeArrows.size
    val currentLvl = uiState.currentLevel.levelNumber

    LaunchedEffect(currentLvl) {
        halfwayShownForLevel = -1
        showHalfwayToast = false
        if (!uiState.isLevelComplete) {
            showPerfectBadge = false
            showVictoryDialog = false
        }
    }

    LaunchedEffect(remaining, total) {
        if (total >= 10 && remaining <= total / 2 && halfwayShownForLevel != currentLvl && remaining > 0) {
            halfwayShownForLevel = currentLvl
            showHalfwayToast = true
            delay(2200L)
            showHalfwayToast = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag("game_play_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 560.dp)
        ) {
            // 1. Top Bar: Back, Title, Gear (matching video 00:11)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("game_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(24.dp)
                    )
                }

                val currentMonth = DateHelper.getCurrentMonthName()
                Text(
                    text = if (uiState.isDailyChallenge) "Daily Challenge • $currentMonth ${uiState.dailyChallengeDay}" else "Level ${uiState.campaignLevelNumber}",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    fontSize = 18.sp
                )

                IconButton(
                    onClick = {
                        onPlayClick()
                        showOptionsDialog = true
                    },
                    modifier = Modifier.testTag("game_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Options",
                        tint = Color(0xFF0F172A),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // 2. Status Pill: Remaining Arrows, 3 Hearts, Difficulty in single floating card (matching video)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color.White,
                    shadowElevation = 3.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9)),
                    modifier = Modifier.testTag("game_status_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Remaining arrows badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.testTag("arrows_remaining_pill")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NearMe,
                                    contentDescription = null,
                                    tint = Color(0xFF334155),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "${uiState.activeArrows.size}",
                                    color = Color(0xFF1E293B),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        // 3 Hearts (Lives) matching video
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.testTag("hearts_row")
                        ) {
                            repeat(uiState.maxLives) { index ->
                                val hasLife = index < uiState.remainingLives
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Life",
                                    tint = if (hasLife) Color(0xFFEF4444) else Color(0xFFCBD5E1),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Difficulty pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.testTag("difficulty_pill")
                        ) {
                            Text(
                                text = uiState.currentLevel.difficulty.label,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // 3. Center Board Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                ArrowPuzzleBoard(
                    uiState = uiState,
                    onArrowTapped = onArrowTapped,
                    onArrowAnimationFinished = onArrowAnimationFinished,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 4. Bottom Controls: Hint & Eraser with cleanly aligned badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp, top = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hint Button (Lightbulb)
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clickable { onHintClick() }
                        .testTag("hint_action_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF8FAFC),
                        shadowElevation = 2.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = "Hint",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    // Uniform, clean badge pinned to TopEnd
                    Surface(
                        shape = RoundedCornerShape(9.dp),
                        color = Color(0xFF2563EB),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 1.dp, end = 1.dp)
                    ) {
                        Text(
                            text = if (uiState.availableHints > 0) "${uiState.availableHints}" else "Ad",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Eraser / Clear 1 Arrow Button
                Box(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clickable { onEraserClick() }
                        .testTag("eraser_action_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF8FAFC),
                        shadowElevation = 2.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AutoFixHigh,
                                contentDescription = "Eraser",
                                tint = Color(0xFF8B5CF6),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Uniform, clean badge pinned to TopEnd
                    Surface(
                        shape = RoundedCornerShape(9.dp),
                        color = Color(0xFF2563EB),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 1.dp, end = 1.dp)
                    ) {
                        Text(
                            text = "Ad",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // 5. "50% Completed" Celebration Toast (matching video 00:53)
        AnimatedVisibility(
            visible = showHalfwayToast,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 50.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.White,
                shadowElevation = 6.dp,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF38BDF8))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🥳", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "50% Completed",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7),
                        fontSize = 14.sp
                    )
                }
            }
        }



        // 6. Out of Lives Dialog matching video 01:00
        if (uiState.isGameOver) {
            Dialog(onDismissRequest = {}) {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .testTag("out_of_lives_dialog")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "Out of Lives!",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            fontSize = 22.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Heart with +1 badge
                        Box(contentAlignment = Alignment.TopEnd) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFFEE2E2),
                                modifier = Modifier.size(76.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(44.dp)
                                    )
                                }
                            }

                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF2563EB),
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "+1",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // Get +1 Life Button
                        Button(
                            onClick = onAddLives,
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2563EB)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("get_more_lives_button")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Get +1 Life",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "▶", color = Color.White, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Restart Game Button
                        TextButton(
                            onClick = onRestartClick,
                            modifier = Modifier.testTag("restart_game_button")
                        ) {
                            Text(
                                text = "Restart Game",
                                color = Color(0xFF2563EB),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // 7. Center "Perfect!" Emoji Badge (matching video 01:14 frames 23-24)
        if (showPerfectBadge) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("perfect_celebration_badge"),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color.White,
                    shadowElevation = 8.dp,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .graphicsLayer(
                            scaleX = perfectScale.value,
                            scaleY = perfectScale.value
                        )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = emojiFace,
                            fontSize = 36.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Perfect!",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2563EB),
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }

        // Red Edge Vignette Flash on obstacle collision (matching video frame 4)
        if (blockedFlash.value > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 4.dp,
                        color = Color(0xFFEF4444).copy(alpha = blockedFlash.value * 0.55f)
                    )
            )
        }

        // 8. Single, Original Level Victory Dialog
        if (showVictoryDialog) {
            com.example.ui.dialogs.LevelVictoryDialog(
                level = uiState.currentLevel,
                starsEarned = uiState.starsEarned,
                movesCount = uiState.movesCount,
                timeRemainingSeconds = uiState.timeRemainingSeconds,
                isDailyChallenge = uiState.isDailyChallenge,
                dailyChallengeDay = uiState.dailyChallengeDay,
                onNextLevelClick = {
                    showVictoryDialog = false
                    onNextLevelClick()
                },
                onHomeClick = {
                    showVictoryDialog = false
                    onBackClick()
                }
            )
        }

        // 8. Options Dialog matching video 01:08 - Instant in-window overlay
        if (showOptionsDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x66000000))
                    .clickable { showOptionsDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .widthIn(max = 440.dp)
                        .clickable(enabled = false) {}
                        .testTag("options_modal")
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Options",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                fontSize = 20.sp
                            )
                            TextButton(onClick = { showOptionsDialog = false }) {
                                Text(
                                    text = "Done",
                                    color = Color(0xFF2563EB),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Toggles for Sound and Vibration
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Sounds", fontWeight = FontWeight.Medium, color = Color(0xFF1E293B), fontSize = 14.sp)
                            Switch(
                                checked = uiState.soundEnabled,
                                onCheckedChange = { onToggleSound() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF2563EB)
                                )
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Vibration", fontWeight = FontWeight.Medium, color = Color(0xFF1E293B), fontSize = 14.sp)
                            Switch(
                                checked = uiState.hapticsEnabled,
                                onCheckedChange = { onToggleHaptics() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF2563EB)
                                )
                            )
                        }

                        HorizontalDivider(color = Color(0xFFE2E8F0), modifier = Modifier.padding(vertical = 6.dp))

                        Text(
                            text = "ℹ️ About Game",
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF475569),
                            fontSize = 14.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        )

                        Text(
                            text = "🛡️ Privacy Policy",
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF475569),
                            fontSize = 14.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        )
                    }
                }
            }
        }
    }
}
