package com.example.domain.logic

import com.example.domain.model.Arrow
import com.example.domain.model.LevelData
import com.example.domain.model.ShapeType

object LevelGenerator {

    fun getLevel(levelNumber: Int): LevelData {
        return DenseMazeGenerator.getLevel(levelNumber)
    }

    fun getDailyChallenge(day: Int = com.example.util.DateHelper.getTodayDayOfMonth()): LevelData {
        return DenseMazeGenerator.getDailyChallenge(day)
    }

    data class ComplexityMetrics(
        val isSolvable: Boolean,
        val initialFreeCount: Int,
        val waveLayers: Int,
        val totalArrows: Int
    )

    fun calculateComplexity(
        arrows: List<Arrow>,
        width: Int,
        height: Int,
        shapeType: ShapeType = ShapeType.RECTANGLE
    ): ComplexityMetrics {
        if (arrows.isEmpty()) return ComplexityMetrics(false, 0, 0, 0)
        var remaining = arrows.toList()
        var waves = 0
        var initialFreeCount = 0

        while (remaining.isNotEmpty()) {
            val free = ObstructionChecker.getFreeArrows(remaining, width, height, shapeType)
            if (free.isEmpty()) {
                return ComplexityMetrics(false, initialFreeCount, waves, arrows.size)
            }
            if (waves == 0) {
                initialFreeCount = free.size
            }
            waves++
            val freeIds = free.map { it.id }.toSet()
            remaining = remaining.filter { !freeIds.contains(it.id) }
        }
        return ComplexityMetrics(true, initialFreeCount, waves, arrows.size)
    }

    fun isFullySolvable(arrows: List<Arrow>, width: Int, height: Int, shapeType: ShapeType = ShapeType.RECTANGLE): Boolean {
        return calculateComplexity(arrows, width, height, shapeType).isSolvable
    }
}
