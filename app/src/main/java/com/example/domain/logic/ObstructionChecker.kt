package com.example.domain.logic

import com.example.domain.model.Arrow
import com.example.domain.model.Direction
import com.example.domain.model.GridPoint
import com.example.domain.model.ShapeType

object ObstructionChecker {

    /**
     * Checks if the path in front of [arrow] is clear to the edge of the board or shape boundary.
     */
    fun isArrowFree(
        arrow: Arrow,
        activeArrows: List<Arrow>,
        gridWidth: Int,
        gridHeight: Int,
        shapeType: ShapeType = ShapeType.RECTANGLE
    ): Boolean {
        return findFirstBlockingArrow(arrow, activeArrows, gridWidth, gridHeight, shapeType) == null
    }

    /**
     * Finds the first active arrow that obstructs [arrow]'s escape path, if any.
     */
    fun findFirstBlockingArrow(
        arrow: Arrow,
        activeArrows: List<Arrow>,
        gridWidth: Int,
        gridHeight: Int,
        shapeType: ShapeType = ShapeType.RECTANGLE
    ): Arrow? {
        val dir = arrow.direction
        var cur = arrow.tip + dir

        val otherArrows = activeArrows.filter { it.id != arrow.id }
        if (otherArrows.isEmpty()) return null

        while (isInsideBoard(cur, gridWidth, gridHeight)) {
            if (arrow.coveredGridPoints.contains(cur)) {
                return arrow
            }
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
        gridHeight: Int,
        shapeType: ShapeType = ShapeType.RECTANGLE
    ): List<Arrow> {
        return activeArrows.filter { arrow ->
            isArrowFree(arrow, activeArrows, gridWidth, gridHeight, shapeType)
        }
    }

    private fun isInsideBoard(point: GridPoint, width: Int, height: Int): Boolean {
        return point.x in 0 until width && point.y in 0 until height
    }
}
