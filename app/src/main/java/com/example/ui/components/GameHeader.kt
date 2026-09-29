package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HeartRed
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.viewmodel.GameUiState

@Composable
fun GameHeader(
    uiState: GameUiState,
    onBackOrSelectClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Top Navigation & Level Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackOrSelectClick,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("nav_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back or Select Level",
                    tint = Slate800
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Level ${uiState.currentLevel.levelNumber}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Slate800,
                        fontSize = 22.sp
                    )
                )
                Text(
                    text = uiState.currentLevel.title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Slate700,
                        fontSize = 12.sp
                    )
                )
            }

            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("nav_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Slate800
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Status Row: Remaining Arrows Badge, 3 Hearts, Difficulty Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Remaining arrows badge
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Slate200.copy(alpha = 0.6f),
                modifier = Modifier.testTag("remaining_arrows_badge")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NearMe,
                        contentDescription = "Arrows Remaining",
                        tint = Slate800,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${uiState.activeArrows.size}",
                        fontWeight = FontWeight.Bold,
                        color = Slate800,
                        fontSize = 14.sp
                    )
                }
            }

            // Three Hearts / Lives
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.testTag("hearts_row")
            ) {
                for (heartIndex in 1..uiState.maxLives) {
                    val isAlive = heartIndex <= uiState.remainingLives
                    val heartColor by animateColorAsState(
                        targetValue = if (isAlive) HeartRed else Slate200,
                        animationSpec = tween(300),
                        label = "heart_color"
                    )
                    Icon(
                        imageVector = if (isAlive) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isAlive) "Active Life" else "Lost Life",
                        tint = heartColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Difficulty Badge
            val difficulty = uiState.currentLevel.difficulty
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = difficulty.badgeColor.copy(alpha = 0.15f),
                modifier = Modifier.testTag("difficulty_badge")
            ) {
                Text(
                    text = difficulty.label,
                    color = difficulty.badgeColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Timer Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Timer",
                    tint = if (uiState.timeRemainingSeconds <= 15) HeartRed else Slate700,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                val minutes = uiState.timeRemainingSeconds / 60
                val seconds = uiState.timeRemainingSeconds % 60
                Text(
                    text = String.format("%02d:%02d", minutes, seconds),
                    fontWeight = FontWeight.SemiBold,
                    color = if (uiState.timeRemainingSeconds <= 15) HeartRed else Slate800,
                    fontSize = 14.sp
                )
            }

            val progress = if (uiState.totalTimeSeconds > 0) {
                uiState.timeRemainingSeconds.toFloat() / uiState.totalTimeSeconds
            } else 0f

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
                    .height(6.dp)
                    .clip(CircleShape),
                color = if (uiState.timeRemainingSeconds <= 15) HeartRed else MaterialTheme.colorScheme.secondary,
                trackColor = Slate200
            )
        }
    }
}
