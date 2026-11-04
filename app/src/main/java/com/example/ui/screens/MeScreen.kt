package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.logic.DenseMazeGenerator
import com.example.ui.dialogs.AboutDialog
import com.example.ui.dialogs.HelpDialog
import com.example.ui.dialogs.PrivacyPolicyDialog
import com.example.ui.viewmodel.GameUiState

@Composable
fun MeScreen(
    uiState: GameUiState,
    onSelectLevel: (Int) -> Unit,
    onToggleSound: () -> Unit,
    onToggleHaptics: () -> Unit,
    onUpdateProfile: (String, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val maxUnlocked = if (uiState.maxUnlockedLevel >= 1000) maxOf(uiState.campaignLevelNumber, 1) else maxOf(uiState.maxUnlockedLevel, 1)
    val myEntry = uiState.leaderboardEntries.firstOrNull { it.isCurrentUser }
    val userRank = myEntry?.rank ?: 1
    val userPoints = myEntry?.points ?: 0

    var showHelpDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var isLevelSelectExpanded by remember { mutableStateOf(false) }
    var selectedLevelChapter by remember { mutableIntStateOf(0) } // 0 = All/Recent, 1 = 1-10, 2 = 11-20, etc.

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAFBFD))
            .testTag("me_screen"),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // 1. Top Header
        item {
            Surface(
                color = Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Me",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        fontSize = 22.sp
                    )
                    Text(
                        text = "Profile & Settings",
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2563EB),
                        fontSize = 15.sp
                    )
                }
            }
        }

        // 1.5 Real Player Profile Card (Allows editing name & avatar for real leaderboard!)
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("me_player_profile_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFDBEAFE),
                            modifier = Modifier
                                .size(52.dp)
                                .clickable { showEditProfileDialog = true }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = uiState.userProfile.avatarEmoji, fontSize = 26.sp)
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = uiState.userProfile.name,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                fontSize = 17.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Rank #$userRank • $userPoints pts",
                                color = Color(0xFF2563EB),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEFF6FF),
                        modifier = Modifier.clickable { showEditProfileDialog = true }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Edit",
                                color = Color(0xFF2563EB),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // 2. Progress Overview Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("me_progress_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFEFF6FF),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Level",
                                        tint = Color(0xFF2563EB),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Current Highest Level",
                                    color = Color(0xFF64748B),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Level $maxUnlocked",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    fontSize = 20.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${maxUnlocked * 3} Stars",
                                    color = Color(0xFFB45309),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Unlocked: 1 to $maxUnlocked",
                            color = Color(0xFF475569),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${uiState.userProfile.totalArrowsCleared} Total Escapes",
                            color = Color(0xFF2563EB),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { (maxUnlocked % 10) / 10f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = Color(0xFF2563EB),
                        trackColor = Color(0xFFE2E8F0)
                    )
                }
            }
        }

        // 3. Compact & Expandable Level Select Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("replay_levels_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Select & Replay Levels",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A),
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Tap any unlocked level to play it again",
                                color = Color(0xFF64748B),
                                fontSize = 12.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFDBEAFE)
                        ) {
                            Text(
                                text = "$maxUnlocked Unlocked",
                                color = Color(0xFF1D4ED8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val allLevels = (1..maxUnlocked).toList()

                    // If expanded and many levels, show Chapter filters (e.g. 1-10, 11-20, etc.)
                    if (isLevelSelectExpanded && maxUnlocked > 10) {
                        val numChapters = (maxUnlocked + 9) / 10
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // "All" chip
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (selectedLevelChapter == 0) Color(0xFF2563EB) else Color(0xFFF1F5F9),
                                modifier = Modifier
                                    .clickable { selectedLevelChapter = 0 }
                            ) {
                                Text(
                                    text = "All",
                                    color = if (selectedLevelChapter == 0) Color.White else Color(0xFF475569),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            // Dynamic chapter chips
                            for (ch in 1..minOf(numChapters, 5)) {
                                val startLvl = (ch - 1) * 10 + 1
                                val endLvl = minOf(ch * 10, maxUnlocked)
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selectedLevelChapter == ch) Color(0xFF2563EB) else Color(0xFFF1F5F9),
                                    modifier = Modifier
                                        .clickable { selectedLevelChapter = ch }
                                ) {
                                    Text(
                                        text = "$startLvl-$endLvl",
                                        color = if (selectedLevelChapter == ch) Color.White else Color(0xFF475569),
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    val levelsToDisplay = when {
                        !isLevelSelectExpanded && maxUnlocked > 8 -> {
                            // Compact mode: show current level and 7 most recent
                            val start = maxOf(1, maxUnlocked - 7)
                            (start..maxUnlocked).toList()
                        }
                        isLevelSelectExpanded && selectedLevelChapter > 0 -> {
                            val start = (selectedLevelChapter - 1) * 10 + 1
                            val end = minOf(selectedLevelChapter * 10, maxUnlocked)
                            (start..end).toList()
                        }
                        else -> allLevels
                    }

                    val rows = (levelsToDisplay.size + 3) / 4

                    for (rowIdx in 0 until rows) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (colIdx in 0 until 4) {
                                val itemIdx = rowIdx * 4 + colIdx
                                if (itemIdx < levelsToDisplay.size) {
                                    val lvl = levelsToDisplay[itemIdx]
                                    val levelData = DenseMazeGenerator.getLevel(lvl)
                                    val isCurrent = lvl == uiState.campaignLevelNumber

                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isCurrent) Color(0xFF2563EB) else Color(0xFFF8FAFC),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isCurrent) Color(0xFF2563EB) else Color(0xFFE2E8F0)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { onSelectLevel(lvl) }
                                            .testTag("select_level_$lvl")
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                                        ) {
                                            Text(
                                                text = levelData.shapeType.iconEmoji,
                                                fontSize = 13.sp
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Lvl $lvl",
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCurrent) Color.White else Color(0xFF0F172A),
                                                fontSize = 11.sp
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(horizontalArrangement = Arrangement.Center) {
                                                repeat(3) {
                                                    Icon(
                                                        imageVector = Icons.Default.Star,
                                                        contentDescription = null,
                                                        tint = if (isCurrent) Color(0xFFFDE047) else Color(0xFFF59E0B),
                                                        modifier = Modifier.size(9.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    // Expand / Collapse button if more than 8 levels
                    if (maxUnlocked > 8) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isLevelSelectExpanded = !isLevelSelectExpanded }
                                .padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isLevelSelectExpanded) "Show Less ▲" else "View All $maxUnlocked Levels ▾",
                                color = Color(0xFF2563EB),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 7.dp)
                            )
                        }
                    }
                }
            }
        }

        // 4. Sound & Vibration Settings Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Game Settings",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        fontSize = 16.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Sounds", fontWeight = FontWeight.Medium, color = Color(0xFF1E293B), fontSize = 14.sp)
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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Vibration", fontWeight = FontWeight.Medium, color = Color(0xFF1E293B), fontSize = 14.sp)
                        Switch(
                            checked = uiState.hapticsEnabled,
                            onCheckedChange = { onToggleHaptics() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF2563EB)
                            )
                        )
                    }
                }
            }
        }

        // 5. Genuine App Information & Trust Section (Requested by user: "about and that policy and help should be there, every genuine app has that")
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Support & Legal",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    SupportRowItem(
                        icon = Icons.Default.HelpOutline,
                        title = "How to Play & Game Rules",
                        onClick = { showHelpDialog = true }
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    SupportRowItem(
                        icon = Icons.Default.Info,
                        title = "About Arrow Escape",
                        onClick = { showAboutDialog = true }
                    )

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    SupportRowItem(
                        icon = Icons.Default.Security,
                        title = "Privacy Policy",
                        onClick = { showPrivacyDialog = true }
                    )
                }
            }
        }
    }

    if (showHelpDialog) {
        HelpDialog(onDismiss = { showHelpDialog = false })
    }
    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }
    if (showPrivacyDialog) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false })
    }
    if (showEditProfileDialog) {
        EditProfileDialog(
            initialName = uiState.userProfile.name,
            initialAvatar = uiState.userProfile.avatarEmoji,
            onSave = { name, avatar ->
                onUpdateProfile(name, avatar)
                showEditProfileDialog = false
            },
            onDismiss = { showEditProfileDialog = false }
        )
    }
}

@Composable
private fun SupportRowItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF2563EB),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF334155),
                fontSize = 14.sp
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun EditProfileDialog(
    initialName: String,
    initialAvatar: String,
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedAvatar by remember { mutableStateOf(initialAvatar) }
    val avatarChoices = listOf("👑", "⚡", "🎯", "🦊", "🥷", "🧙", "🧘", "🦁", "🐼", "🚀", "⭐", "🏹")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().testTag("edit_profile_dialog")
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Edit Player Profile",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    fontSize = 19.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { if (it.length <= 16) name = it },
                    label = { Text("Player Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("player_name_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Choose Your Avatar",
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF475569),
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (av in avatarChoices.take(6)) {
                        val isSelected = av == selectedAvatar
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) Color(0xFFDBEAFE) else Color(0xFFF8FAFC),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF2563EB)) else null,
                            modifier = Modifier
                                .size(38.dp)
                                .clickable { selectedAvatar = av }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = av, fontSize = 20.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (av in avatarChoices.drop(6)) {
                        val isSelected = av == selectedAvatar
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) Color(0xFFDBEAFE) else Color(0xFFF8FAFC),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF2563EB)) else null,
                            modifier = Modifier
                                .size(38.dp)
                                .clickable { selectedAvatar = av }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = av, fontSize = 20.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = Color(0xFF64748B))
                    }
                    Button(
                        onClick = {
                            onSave(name, selectedAvatar)
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        modifier = Modifier.weight(1f).testTag("save_profile_button")
                    ) {
                        Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
