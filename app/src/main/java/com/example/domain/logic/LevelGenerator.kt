package com.example.domain.logic

import com.example.domain.model.Arrow
import com.example.domain.model.Difficulty
import com.example.domain.model.Direction
import com.example.domain.model.GridPoint
import com.example.domain.model.LevelData
import kotlin.random.Random

object LevelGenerator {

    fun getLevel(levelNumber: Int): LevelData {
        return when (levelNumber) {
            1 -> getLevel1()
            2 -> getLevel2()
            3 -> getLevel3()
            4 -> getLevel4()
            5 -> getLevel5()
            6 -> getLevel6()
            7 -> getLevel7()
            8 -> getLevel8()
            9 -> getLevel9()
            10 -> getLevel10()
            else -> generateProceduralLevel(levelNumber)
        }
    }

    // Level 1: "The Opener" (5x5) - 5 arrows with immediate cross-dependencies
    private fun getLevel1(): LevelData {
        val arrows = listOf(
            Arrow(1, listOf(GridPoint(0, 1), GridPoint(0, 0))), // UP (free)
            Arrow(2, listOf(GridPoint(2, 0), GridPoint(0, 0))), // LEFT (blocked by 1)
            Arrow(3, listOf(GridPoint(2, 3), GridPoint(2, 0))), // UP (blocked by 2)
            Arrow(4, listOf(GridPoint(4, 3), GridPoint(4, 4))), // DOWN (free)
            Arrow(5, listOf(GridPoint(2, 4), GridPoint(4, 4)))  // RIGHT (blocked by 4)
        )
        return LevelData(1, 5, 5, arrows, timeLimitSeconds = 70, Difficulty.EASY, "Level 1: The Opener")
    }

    // Level 2: "The Interlock" (5x5) - 7 arrows, pinwheel corners
    private fun getLevel2(): LevelData {
        val arrows = listOf(
            Arrow(1, listOf(GridPoint(4, 1), GridPoint(4, 0))), // UP (free)
            Arrow(2, listOf(GridPoint(1, 0), GridPoint(4, 0))), // RIGHT (blocked by 1)
            Arrow(3, listOf(GridPoint(1, 4), GridPoint(1, 0))), // UP (blocked by 2)
            Arrow(4, listOf(GridPoint(3, 4), GridPoint(1, 4))), // LEFT (blocked by 3)
            Arrow(5, listOf(GridPoint(3, 1), GridPoint(3, 4))), // DOWN (blocked by 4)
            Arrow(6, listOf(GridPoint(0, 2), GridPoint(0, 4))), // DOWN (free)
            Arrow(7, listOf(GridPoint(0, 2), GridPoint(4, 2)))  // RIGHT (blocked by 5 and 1)
        )
        return LevelData(2, 5, 5, arrows, timeLimitSeconds = 65, Difficulty.EASY, "Level 2: The Interlock")
    }

    // Level 3: "Spiral Trap" (6x6) - 9 arrows with 90° bends
    private fun getLevel3(): LevelData {
        val arrows = listOf(
            Arrow(1, listOf(GridPoint(1, 1), GridPoint(1, 0))), // UP (free)
            Arrow(2, listOf(GridPoint(4, 0), GridPoint(1, 0))), // LEFT (blocked by 1)
            Arrow(3, listOf(GridPoint(5, 4), GridPoint(5, 5))), // DOWN (free)
            Arrow(4, listOf(GridPoint(2, 5), GridPoint(5, 5))), // RIGHT (blocked by 3)
            Arrow(5, listOf(GridPoint(2, 2), GridPoint(2, 5))), // DOWN (blocked by 4)
            Arrow(6, listOf(GridPoint(4, 2), GridPoint(2, 2))), // LEFT (blocked by 5)
            Arrow(7, listOf(GridPoint(4, 4), GridPoint(4, 0))), // UP (blocked by 2 and 6)
            Arrow(8, listOf(GridPoint(0, 3), GridPoint(0, 0))), // UP (free)
            Arrow(9, listOf(GridPoint(3, 3), GridPoint(0, 3)))  // LEFT (blocked by 8)
        )
        return LevelData(3, 6, 6, arrows, timeLimitSeconds = 60, Difficulty.EASY, "Level 3: Spiral Trap")
    }

    // Level 4: "Curved Labyrinth" (6x6) - 10 arrows with 90° curved paths
    private fun getLevel4(): LevelData {
        val arrows = listOf(
            Arrow(1, listOf(GridPoint(0, 3), GridPoint(0, 0))), // UP (free)
            Arrow(2, listOf(GridPoint(5, 2), GridPoint(5, 5))), // DOWN (free)
            Arrow(3, listOf(GridPoint(2, 1), GridPoint(0, 1))), // LEFT (blocked by 1)
            Arrow(4, listOf(GridPoint(3, 4), GridPoint(5, 4))), // RIGHT (blocked by 2)
            // L-shape arrow
            Arrow(5, listOf(GridPoint(2, 3), GridPoint(2, 1))), // UP (blocked by 3)
            Arrow(6, listOf(GridPoint(3, 2), GridPoint(3, 4))), // DOWN (blocked by 4)
            Arrow(7, listOf(GridPoint(1, 4), GridPoint(1, 0), GridPoint(0, 0))), // Hook LEFT (free)
            Arrow(8, listOf(GridPoint(4, 1), GridPoint(4, 5), GridPoint(5, 5))), // Hook RIGHT (free)
            Arrow(9, listOf(GridPoint(1, 2), GridPoint(3, 2))), // RIGHT (blocked by 6)
            Arrow(10, listOf(GridPoint(4, 3), GridPoint(2, 3))) // LEFT (blocked by 5)
        )
        return LevelData(4, 6, 6, arrows, timeLimitSeconds = 60, Difficulty.EASY, "Level 4: Curved Labyrinth")
    }

    // Level 5: "Crossweave" (6x6) - 12 arrows, multi-branching dependencies
    private fun getLevel5(): LevelData {
        val arrows = listOf(
            Arrow(1, listOf(GridPoint(5, 1), GridPoint(5, 0))), // UP (free)
            Arrow(2, listOf(GridPoint(0, 4), GridPoint(0, 5))), // DOWN (free)
            Arrow(3, listOf(GridPoint(2, 0), GridPoint(5, 0))), // RIGHT (blocked by 1)
            Arrow(4, listOf(GridPoint(3, 5), GridPoint(0, 5))), // LEFT (blocked by 2)
            Arrow(5, listOf(GridPoint(2, 3), GridPoint(2, 0))), // UP (blocked by 3)
            Arrow(6, listOf(GridPoint(3, 2), GridPoint(3, 5))), // DOWN (blocked by 4)
            Arrow(7, listOf(GridPoint(1, 2), GridPoint(1, 0))), // UP (free)
            Arrow(8, listOf(GridPoint(4, 3), GridPoint(4, 5))), // DOWN (free)
            Arrow(9, listOf(GridPoint(4, 2), GridPoint(1, 2))), // LEFT (blocked by 7)
            Arrow(10, listOf(GridPoint(1, 3), GridPoint(4, 3))), // RIGHT (blocked by 8)
            Arrow(11, listOf(GridPoint(0, 1), GridPoint(0, 0))), // UP (free)
            Arrow(12, listOf(GridPoint(5, 4), GridPoint(5, 5)))  // DOWN (free)
        )
        return LevelData(5, 6, 6, arrows, timeLimitSeconds = 55, Difficulty.EASY, "Level 5: Crossweave")
    }

    // Level 6: "The Pinch" (7x7) - 14 arrows, tight choke points
    private fun getLevel6(): LevelData {
        val arrows = listOf(
            Arrow(1, listOf(GridPoint(0, 2), GridPoint(0, 0))), // UP (free)
            Arrow(2, listOf(GridPoint(6, 4), GridPoint(6, 6))), // DOWN (free)
            Arrow(3, listOf(GridPoint(3, 0), GridPoint(0, 0))), // LEFT (blocked by 1)
            Arrow(4, listOf(GridPoint(3, 6), GridPoint(6, 6))), // RIGHT (blocked by 2)
            Arrow(5, listOf(GridPoint(3, 3), GridPoint(3, 0))), // UP (blocked by 3)
            Arrow(6, listOf(GridPoint(3, 3), GridPoint(3, 6))), // DOWN (blocked by 4)
            Arrow(7, listOf(GridPoint(1, 4), GridPoint(1, 0))), // UP (free)
            Arrow(8, listOf(GridPoint(5, 2), GridPoint(5, 6))), // DOWN (free)
            Arrow(9, listOf(GridPoint(5, 3), GridPoint(1, 3))), // LEFT (blocked by 7)
            Arrow(10, listOf(GridPoint(1, 3), GridPoint(5, 3))), // RIGHT (blocked by 8)
            Arrow(11, listOf(GridPoint(2, 5), GridPoint(0, 5))), // LEFT (free)
            Arrow(12, listOf(GridPoint(4, 1), GridPoint(6, 1))), // RIGHT (free)
            Arrow(13, listOf(GridPoint(2, 6), GridPoint(2, 5))), // UP (blocked by 11)
            Arrow(14, listOf(GridPoint(4, 0), GridPoint(4, 1)))  // DOWN (blocked by 12)
        )
        return LevelData(6, 7, 7, arrows, timeLimitSeconds = 55, Difficulty.EASY, "Level 6: The Pinch")
    }

    // Level 7: "Double Helix" (7x7) - 16 arrows
    private fun getLevel7(): LevelData {
        val arrows = listOf(
            Arrow(1, listOf(GridPoint(1, 1), GridPoint(1, 0))), // UP (free)
            Arrow(2, listOf(GridPoint(5, 5), GridPoint(5, 6))), // DOWN (free)
            Arrow(3, listOf(GridPoint(3, 0), GridPoint(1, 0))), // LEFT (blocked by 1)
            Arrow(4, listOf(GridPoint(3, 6), GridPoint(5, 6))), // RIGHT (blocked by 2)
            Arrow(5, listOf(GridPoint(3, 2), GridPoint(3, 0))), // UP (blocked by 3)
            Arrow(6, listOf(GridPoint(3, 4), GridPoint(3, 6))), // DOWN (blocked by 4)
            Arrow(7, listOf(GridPoint(0, 3), GridPoint(0, 0))), // UP (free)
            Arrow(8, listOf(GridPoint(6, 3), GridPoint(6, 6))), // DOWN (free)
            Arrow(9, listOf(GridPoint(2, 3), GridPoint(0, 3))), // LEFT (blocked by 7)
            Arrow(10, listOf(GridPoint(4, 3), GridPoint(6, 3))), // RIGHT (blocked by 8)
            Arrow(11, listOf(GridPoint(2, 5), GridPoint(2, 3))), // UP (blocked by 9)
            Arrow(12, listOf(GridPoint(4, 1), GridPoint(4, 3))), // DOWN (blocked by 10)
            Arrow(13, listOf(GridPoint(2, 1), GridPoint(0, 1))), // LEFT (free)
            Arrow(14, listOf(GridPoint(4, 5), GridPoint(6, 5))), // RIGHT (free)
            Arrow(15, listOf(GridPoint(0, 5), GridPoint(0, 6))), // DOWN (free)
            Arrow(16, listOf(GridPoint(6, 1), GridPoint(6, 0)))  // UP (free)
        )
        return LevelData(7, 7, 7, arrows, timeLimitSeconds = 50, Difficulty.EASY, "Level 7: Double Helix")
    }

    // Level 8: "The Fortress" (7x7) - 18 arrows, dense interlocking citadel
    private fun getLevel8(): LevelData {
        val arrows = listOf(
            // Outer four gatekeepers
            Arrow(1, listOf(GridPoint(0, 1), GridPoint(0, 0))), // UP (free)
            Arrow(2, listOf(GridPoint(6, 5), GridPoint(6, 6))), // DOWN (free)
            Arrow(3, listOf(GridPoint(5, 0), GridPoint(6, 0))), // RIGHT (free)
            Arrow(4, listOf(GridPoint(1, 6), GridPoint(0, 6))), // LEFT (free)
            // Second perimeter
            Arrow(5, listOf(GridPoint(2, 0), GridPoint(0, 0))), // LEFT (blocked by 1)
            Arrow(6, listOf(GridPoint(4, 6), GridPoint(6, 6))), // RIGHT (blocked by 2)
            Arrow(7, listOf(GridPoint(6, 2), GridPoint(6, 0))), // UP (blocked by 3)
            Arrow(8, listOf(GridPoint(0, 4), GridPoint(0, 6))), // DOWN (blocked by 4)
            // Inner core
            Arrow(9, listOf(GridPoint(2, 2), GridPoint(2, 0))), // UP (blocked by 5)
            Arrow(10, listOf(GridPoint(4, 4), GridPoint(4, 6))), // DOWN (blocked by 6)
            Arrow(11, listOf(GridPoint(4, 2), GridPoint(6, 2))), // RIGHT (blocked by 7)
            Arrow(12, listOf(GridPoint(2, 4), GridPoint(0, 4))), // LEFT (blocked by 8)
            // Center cross
            Arrow(13, listOf(GridPoint(3, 3), GridPoint(2, 3), GridPoint(2, 2))), // UP (blocked by 9)
            Arrow(14, listOf(GridPoint(3, 3), GridPoint(4, 3), GridPoint(4, 4))), // DOWN (blocked by 10)
            Arrow(15, listOf(GridPoint(1, 3), GridPoint(0, 3))), // LEFT (free)
            Arrow(16, listOf(GridPoint(5, 3), GridPoint(6, 3))), // RIGHT (free)
            Arrow(17, listOf(GridPoint(3, 1), GridPoint(3, 0))), // UP (free)
            Arrow(18, listOf(GridPoint(3, 5), GridPoint(3, 6)))  // DOWN (free)
        )
        return LevelData(8, 7, 7, arrows, timeLimitSeconds = 50, Difficulty.EASY, "Level 8: The Fortress")
    }

    // Level 9: "Heart of Arrows" (8x8) - 20 arrows, complex heart silhouette
    private fun getLevel9(): LevelData {
        val arrows = listOf(
            Arrow(1, listOf(GridPoint(0, 2), GridPoint(0, 0))), // UP (free)
            Arrow(2, listOf(GridPoint(7, 5), GridPoint(7, 7))), // DOWN (free)
            Arrow(3, listOf(GridPoint(2, 0), GridPoint(0, 0))), // LEFT (blocked by 1)
            Arrow(4, listOf(GridPoint(5, 7), GridPoint(7, 7))), // RIGHT (blocked by 2)
            Arrow(5, listOf(GridPoint(2, 3), GridPoint(2, 0))), // UP (blocked by 3)
            Arrow(6, listOf(GridPoint(5, 4), GridPoint(5, 7))), // DOWN (blocked by 4)
            Arrow(7, listOf(GridPoint(4, 3), GridPoint(2, 3))), // LEFT (blocked by 5)
            Arrow(8, listOf(GridPoint(3, 4), GridPoint(5, 4))), // RIGHT (blocked by 6)
            Arrow(9, listOf(GridPoint(3, 2), GridPoint(3, 0))), // UP (free)
            Arrow(10, listOf(GridPoint(4, 5), GridPoint(4, 7))), // DOWN (free)
            Arrow(11, listOf(GridPoint(1, 5), GridPoint(0, 5))), // LEFT (free)
            Arrow(12, listOf(GridPoint(6, 2), GridPoint(7, 2))), // RIGHT (free)
            Arrow(13, listOf(GridPoint(1, 7), GridPoint(1, 5))), // UP (blocked by 11)
            Arrow(14, listOf(GridPoint(6, 0), GridPoint(6, 2))), // DOWN (blocked by 12)
            Arrow(15, listOf(GridPoint(3, 6), GridPoint(0, 6))), // LEFT (free)
            Arrow(16, listOf(GridPoint(4, 1), GridPoint(7, 1))), // RIGHT (free)
            Arrow(17, listOf(GridPoint(3, 7), GridPoint(3, 6))), // UP (blocked by 15)
            Arrow(18, listOf(GridPoint(4, 0), GridPoint(4, 1))), // DOWN (blocked by 16)
            Arrow(19, listOf(GridPoint(0, 3), GridPoint(0, 4))), // DOWN (free)
            Arrow(20, listOf(GridPoint(7, 4), GridPoint(7, 3)))  // UP (free)
        )
        return LevelData(9, 8, 8, arrows, timeLimitSeconds = 48, Difficulty.EASY, "Level 9: Heart of Arrows")
    }

    // Level 10: "Grand Labyrinth" (8x8) - 24 arrows, ultimate brain teaser
    private fun getLevel10(): LevelData {
        val arrows = listOf(
            Arrow(1, listOf(GridPoint(1, 1), GridPoint(1, 0))), // UP (free)
            Arrow(2, listOf(GridPoint(6, 6), GridPoint(6, 7))), // DOWN (free)
            Arrow(3, listOf(GridPoint(4, 0), GridPoint(1, 0))), // LEFT (blocked by 1)
            Arrow(4, listOf(GridPoint(3, 7), GridPoint(6, 7))), // RIGHT (blocked by 2)
            Arrow(5, listOf(GridPoint(4, 2), GridPoint(4, 0))), // UP (blocked by 3)
            Arrow(6, listOf(GridPoint(3, 5), GridPoint(3, 7))), // DOWN (blocked by 4)
            Arrow(7, listOf(GridPoint(6, 2), GridPoint(4, 2))), // LEFT (blocked by 5)
            Arrow(8, listOf(GridPoint(1, 5), GridPoint(3, 5))), // RIGHT (blocked by 6)
            Arrow(9, listOf(GridPoint(6, 4), GridPoint(6, 2))), // UP (blocked by 7)
            Arrow(10, listOf(GridPoint(1, 3), GridPoint(1, 5))), // DOWN (blocked by 8)
            Arrow(11, listOf(GridPoint(3, 4), GridPoint(6, 4))), // RIGHT (blocked by 9)
            Arrow(12, listOf(GridPoint(4, 3), GridPoint(1, 3))), // LEFT (blocked by 10)
            Arrow(13, listOf(GridPoint(3, 2), GridPoint(3, 0))), // UP (free)
            Arrow(14, listOf(GridPoint(4, 5), GridPoint(4, 7))), // DOWN (free)
            Arrow(15, listOf(GridPoint(0, 2), GridPoint(0, 0))), // UP (free)
            Arrow(16, listOf(GridPoint(7, 5), GridPoint(7, 7))), // DOWN (free)
            Arrow(17, listOf(GridPoint(0, 4), GridPoint(0, 7))), // DOWN (free)
            Arrow(18, listOf(GridPoint(7, 3), GridPoint(7, 0))), // UP (free)
            Arrow(19, listOf(GridPoint(2, 6), GridPoint(0, 6))), // LEFT (free)
            Arrow(20, listOf(GridPoint(5, 1), GridPoint(7, 1))), // RIGHT (free)
            Arrow(21, listOf(GridPoint(2, 7), GridPoint(2, 6))), // UP (blocked by 19)
            Arrow(22, listOf(GridPoint(5, 0), GridPoint(5, 1))), // DOWN (blocked by 20)
            Arrow(23, listOf(GridPoint(2, 1), GridPoint(0, 1))), // LEFT (free)
            Arrow(24, listOf(GridPoint(5, 6), GridPoint(7, 6)))  // RIGHT (free)
        )
        return LevelData(10, 8, 8, arrows, timeLimitSeconds = 45, Difficulty.EASY, "Level 10: Grand Labyrinth")
    }

    /**
     * Solvable Procedural Level Generation for Level 11+
     */
    fun generateProceduralLevel(levelNumber: Int): LevelData {
        val difficulty = Difficulty.forLevel(levelNumber)
        val (gridSize, targetArrows) = when (difficulty) {
            Difficulty.EASY -> Pair(5, 7)
            Difficulty.NORMAL -> Pair(6, 11 + (levelNumber - 11).coerceAtMost(5))
            Difficulty.HARD -> Pair(7, 15 + (levelNumber - 26).coerceAtMost(6))
            Difficulty.VERY_HARD -> Pair(8, 19 + (levelNumber - 51).coerceAtMost(6))
            Difficulty.EXPERT -> Pair(8, 23 + (levelNumber - 101).coerceAtMost(8))
        }

        val rng = Random(levelNumber * 10007L + 42)
        var arrows = generateReverseSolvableLevel(gridSize, targetArrows, rng)
        val timeLimit = difficulty.timeLimitSec + (targetArrows * 1.5f).toInt().coerceAtMost(30)

        return LevelData(
            levelNumber = levelNumber,
            gridWidth = gridSize,
            gridHeight = gridSize,
            arrows = arrows,
            timeLimitSeconds = timeLimit,
            difficulty = difficulty,
            title = "Level $levelNumber"
        )
    }

    private fun generateReverseSolvableLevel(
        gridSize: Int,
        count: Int,
        rng: Random
    ): List<Arrow> {
        val result = mutableListOf<Arrow>()
        val occupiedPoints = mutableSetOf<GridPoint>()

        var idCounter = 1
        var attempts = 0
        while (result.size < count && attempts < 350) {
            attempts++
            val dir = Direction.values()[rng.nextInt(4)]
            val length = rng.nextInt(1, 4)

            val tipCandidates = mutableListOf<GridPoint>()
            for (x in 0 until gridSize) {
                for (y in 0 until gridSize) {
                    val p = GridPoint(x, y)
                    if (!occupiedPoints.contains(p)) {
                        var cur = p + dir
                        var clear = true
                        while (cur.x in 0 until gridSize && cur.y in 0 until gridSize) {
                            if (occupiedPoints.contains(cur)) {
                                clear = false
                                break
                            }
                            cur += dir
                        }
                        if (clear) {
                            tipCandidates.add(p)
                        }
                    }
                }
            }

            if (tipCandidates.isEmpty()) continue
            val tip = tipCandidates[rng.nextInt(tipCandidates.size)]

            val backDir = when (dir) {
                Direction.UP -> Direction.DOWN
                Direction.DOWN -> Direction.UP
                Direction.LEFT -> Direction.RIGHT
                Direction.RIGHT -> Direction.LEFT
            }

            val makeTurn = rng.nextBoolean() && length >= 2
            val points = mutableListOf<GridPoint>()
            var valid = true

            if (!makeTurn) {
                val tail = tip + GridPoint(backDir.dx * length, backDir.dy * length)
                if (tail.x in 0 until gridSize && tail.y in 0 until gridSize) {
                    val minX = minOf(tail.x, tip.x)
                    val maxX = maxOf(tail.x, tip.x)
                    val minY = minOf(tail.y, tip.y)
                    val maxY = maxOf(tail.y, tip.y)
                    for (x in minX..maxX) {
                        for (y in minY..maxY) {
                            if (occupiedPoints.contains(GridPoint(x, y))) {
                                valid = false
                            }
                        }
                    }
                    if (valid) {
                        points.add(tail)
                        points.add(tip)
                    }
                } else {
                    valid = false
                }
            } else {
                val bendLen = 1
                val bend = tip + GridPoint(backDir.dx * bendLen, backDir.dy * bendLen)
                val perpDir = if (dir == Direction.UP || dir == Direction.DOWN) {
                    if (rng.nextBoolean()) Direction.LEFT else Direction.RIGHT
                } else {
                    if (rng.nextBoolean()) Direction.UP else Direction.DOWN
                }
                val tail = bend + GridPoint(perpDir.dx * 1, perpDir.dy * 1)
                if (bend.x in 0 until gridSize && bend.y in 0 until gridSize &&
                    tail.x in 0 until gridSize && tail.y in 0 until gridSize
                ) {
                    val candidatePoints = listOf(tail, bend, tip)
                    val arrow = Arrow(idCounter, candidatePoints)
                    if (arrow.coveredGridPoints.none { occupiedPoints.contains(it) }) {
                        points.addAll(candidatePoints)
                    } else {
                        valid = false
                    }
                } else {
                    valid = false
                }
            }

            if (valid && points.size >= 2) {
                val newArrow = Arrow(idCounter++, points)
                result.add(newArrow)
                occupiedPoints.addAll(newArrow.coveredGridPoints)
            }
        }

        return result
    }

    fun isFullySolvable(arrows: List<Arrow>, width: Int, height: Int): Boolean {
        var remaining = arrows.toList()
        while (remaining.isNotEmpty()) {
            val free = ObstructionChecker.getFreeArrows(remaining, width, height)
            if (free.isEmpty()) {
                return false
            }
            remaining = remaining.filter { it.id != free.first().id }
        }
        return true
    }
}
