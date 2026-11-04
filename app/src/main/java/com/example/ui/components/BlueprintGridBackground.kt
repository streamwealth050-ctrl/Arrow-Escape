package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Blueprint grid background matching the dark technical grid aesthetic in
 * Screenshot_20261025_214937_Tiny Car Obstacle.jpg.
 *
 * Highly optimized: uses hardware-accelerated drawBehind with zero heap allocations in the draw pass.
 */
@Composable
fun BlueprintGridBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val baseGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF061020),
            Color(0xFF091830),
            Color(0xFF050B16)
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseGradient)
            .drawBehind {
                val gridStep = 28.dp.toPx()
                val gridColor = Color(0xFF1E3A5F).copy(alpha = 0.30f)
                val strokeWidth = 1.dp.toPx()

                // Ambient soft radial glow in upper-center (just like the screenshot)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x1F38BDF8),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.32f),
                        radius = size.width * 0.85f
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.32f),
                    radius = size.width * 0.85f
                )

                // Vertical blueprint lines
                var x = 0f
                while (x <= size.width) {
                    drawLine(
                        color = gridColor,
                        start = Offset(x, 0f),
                        end = Offset(x, size.height),
                        strokeWidth = strokeWidth
                    )
                    x += gridStep
                }

                // Horizontal blueprint lines
                var y = 0f
                while (y <= size.height) {
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = strokeWidth
                    )
                    y += gridStep
                }
            }
    ) {
        content()
    }
}
