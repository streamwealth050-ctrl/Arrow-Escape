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

/**
 * Dark midnight vignette background matching Screenshot_20261025_214949.
 * Uses high-performance vertical vignette gradient with soft ambient radial lighting in the center.
 */
@Composable
fun MidnightVignetteBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF030712),
                        Color(0xFF071428),
                        Color(0xFF0B1F3D),
                        Color(0xFF061122),
                        Color(0xFF02050D)
                    )
                )
            )
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x3338BDF8),
                            Color(0x151E3A5F),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.45f),
                        radius = size.width * 0.85f
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.45f),
                    radius = size.width * 0.85f
                )
            }
    ) {
        content()
    }
}
