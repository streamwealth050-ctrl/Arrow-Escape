package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.HeartRed
import com.example.ui.theme.SkyBlue600
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800

/**
 * Ultra-fast In-Window Settings Modal.
 * Appears instantly on touch with zero WindowManager IPC overhead.
 */
@Composable
fun SettingsDialog(
    soundEnabled: Boolean,
    hapticsEnabled: Boolean,
    onToggleSound: () -> Unit,
    onToggleHaptics: () -> Unit,
    onResetProgress: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x66000000))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 440.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {} // Consume click inside card so backdrop doesn't dismiss
                )
                .testTag("settings_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Title and Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Slate800,
                            fontSize = 22.sp
                        )
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("settings_close_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Slate700)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sound FX Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = SkyBlue600
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Sound Effects",
                            fontWeight = FontWeight.Medium,
                            color = Slate800,
                            fontSize = 16.sp
                        )
                    }
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = { onToggleSound() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SkyBlue600
                        ),
                        modifier = Modifier.testTag("sound_toggle")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Slate200)
                Spacer(modifier = Modifier.height(12.dp))

                // Haptic Feedback Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = null,
                            tint = SkyBlue600
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Haptic Vibration",
                            fontWeight = FontWeight.Medium,
                            color = Slate800,
                            fontSize = 16.sp
                        )
                    }
                    Switch(
                        checked = hapticsEnabled,
                        onCheckedChange = { onToggleHaptics() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SkyBlue600
                        ),
                        modifier = Modifier.testTag("haptics_toggle")
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Reset Progress
                OutlinedButton(
                    onClick = {
                        onResetProgress()
                        onDismiss()
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HeartRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("reset_progress_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = HeartRed
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reset All Progress",
                        fontWeight = FontWeight.SemiBold,
                        color = HeartRed
                    )
                }
            }
        }
    }
}
