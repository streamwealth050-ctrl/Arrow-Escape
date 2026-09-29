package com.example.domain.logic

import com.example.domain.model.Arrow
import com.example.domain.model.Direction
import com.example.domain.model.GridPoint

object ObstructionChecker {

    /**
     * Checks if the path in front of [arrow] is clear to the edge of the board.
     * An arrow is free if no other arrow occupies any point on the ray starting
     * from arrow.tip + arrow.direction up to the board boundaries.
     */
    fun isArrowFree(
        arrow: Arrow,
        activeArrows: List<Arrow>,
        gridWidth: Int,
        gridHeight: Int
    ): Boolean {
        return findFirstBlockingArrow(arrow, activeArrows, gridWidth, gridHeight) == null
    }

    /**
     * Finds the first active arrow that obstructs [arrow]'s escape path, if any.
     */
    fun findFirstBlockingArrow(
        arrow: Arrow,
        activeArrows: List<Arrow>,
        gridWidth: Int,
        gridHeight: Int
    ): Arrow? {
        val dir = arrow.direction
        var cur = arrow.tip + dir

        val otherArrows = activeArrows.filter { it.id != arrow.id }
        if (otherArrows.isEmpty()) return null

        while (isInsideBoard(cur, gridWidth, gridHeight)) {
            // Check if any other arrow occupies this point
            for (other in otherArrows) {
                if (other.coveredGridPoints.contains(cur)) {
                    return other
                }
            }
            cur += dir
        }

        return null
    }

    /**
     * Returns all arrows from [activeArrows] that currently have a clear escape path.
     */
    fun getFreeArrows(
        activeArrows: List<Arrow>,
        gridWidth: Int,
        gridHeight: Int
    ): List<Arrow> {
        return activeArrows.filter { arrow ->
            isArrowFree(arrow, activeArrows, gridWidth, gridHeight)
        }
    }

    private fun isInsideBoard(point: GridPoint, width: Int, height: Int): Boolean {
        return point.x in 0 until width && point.y in 0 until height
    }
}
