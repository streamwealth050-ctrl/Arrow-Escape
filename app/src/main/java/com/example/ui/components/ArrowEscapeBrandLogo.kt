package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * Universal Game App Logo component for Arrow Escape.
 * Renders the real, authentic vector launcher icon of the app at any size.
 */
@Composable
fun GameAppLogo(
    modifier: Modifier = Modifier,
    sizeDp: Int = 40,
    elevation: Int = 0
) {
    val cornerRadius = (sizeDp * 0.22f).coerceIn(4f, 28f).dp
    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .then(
                if (elevation > 0) {
                    Modifier.shadow(elevation.dp, RoundedCornerShape(cornerRadius), spotColor = Color(0x440F172A))
                } else Modifier
            )
            .clip(RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_game_logo),
            contentDescription = "Arrow Escape Game Logo",
            modifier = Modifier.size(sizeDp.dp)
        )
    }
}

/**
 * Full branded Arrow Escape logo card with optional title and animated pulse.
 */
@Composable
fun ArrowEscapeBrandLogo(
    modifier: Modifier = Modifier,
    sizeDp: Int = 114,
    showTitle: Boolean = true,
    animated: Boolean = true,
    titleColor: Color = Color(0xFF0F172A),
    subtitleColor: Color = Color(0xFF64748B)
) {
    val infiniteTransition = rememberInfiniteTransition(label = "cyan_arrow_escape")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_pulse"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        GameAppLogo(
            modifier = if (animated) {
                Modifier.graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
            } else Modifier,
            sizeDp = sizeDp,
            elevation = 10
        )

        if (showTitle) {
            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Arrow Escape",
                color = titleColor,
                fontWeight = FontWeight.Bold,
                fontSize = 25.sp,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Unlock the Maze",
                color = subtitleColor,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )
        }
    }
}
