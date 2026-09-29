package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.SkyBlue600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800

@Composable
fun GameControls(
    onHintClick: () -> Unit,
    onRestartClick: () -> Unit,
    onLevelSelectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Restart Level Button
        FloatingActionButton(
            onClick = onRestartClick,
            shape = CircleShape,
            containerColor = Color.White,
            contentColor = Slate800,
            elevation = FloatingActionButtonDefaults.elevation(4.dp),
            modifier = Modifier
                .size(56.dp)
                .testTag("restart_button")
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Restart Level",
                modifier = Modifier.size(26.dp)
            )
        }

        // Hint Button (Prominent center button)
        Box(contentAlignment = Alignment.TopEnd) {
            FloatingActionButton(
                onClick = onHintClick,
                shape = CircleShape,
                containerColor = Color.White,
                contentColor = SkyBlue600,
                elevation = FloatingActionButtonDefaults.elevation(6.dp),
                modifier = Modifier
                    .size(68.dp)
                    .testTag("hint_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = "Get Hint",
                    tint = SkyBlue600,
                    modifier = Modifier.size(34.dp)
                )
            }

            Surface(
                shape = CircleShape,
                color = SkyBlue600,
                modifier = Modifier
                    .size(22.dp)
                    .shadow(2.dp, CircleShape)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "💡",
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Level Select Button
        FloatingActionButton(
            onClick = onLevelSelectClick,
            shape = CircleShape,
            containerColor = Color.White,
            contentColor = Slate800,
            elevation = FloatingActionButtonDefaults.elevation(4.dp),
            modifier = Modifier
                .size(56.dp)
                .testTag("level_select_button")
        ) {
            Icon(
                imageVector = Icons.Default.GridOn,
                contentDescription = "Select Level",
                tint = Slate700,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}
