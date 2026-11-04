package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun HelpDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().testTag("help_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "How to Play Arrow Escape",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "🎯 Objective:",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB),
                    fontSize = 15.sp
                )
                Text(
                    text = "Tap arrows that have a clear, unobstructed path toward the edge of the board. Free all arrows to clear the maze!",
                    color = Color(0xFF334155),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "📐 Unique Maze Shapes:",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB),
                    fontSize = 15.sp
                )
                Text(
                    text = "Levels feature distinctive shapes like Heart ❤️, Triangle 🔺, Diamond 🔷, and Cross ➕. Arrows escape when their path clears the shape boundary.",
                    color = Color(0xFF334155),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "❤️ Lives & Tips:",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2563EB),
                    fontSize = 15.sp
                )
                Text(
                    text = "You have 3 hearts. Tapping a blocked arrow loses 1 heart. Use the Lightbulb hint or the Crusher tool if you get stuck!",
                    color = Color(0xFF334155),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(text = "Got It!", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().testTag("about_dialog")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                com.example.ui.components.GameAppLogo(sizeDp = 56, elevation = 4)

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Arrow Escape",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    fontSize = 22.sp
                )
                Text(
                    text = "Version 3.6.0 (Build 9)",
                    color = Color(0xFF64748B),
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Arrow Escape is an original relaxing logic puzzle game. Designed with Jetpack Compose, Material 3, and authentic procedural reverse-solvable algorithms.",
                    color = Color(0xFF475569),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "© 2026 Arrow Escape Studio. All rights reserved.",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(text = "Close", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().testTag("privacy_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Privacy Policy",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Your privacy is our priority. Arrow Escape does not collect, sell, or share personal identifying information.\n\n" +
                            "• Gameplay Progress: Stored locally on your device via Room SQLite database.\n" +
                            "• Analytics & Crashes: Anonymous diagnostic data used solely for stability.\n" +
                            "• Permissions: VIBRATE is used for tactile feedback. No camera, location, or sensitive storage access is requested.\n\n" +
                            "For inquiries or data rights requests, contact support@arrowescape.game.",
                    color = Color(0xFF334155),
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(text = "Understood", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
