package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ArrowEscapeBrandLogo
import com.example.ui.components.BlueprintGridBackground
import com.example.ui.components.GameAppLogo
import com.example.ui.viewmodel.GameUiState
import com.example.util.DateHelper

enum class HomeBottomTab {
    MAIN,
    DAILY,
    ME
}

/**
 * Main Home screen for Arrow Escape with the dark BlueprintGridBackground
 * requested from Screenshot_20261025_214937.
 */
@Composable
fun MainHomeScreen(
    uiState: GameUiState,
    selectedTab: HomeBottomTab,
    onTabSelected: (HomeBottomTab) -> Unit,
    onNewGameClick: () -> Unit,
    onDailyPlayClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onToggleSound: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val levelNumber = uiState.campaignLevelNumber
    val shapeBadge = uiState.campaignLevel.shapeType.iconEmoji

    BlueprintGridBackground(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar: Brand label and Settings Gear
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GameAppLogo(sizeDp = 28)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Arrow Escape",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 17.sp
                    )
                }

                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.testTag("home_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color(0xFFE2E8F0),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Main content area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 1. Sleek Compact Daily Challenge Option
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .shadow(8.dp, RoundedCornerShape(20.dp), spotColor = Color(0x332563EB))
                            .clickable { onDailyPlayClick() }
                            .testTag("daily_challenge_card")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF0F1E36), Color(0xFF081426))
                                    )
                                )
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                GameAppLogo(sizeDp = 42, elevation = 3)

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = "DAILY CHALLENGE",
                                        color = Color(0xFF38BDF8),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.5.sp
                                    )
                                    val currentMonth = DateHelper.getCurrentMonthName()
                                    val currentDay = DateHelper.getTodayDayOfMonth()
                                    Text(
                                        text = "Today, $currentMonth $currentDay • Daily Challenge",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF2563EB),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                ) {
                                    Text(
                                        text = "Play",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 2. Brand Hero: Arrow Escape with original logo
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ArrowEscapeBrandLogo(
                            showTitle = false,
                            titleColor = Color.White,
                            subtitleColor = Color(0xFF94A3B8)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Arrow Escape",
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 32.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Solve paths • Escape the maze",
                            color = Color(0xFF94A3B8),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // 3. New Game Button
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        Button(
                            onClick = onNewGameClick,
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2563EB)
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(56.dp)
                                .testTag("new_game_button")
                        ) {
                            Text(
                                text = "Play Level $levelNumber  $shapeBadge",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Shape: ${uiState.campaignLevel.shapeType.label}",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Bottom Navigation Bar with 3 tabs: Main, Daily, Me
            HorizontalDivider(color = Color(0xFF16253D), thickness = 1.dp)

            NavigationBar(
                containerColor = Color(0xFF060E1C),
                tonalElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
            ) {
                NavigationBarItem(
                    selected = selectedTab == HomeBottomTab.MAIN,
                    onClick = { onTabSelected(HomeBottomTab.MAIN) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Main",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Main",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == HomeBottomTab.MAIN) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF38BDF8),
                        selectedTextColor = Color(0xFF38BDF8),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B),
                        indicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.testTag("tab_main")
                )

                NavigationBarItem(
                    selected = selectedTab == HomeBottomTab.DAILY,
                    onClick = { onTabSelected(HomeBottomTab.DAILY) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Daily & Rankings",
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Daily",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == HomeBottomTab.DAILY) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF38BDF8),
                        selectedTextColor = Color(0xFF38BDF8),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B),
                        indicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.testTag("tab_daily")
                )

                NavigationBarItem(
                    selected = selectedTab == HomeBottomTab.ME,
                    onClick = { onTabSelected(HomeBottomTab.ME) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Me",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Me",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == HomeBottomTab.ME) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF38BDF8),
                        selectedTextColor = Color(0xFF38BDF8),
                        unselectedIconColor = Color(0xFF64748B),
                        unselectedTextColor = Color(0xFF64748B),
                        indicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.testTag("tab_me")
                )
            }
        }
    }
}
