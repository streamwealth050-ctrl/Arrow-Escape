package com.example

import com.example.domain.logic.DenseMazeGenerator
import com.example.domain.logic.LevelGenerator
import com.example.domain.logic.ObstructionChecker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * High-speed pure JVM unit tests for Level Generator, Solvability, and Obstruction logic.
 */
class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun `verify level 1 to 10 are all fully solvable`() {
        for (lvl in 1..10) {
            val level = LevelGenerator.getLevel(lvl)
            val free = ObstructionChecker.getFreeArrows(level.arrows, level.gridWidth, level.gridHeight, level.shapeType)
            println("DEBUG LEVEL $lvl: arrows=${level.arrows.size}, free=${free.size}, width=${level.gridWidth}, height=${level.gridHeight}")
            assertTrue(
                "Level $lvl should be fully solvable",
                LevelGenerator.isFullySolvable(level.arrows, level.gridWidth, level.gridHeight, level.shapeType)
            )
            assertTrue("Level $lvl must have at least 1 free arrow to start, had ${free.size}", free.isNotEmpty())
            assertTrue("Level $lvl must have locked arrows, had ${free.size} free of ${level.arrows.size}", free.size < level.arrows.size)
        }
    }

    @Test
    fun `verify multi-bend arrows have between 2 and 10 points and are orthogonal`() {
        for (lvl in 1..10) {
            val level = LevelGenerator.getLevel(lvl)
            for (arrow in level.arrows) {
                assertTrue(
                    "Arrow ${arrow.id} in level $lvl should have 2 to 10 points, had ${arrow.points.size}",
                    arrow.points.size in 2..10
                )
                // Verify each segment is orthogonal
                for (i in 0 until arrow.points.size - 1) {
                    val p1 = arrow.points[i]
                    val p2 = arrow.points[i + 1]
                    val isOrthogonal = (p1.x == p2.x && p1.y != p2.y) || (p1.y == p2.y && p1.x != p2.x)
                    assertTrue("Segment from $p1 to $p2 in arrow ${arrow.id} must be orthogonal", isOrthogonal)
                }
            }
        }
    }

    @Test
    fun `verify daily challenges are solvable`() {
        for (day in 1..5) {
            val daily = DenseMazeGenerator.getDailyChallenge(day)
            assertTrue(
                "Daily challenge $day must be fully solvable",
                LevelGenerator.isFullySolvable(daily.arrows, daily.gridWidth, daily.gridHeight, daily.shapeType)
            )
        }
    }

    @Test
    fun `verify level 5 has advanced benchmark challenge with limited master keys and deep dependencies`() {
        val level5 = LevelGenerator.getLevel(5)
        assertTrue("Level 5 should have at least 15 arrows, had ${level5.arrows.size}", level5.arrows.size >= 15)

        val complexity = LevelGenerator.calculateComplexity(level5.arrows, level5.gridWidth, level5.gridHeight, level5.shapeType)
        assertTrue("Level 5 must be 100% solvable", complexity.isSolvable)
        assertEquals("Level 5 must start with exactly 2 Master Keys", 2, complexity.initialFreeCount)
        assertTrue("Level 5 must require at least 10 wave layers of sequential deductions, had ${complexity.waveLayers}", complexity.waveLayers >= 10)
    }
}
