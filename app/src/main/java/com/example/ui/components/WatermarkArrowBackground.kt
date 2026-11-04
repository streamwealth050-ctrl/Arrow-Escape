package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Ultra-fast, zero-overhead background.
 * Uses pure GPU gradient with zero Canvas paths or CPU drawing loops.
 */
@Composable
fun WatermarkArrowBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFAFBFE),
                        Color(0xFFF1F5F9)
                    )
                )
            )
    ) {
        content()
    }
}
