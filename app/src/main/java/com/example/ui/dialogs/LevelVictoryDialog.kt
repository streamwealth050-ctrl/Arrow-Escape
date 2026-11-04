package com.example.ui.dialogs

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.LevelData
import com.example.util.DateHelper

/**
 * Single, original, and elegant Level Cleared Dialog for Arrow Escape.
 * Shows level achievement, star rating, coins reward, and next level action.
 */
@Composable
fun LevelVictoryDialog(
    level: LevelData,
    starsEarned: Int,
    movesCount: Int,
    timeRemainingSeconds: Int,
    isDailyChallenge: Boolean = false,
    dailyChallengeDay: Int = DateHelper.getTodayDayOfMonth(),
    onNextLevelClick: () -> Unit,
    onHomeClick: () -> Unit
) {
    var animateIn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animateIn = true
    }

    val scaleAnim by animateFloatAsState(
        targetValue = if (animateIn) 1f else 0.85f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "dialog_scale"
    )

    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .scale(scaleAnim)
                .testTag("level_victory_dialog")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                // Header badge
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFEFF6FF),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "🎉", fontSize = 32.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isDailyChallenge) "Challenge Cleared!" else "Level Cleared!",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    fontSize = 24.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isDailyChallenge) {
                        "${DateHelper.getCurrentMonthName()} $dailyChallengeDay Challenge • Completed!"
                    } else {
                        "${level.title} • ${level.shapeType.iconEmoji} ${level.shapeType.label} Maze"
                    },
                    color = Color(0xFF2563EB),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stars row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { idx ->
                        val earned = idx < starsEarned
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (earned) Color(0xFFF59E0B) else Color(0xFFE2E8F0),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Stats card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF8FAFC),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🎯 Moves", color = Color(0xFF64748B), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text(text = "$movesCount", color = Color(0xFF0F172A), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        Box(modifier = Modifier.size(1.dp, 28.dp).background(Color(0xFFE2E8F0)))

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "🪙 Reward", color = Color(0xFF64748B), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text(text = "+50", color = Color(0xFFB45309), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                var hasClickedNext by remember { mutableStateOf(false) }

                // Next Level Button
                Button(
                    onClick = {
                        if (!hasClickedNext) {
                            hasClickedNext = true
                            onNextLevelClick()
                        }
                    },
                    enabled = !hasClickedNext,
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("next_level_button")
                ) {
                    Text(
                        text = if (isDailyChallenge) "Done / Return to Menu" else "Next Level →",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onHomeClick,
                    modifier = Modifier.testTag("home_from_victory_button")
                ) {
                    Text(
                        text = "Main Menu",
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
