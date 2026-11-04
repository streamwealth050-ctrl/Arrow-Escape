package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.LeaderboardEntry
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.SkyBlue500
import com.example.ui.theme.SkyBlue600
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800

@Composable
fun LeaderboardScreen(
    entries: List<LeaderboardEntry>,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAFBFD))
            .padding(horizontal = 20.dp)
            .testTag("leaderboard_screen")
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Top Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.testTag("leaderboard_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Slate800
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Global Leaderboard",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Slate800,
                    fontSize = 22.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Top 3 Podium
        val top3 = entries.take(3)
        if (top3.size >= 3) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // 2nd Place
                PodiumColumn(entry = top3[1], rank = 2, height = 120.dp, color = Color(0xFF94A3B8))

                // 1st Place (Center, tallest)
                PodiumColumn(entry = top3[0], rank = 1, height = 148.dp, color = GoldAmber)

                // 3rd Place
                PodiumColumn(entry = top3[2], rank = 3, height = 100.dp, color = Color(0xFFB45309))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Full Rankings List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(entries) { item ->
                val isMe = item.isCurrentUser
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isMe) SkyBlue500.copy(alpha = 0.12f) else Color.White
                    ),
                    border = if (isMe) androidx.compose.foundation.BorderStroke(1.5.dp, SkyBlue600) else null,
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isMe) 4.dp else 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "#${item.rank}",
                            fontWeight = FontWeight.Bold,
                            color = if (isMe) SkyBlue600 else Slate700,
                            fontSize = 15.sp,
                            modifier = Modifier.width(36.dp)
                        )

                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = item.avatarEmoji, fontSize = 20.sp)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                fontWeight = if (isMe) FontWeight.Bold else FontWeight.SemiBold,
                                color = Slate800,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Level ${item.levelsCleared} cleared",
                                color = Slate700,
                                fontSize = 12.sp
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${item.points} pts",
                                fontWeight = FontWeight.Bold,
                                color = if (isMe) SkyBlue600 else Slate800,
                                fontSize = 14.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Stars",
                                    tint = GoldAmber,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${item.stars}",
                                    fontWeight = FontWeight.SemiBold,
                                    color = Slate700,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun PodiumColumn(
    entry: LeaderboardEntry,
    rank: Int,
    height: androidx.compose.ui.unit.Dp,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Avatar with crown
        Box(contentAlignment = Alignment.TopCenter) {
            Surface(
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .size(if (rank == 1) 56.dp else 46.dp)
                    .border(2.dp, color, CircleShape)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = entry.avatarEmoji, fontSize = if (rank == 1) 26.sp else 20.sp)
                }
            }
            if (rank == 1) {
                Text(text = "👑", fontSize = 14.sp, modifier = Modifier.padding(bottom = 44.dp))
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = entry.name.take(9),
            fontWeight = FontWeight.Bold,
            color = Slate800,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Pedestal block
        Surface(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            color = color.copy(alpha = 0.2f),
            modifier = Modifier
                .width(84.dp)
                .height(height)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "#$rank",
                    fontWeight = FontWeight.ExtraBold,
                    color = color,
                    fontSize = 20.sp
                )
                Text(
                    text = "${entry.points} pts",
                    fontWeight = FontWeight.Bold,
                    color = Slate800,
                    fontSize = 11.sp
                )
                Text(
                    text = "${entry.stars} ⭐",
                    fontWeight = FontWeight.SemiBold,
                    color = Slate700,
                    fontSize = 11.sp
                )
            }
        }
    }
}
