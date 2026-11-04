package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 3D Metallic Trophy Cup illustration inspired by Screenshot_20261029_113844.
 * Rendered using Canvas with metallic gradients and glowing ambient aura.
 */
@Composable
fun TrophyGraphic(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 150.dp
) {
    Box(
        modifier = modifier.size(sizeDp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(sizeDp)) {
            val w = size.width
            val h = size.height

            // Soft glowing ambient aura behind cup
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x667DD3FC), Color(0x2238BDF8), Color.Transparent),
                    center = Offset(w * 0.5f, h * 0.42f),
                    radius = w * 0.48f
                ),
                radius = w * 0.48f,
                center = Offset(w * 0.5f, h * 0.42f)
            )

            // Ambient sparkle particles
            val sparkleColor = Color.White.copy(alpha = 0.65f)
            drawCircle(sparkleColor, radius = 2.5.dp.toPx(), center = Offset(w * 0.28f, h * 0.33f))
            drawCircle(sparkleColor, radius = 1.8.dp.toPx(), center = Offset(w * 0.74f, h * 0.36f))
            drawCircle(sparkleColor, radius = 3.0.dp.toPx(), center = Offset(w * 0.34f, h * 0.60f))
            drawCircle(sparkleColor, radius = 2.0.dp.toPx(), center = Offset(w * 0.68f, h * 0.58f))

            // Metallic gradient with cyan/chrome sheen
            val metallicBrush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF0F2B48),
                    Color(0xFF1E5B7E),
                    Color(0xFF38BDF8),
                    Color(0xFFE0F2FE),
                    Color(0xFF1E5B7E),
                    Color(0xFF0A1E32)
                )
            )

            // 1. Curved ear handles
            val handleStroke = 3.5.dp.toPx()
            val leftHandle = Path().apply {
                moveTo(w * 0.36f, h * 0.22f)
                cubicTo(w * 0.16f, h * 0.18f, w * 0.16f, h * 0.42f, w * 0.38f, h * 0.44f)
            }
            drawPath(leftHandle, brush = metallicBrush, style = Stroke(handleStroke, cap = StrokeCap.Round))

            val rightHandle = Path().apply {
                moveTo(w * 0.64f, h * 0.22f)
                cubicTo(w * 0.84f, h * 0.18f, w * 0.84f, h * 0.42f, w * 0.62f, h * 0.44f)
            }
            drawPath(rightHandle, brush = metallicBrush, style = Stroke(handleStroke, cap = StrokeCap.Round))

            // 2. Main Cup Bowl
            val cupPath = Path().apply {
                moveTo(w * 0.32f, h * 0.18f)
                lineTo(w * 0.68f, h * 0.18f)
                cubicTo(w * 0.68f, h * 0.38f, w * 0.58f, h * 0.52f, w * 0.50f, h * 0.54f)
                cubicTo(w * 0.42f, h * 0.52f, w * 0.32f, h * 0.38f, w * 0.32f, h * 0.18f)
                close()
            }
            drawPath(cupPath, brush = metallicBrush)

            // Cup top oval lip
            drawOval(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF0E2C48), Color(0xFF7DD3FC), Color(0xFF0A1E32))
                ),
                topLeft = Offset(w * 0.31f, h * 0.16f),
                size = Size(w * 0.38f, h * 0.05f)
            )

            // 3. Slender Stem / Pillar
            val stemPath = Path().apply {
                moveTo(w * 0.46f, h * 0.54f)
                lineTo(w * 0.54f, h * 0.54f)
                lineTo(w * 0.52f, h * 0.72f)
                lineTo(w * 0.48f, h * 0.72f)
                close()
            }
            drawPath(stemPath, brush = metallicBrush)

            // Stem decorative ring
            drawRoundRect(
                brush = metallicBrush,
                topLeft = Offset(w * 0.44f, h * 0.62f),
                size = Size(w * 0.12f, h * 0.03f),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )

            // 4. Base Pedestal
            drawRoundRect(
                brush = metallicBrush,
                topLeft = Offset(w * 0.42f, h * 0.72f),
                size = Size(w * 0.16f, h * 0.04f),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
            )

            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF0F1E32), Color(0xFF1E3A5F), Color(0xFF0F1E32))
                ),
                topLeft = Offset(w * 0.37f, h * 0.76f),
                size = Size(w * 0.26f, h * 0.06f),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
            )

            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF0A1422), Color(0xFF162A45), Color(0xFF0A1422))
                ),
                topLeft = Offset(w * 0.34f, h * 0.82f),
                size = Size(w * 0.32f, h * 0.045f),
                cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
            )

            // Base ground glow
            drawOval(
                color = Color(0x330284C7),
                topLeft = Offset(w * 0.30f, h * 0.865f),
                size = Size(w * 0.40f, h * 0.035f)
            )
        }
    }
}
