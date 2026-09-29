package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.domain.model.Arrow
import com.example.domain.model.Direction
import com.example.domain.model.GridPoint
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.HeartRed
import com.example.ui.theme.SkyBlue600
import com.example.ui.theme.Slate800
import com.example.ui.viewmodel.GameUiState
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun GameBoard(
    uiState: GameUiState,
    onArrowTapped: (Arrow) -> Unit,
    modifier: Modifier = Modifier
) {
    val level = uiState.currentLevel
    val gridWidth = level.gridWidth
    val gridHeight = level.gridHeight

    // Pulsing halo animation for hinted arrow
    val infiniteTransition = rememberInfiniteTransition(label = "hint_transition")
    val hintPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hint_pulse"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0x1A0F172A)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(12.dp)
            .testTag("game_board"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f)
                .pointerInput(uiState.activeArrows, gridWidth, gridHeight) {
                    detectTapGestures { tapOffset ->
                        val cellSize = min(
                            size.width / (gridWidth + 0.6f),
                            size.height / (gridHeight + 0.6f)
                        )
                        val originX = (size.width - (gridWidth - 1) * cellSize) / 2f
                        val originY = (size.height - (gridHeight - 1) * cellSize) / 2f

                        // Find closest arrow to tapOffset
                        var bestArrow: Arrow? = null
                        var minDistance = Float.MAX_VALUE
                        val maxHitDistance = max(cellSize * 0.6f, 48.dp.toPx())

                        for (arrow in uiState.activeArrows) {
                            if (uiState.escapingArrowIds.contains(arrow.id)) continue
                            val dist = calculateDistanceToArrow(
                                tapOffset,
                                arrow,
                                originX,
                                originY,
                                cellSize
                            )
                            if (dist < minDistance && dist <= maxHitDistance) {
                                minDistance = dist
                                bestArrow = arrow
                            }
                        }

                        bestArrow?.let { onArrowTapped(it) }
                    }
                }
        ) {
            val cellSize = min(
                size.width / (gridWidth + 0.6f),
                size.height / (gridHeight + 0.6f)
            )
            val originX = (size.width - (gridWidth - 1) * cellSize) / 2f
            val originY = (size.height - (gridHeight - 1) * cellSize) / 2f

            // 1. Draw subtle background dot grid
            drawGridDots(gridWidth, gridHeight, originX, originY, cellSize)

            // 2. Draw active arrows
            for (arrow in uiState.activeArrows) {
                val isEscaping = uiState.escapingArrowIds.contains(arrow.id)
                val isBlocked = uiState.blockedArrowIds.contains(arrow.id)
                val isHinted = uiState.hintedArrowId == arrow.id

                drawSingleArrow(
                    arrow = arrow,
                    originX = originX,
                    originY = originY,
                    cellSize = cellSize,
                    isEscaping = isEscaping,
                    isBlocked = isBlocked,
                    isHinted = isHinted,
                    hintPulse = hintPulse
                )
            }
        }
    }
}

/**
 * Draws the faint circular dots at grid intersections.
 */
private fun DrawScope.drawGridDots(
    gridWidth: Int,
    gridHeight: Int,
    originX: Float,
    originY: Float,
    cellSize: Float
) {
    val dotColor = Color(0xFFCBD5E1)
    val dotRadius = 3.dp.toPx()

    for (x in 0 until gridWidth) {
        for (y in 0 until gridHeight) {
            val px = originX + x * cellSize
            val py = originY + y * cellSize
            drawCircle(
                color = dotColor,
                radius = dotRadius,
                center = Offset(px, py)
            )
        }
    }
}

/**
 * Draws a single arrow polyline path and its directional arrowhead.
 */
private fun DrawScope.drawSingleArrow(
    arrow: Arrow,
    originX: Float,
    originY: Float,
    cellSize: Float,
    isEscaping: Boolean,
    isBlocked: Boolean,
    isHinted: Boolean,
    hintPulse: Float
) {
    val strokeWidth = cellSize * 0.17f
    val baseColor = when {
        isEscaping -> SkyBlue600
        isBlocked -> HeartRed
        isHinted -> GoldAmber
        else -> Slate800
    }

    // Offset adjustments for animations
    var animOffsetX = 0f
    var animOffsetY = 0f
    var alpha = 1f

    if (isEscaping) {
        // Slide out along direction
        val escapeDistance = cellSize * 3.5f
        animOffsetX = arrow.direction.dx * escapeDistance
        animOffsetY = arrow.direction.dy * escapeDistance
        alpha = 0.85f
    } else if (isBlocked) {
        // Shake back and forth along perpendicular axis
        val shakeAmount = cellSize * 0.14f
        animOffsetX = -arrow.direction.dy * shakeAmount
        animOffsetY = arrow.direction.dx * shakeAmount
    }

    // If hinted, draw a radiant pulsing glow behind the arrow
    if (isHinted) {
        val glowPath = Path()
        arrow.points.forEachIndexed { index, pt ->
            val px = originX + pt.x * cellSize + animOffsetX
            val py = originY + pt.y * cellSize + animOffsetY
            if (index == 0) glowPath.moveTo(px, py) else glowPath.lineTo(px, py)
        }
        drawPath(
            path = glowPath,
            color = GoldAmber.copy(alpha = 0.35f),
            style = Stroke(
                width = strokeWidth * 2.2f * hintPulse,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }

    // Main arrow stroke
    val path = Path()
    val pixelPoints = arrow.points.map { pt ->
        Offset(
            originX + pt.x * cellSize + animOffsetX,
            originY + pt.y * cellSize + animOffsetY
        )
    }

    pixelPoints.forEachIndexed { index, pt ->
        if (index == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
    }

    drawPath(
        path = path,
        color = baseColor.copy(alpha = alpha),
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // Draw the arrowhead at the tip
    val tip = pixelPoints.last()
    drawArrowHead(
        tip = tip,
        direction = arrow.direction,
        cellSize = cellSize,
        color = baseColor.copy(alpha = alpha)
    )
}

/**
 * Draws a sharp, clean triangular arrowhead at [tip] pointing in [direction].
 */
private fun DrawScope.drawArrowHead(
    tip: Offset,
    direction: Direction,
    cellSize: Float,
    color: Color
) {
    val headLength = cellSize * 0.36f
    val headHalfWidth = cellSize * 0.22f

    val dirX = direction.dx.toFloat()
    val dirY = direction.dy.toFloat()
    val perpX = -dirY
    val perpY = dirX

    // Base center of arrowhead
    val baseCenterX = tip.x - dirX * headLength
    val baseCenterY = tip.y - dirY * headLength

    // Left and right corners of the triangle base
    val leftCorner = Offset(
        baseCenterX + perpX * headHalfWidth,
        baseCenterY + perpY * headHalfWidth
    )
    val rightCorner = Offset(
        baseCenterX - perpX * headHalfWidth,
        baseCenterY - perpY * headHalfWidth
    )

    val headPath = Path().apply {
        moveTo(tip.x + dirX * (cellSize * 0.04f), tip.y + dirY * (cellSize * 0.04f))
        lineTo(leftCorner.x, leftCorner.y)
        lineTo(baseCenterX + dirX * (headLength * 0.18f), baseCenterY + dirY * (headLength * 0.18f))
        lineTo(rightCorner.x, rightCorner.y)
        close()
    }

    drawPath(path = headPath, color = color, style = Fill)
}

/**
 * Calculates the shortest distance from tap point [p] to any segment of [arrow].
 */
private fun calculateDistanceToArrow(
    p: Offset,
    arrow: Arrow,
    originX: Float,
    originY: Float,
    cellSize: Float
): Float {
    var minDistance = Float.MAX_VALUE
    val pixelPoints = arrow.points.map { pt ->
        Offset(originX + pt.x * cellSize, originY + pt.y * cellSize)
    }

    for (i in 0 until pixelPoints.size - 1) {
        val a = pixelPoints[i]
        val b = pixelPoints[i + 1]
        val dist = distanceToSegment(p, a, b)
        if (dist < minDistance) {
            minDistance = dist
        }
    }
    return minDistance
}

private fun distanceToSegment(p: Offset, a: Offset, b: Offset): Float {
    val dx = b.x - a.x
    val dy = b.y - a.y
    val lengthSq = dx * dx + dy * dy
    if (lengthSq == 0f) {
        val px = p.x - a.x
        val py = p.y - a.y
        return sqrt(px * px + py * py)
    }
    var t = ((p.x - a.x) * dx + (p.y - a.y) * dy) / lengthSq
    t = max(0f, min(1f, t))
    val projX = a.x + t * dx
    val projY = a.y + t * dy
    val diffX = p.x - projX
    val diffY = p.y - projY
    return sqrt(diffX * diffX + diffY * diffY)
}
