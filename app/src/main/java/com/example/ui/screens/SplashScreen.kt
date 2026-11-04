package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.example.ui.components.ArrowEscapeBrandLogo
import com.example.ui.components.MidnightVignetteBackground

/**
 * Loading/Splash screen featuring the dark midnight vignette background with ambient radial glow
 * matching Screenshot_20261025_214949, showcasing the authentic Arrow Escape brand.
 */
@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    MidnightVignetteBackground(
        modifier = modifier.testTag("splash_screen")
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ArrowEscapeBrandLogo(
                showTitle = true,
                animated = true,
                titleColor = Color.White,
                subtitleColor = Color(0xFF94A3B8)
            )
        }
    }
}
