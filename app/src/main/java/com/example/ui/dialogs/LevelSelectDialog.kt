package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.LevelProgressEntity
import com.example.domain.model.Difficulty
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.SkyBlue600
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800

@Composable
fun LevelSelectDialog(
    maxUnlockedLevel: Int,
    currentLevelNumber: Int,
    progressList: List<LevelProgressEntity>,
    onSelectLevel: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val progressMap = progressList.associateBy { it.levelNumber }
    val totalDisplayLevels = maxOf(30, maxUnlockedLevel + 5)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp)
                .padding(8.dp)
                .testTag("level_select_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Select Level",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Slate800,
                                fontSize = 22.sp
                            )
                        )
                        Text(
                            text = "Unlocked: $maxUnlockedLevel",
                            color = Slate700,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Slate700)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Grid of levels
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(totalDisplayLevels) { index ->
                        val levelNum = index + 1
                        val isUnlocked = levelNum <= maxUnlockedLevel
                        val isCurrent = levelNum == currentLevelNumber
                        val progress = progressMap[levelNum]
                        val stars = progress?.stars ?: 0

                        val containerColor = when {
                            isCurrent -> SkyBlue600
                            isUnlocked -> Color(0xFFF1F5F9)
                            else -> Slate200.copy(alpha = 0.5f)
                        }

                        val textColor = when {
                            isCurrent -> Color.White
                            isUnlocked -> Slate800
                            else -> Color(0xFF94A3B8)
                        }

                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(containerColor)
                                .clickable(enabled = isUnlocked) {
                                    onSelectLevel(levelNum)
                                    onDismiss()
                                }
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isUnlocked) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "$levelNum",
                                        fontWeight = FontWeight.Bold,
                                        color = textColor,
                                        fontSize = 16.sp
                                    )
                                    if (stars > 0) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                                            for (s in 1..stars) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = if (isCurrent) Color.White else GoldAmber,
                                                    modifier = Modifier.size(10.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
