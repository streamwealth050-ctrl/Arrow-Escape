package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.LeaderboardEntry
import com.example.ui.components.TrophyGraphic
import com.example.util.DateHelper

/**
 * Daily Challenges screen inspired by Screenshot_20261029_113844:
 * - Radiant sky-blue header banner with 3D metallic Trophy Cup
 * - Clean calendar layout with monthly star completion counter (e.g. ⭐ 1/31)
 * - Today's date highlighted with a gold star badge
 * - Future dates locked and disabled
 * - High-elevation rounded blue Play button
 */
@Composable
fun DailyScreen(
    leaderboardEntries: List<LeaderboardEntry>,
    onPlayDaily: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = DateHelper.getTodayDayOfMonth()
    val monthName = DateHelper.getCurrentMonthName()
    val monthYear = DateHelper.getCurrentMonthYear()
    val daysInMonth = DateHelper.getDaysInCurrentMonth()
    val firstDayOffset = DateHelper.getFirstDayOfWeekOffset()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Calendar, 1 = Leaderboard
    var selectedDay by remember(today) { mutableIntStateOf(today) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag("daily_screen")
    ) {
        // 1. TOP HERO BANNER: Radiant Sky Blue with 3D Metallic Trophy Cup
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.38f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0284C7),
                            Color(0xFF0EA5E9),
                            Color(0xFF38BDF8)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header row: Title + Segmented tab switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily Challenges",
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    // Minimal segmented tab switch: Calendar / Leaderboard
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.25f),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (selectedTab == 0) Color.White else Color.Transparent)
                                    .clickable { selectedTab = 0 }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                    .testTag("tab_daily_calendar"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Calendar",
                                    color = if (selectedTab == 0) Color(0xFF0284C7) else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (selectedTab == 1) Color.White else Color.Transparent)
                                    .clickable { selectedTab = 1 }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                    .testTag("tab_daily_leaderboard"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Rankings",
                                    color = if (selectedTab == 1) Color(0xFF0284C7) else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // 3D Metallic Trophy Cup Graphic
                TrophyGraphic(
                    sizeDp = 135.dp,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }

        // 2. BOTTOM SECTION: Calendar or Leaderboard
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.62f)
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            if (selectedTab == 0) {
                // CALENDAR VIEW (Inspired by Screenshot_20261029_113844)
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Month Header with Star Counter: "October 2026    ⭐ 1/31"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = monthYear,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                fontSize = 17.sp
                            )

                            // Star Counter badge: ⭐ 1/31
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF59E0B)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "1/$daysInMonth",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Weekday labels: M T W T F S S
                        val dayNames = listOf("M", "T", "W", "T", "F", "S", "S")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            for (d in dayNames) {
                                Text(
                                    text = d,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Month Day Grid
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(7),
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(firstDayOffset) {
                                Box(modifier = Modifier.aspectRatio(1f))
                            }

                            items(daysInMonth) { index ->
                                val day = index + 1
                                val isFuture = day > today
                                val isToday = day == today
                                val isSelected = day == selectedDay && !isFuture

                                Box(
                                    modifier = Modifier
                                        .aspectRatio(1f)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isSelected -> Color(0xFF2563EB)
                                                isToday -> Color(0xFFF59E0B) // Amber star badge for Today
                                                else -> Color.Transparent
                                            }
                                        )
                                        .then(
                                            if (!isFuture) {
                                                Modifier.clickable { selectedDay = day }
                                            } else Modifier
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isToday && !isSelected) {
                                        // Golden Star Icon for Today's Challenge (matches screenshot)
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Today",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "$day",
                                            color = when {
                                                isFuture -> Color(0xFFCBD5E1) // disabled grey
                                                isSelected -> Color.White
                                                else -> Color(0xFF334155)
                                            },
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // High-Elevation Blue "Play" Button (matches screenshot)
                    val isSelectedFuture = selectedDay > today
                    Button(
                        onClick = { if (!isSelectedFuture) onPlayDaily(selectedDay) },
                        enabled = !isSelectedFuture,
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB),
                            disabledContainerColor = Color(0xFF94A3B8)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .shadow(8.dp, RoundedCornerShape(26.dp), spotColor = Color(0x442563EB))
                            .testTag("play_daily_button")
                    ) {
                        Text(
                            text = if (isSelectedFuture) "Locked • Available on $monthName $selectedDay" else "Play",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                // LEADERBOARD VIEW
                val myEntry = leaderboardEntries.firstOrNull { it.isCurrentUser } ?: leaderboardEntries.firstOrNull()
                val userRank = myEntry?.rank ?: 1
                val userPoints = myEntry?.points ?: 0

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Your Standing",
                                        color = Color(0xFF2563EB),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Rank #$userRank",
                                        color = Color(0xFF0F172A),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFF2563EB),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "$userPoints pts",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    itemsIndexed(leaderboardEntries) { index, entry ->
                        val isUser = entry.isCurrentUser
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUser) Color(0xFFDBEAFE) else Color(0xFFF8FAFC)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "#${entry.rank}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (entry.rank <= 3) Color(0xFFF59E0B) else Color(0xFF64748B),
                                        modifier = Modifier.width(32.dp)
                                    )
                                    Text(text = entry.avatarEmoji, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = entry.name,
                                            fontWeight = if (isUser) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = "${entry.levelsCleared} solved • ${entry.stars}★",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                Text(
                                    text = "${entry.points} pts",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFF2563EB)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
