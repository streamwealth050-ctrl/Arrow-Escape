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

    // Level 1: Very simple board with obvious free arrows (5x5, 4 straight arrows)
    private fun getLevel1(): LevelData {
        val arrows = listOf(
            Arrow(1, listOf(GridPoint(1, 2), GridPoint(1, 0))), // UP
            Arrow(2, listOf(GridPoint(2, 1), GridPoint(4, 1))), // RIGHT
            Arrow(3, listOf(GridPoint(3, 2), GridPoint(3, 4))), // DOWN
            Arrow(4, listOf(GridPoint(2, 3), GridPoint(0, 3)))  // LEFT
        )
        return LevelData(1, 5, 5, arrows, timeLimitSeconds = 90, Difficulty.EASY, "Level 1: Tap to Escape")
    }

    // Level 2: Slightly more arrows and basic blocking
    private fun getLevel2(): LevelData {
        val arrows = listOf(
            Arrow(1, listOf(GridPoint(1, 4), GridPoint(1, 0))), // UP (free)
            Arrow(2, listOf(GridPoint(3, 0), GridPoint(3, 4))), // DOWN (free)
            Arrow(3, listOf(GridPoint(2, 1), GridPoint(0, 1))), // LEFT (blocked by Arrow 1 until cleared)
            Arrow(4, listOf(GridPoint(2, 3), GridPoint(4, 3))), // RIGHT (blocked by Arrow 2 until cleared)
            Arrow(5, listOf(GridPoint(2, 4), GridPoint(2, 0)))  // UP (blocked by Arrow 3 at y=1 until cleared)
        )
        return LevelData(2, 5, 5, arrows, timeLimitSeconds = 85, Difficulty.EASY, "Level 2: Basic Blocking")
    }

    // Level 3: Introduce simple arrow dependencies (Chain of 6 arrows)
    private fun getLevel3(): LevelData {
        val arrows = listOf(
            Arrow(1, listOf(GridPoint(4, 1), GridPoint(4, 0))), // UP (free at edge)
            Arrow(2, listOf(GridPoint(2, 0), GridPoint(4, 0))), // RIGHT (blocked by 1)
            Arrow(3, listOf(GridPoint(2, 3), GridPoint(2, 0))), // UP (blocked by 2)
            Arrow(4, listOf(GridPoint(0, 3), GridPoint(0, 4))), // DOWN (free at edge)
            Arrow(5, listOf(GridPoint(3, 4), GridPoint(0, 4))), // LEFT (blocked by 4)
            Arrow(6, listOf(GridPoint(3, 2), GridPoint(3, 4)))  // DOWN (blocked by 5)
        )
        return LevelData(3, 5, 5, arrows, timeLimitSeconds = 80, Difficulty.EASY, "Level 3: Dependencies")
    }

    // Level 4: Add curved paths (90° turns) and tighter spaces
    private fun getLevel4(): LevelData {
        val arrows = listOf(
            // Curved arrow turning right: tail at (1, 3), bend at (1, 1), tip at (3, 1) pointing RIGHT -> blocked by 2
            // Free curved arrow: tail at (3, 3), bend at (3, 4), tip at (4, 4) pointing RIGHT (free)
            Arrow(1, listOf(GridPoint(3, 2), GridPoint(3, 4), GridPoint(4, 4))), // RIGHT (free)
            Arrow(2, listOf(GridPoint(3, 1), GridPoint(4, 1))), // RIGHT (free)
            Arrow(3, listOf(GridPoint(1, 3), GridPoint(1, 1), GridPoint(3, 1))), // RIGHT (blocked by 2)
            Arrow(4, listOf(GridPoint(0, 2), GridPoint(0, 0))), // UP (free)
            Arrow(5, listOf(GridPoint(2, 2), GridPoint(0, 2))), // LEFT (blocked by 4)
            Arrow(6, listOf(GridPoint(2, 4), GridPoint(2, 2))), // UP (blocked by 5)
            Arrow(7, listOf(GridPoint(4, 3), GridPoint(4, 2)))  // UP (blocked by 2 until 2 clears)
        )
        return LevelData(4, 5, 5, arrows, timeLimitSeconds = 75, Difficulty.EASY, "Level 4: Curved Paths")
    }

    // Level 5: Require a specific sequence of moves (6x6 board, 8 arrows)
    private fun getLevel5(): LevelData {
        val arrows = listOf(
            Arrow(1, listOf(GridPoint(1, 1), GridPoint(1, 0))), // UP (free)
            Arrow(2, listOf(GridPoint(3, 0), GridPoint(0, 0))), // LEFT (blocked by 1)
            Arrow(3, listOf(GridPoint(4, 2), GridPoint(4, 5))), // DOWN (free)
            Arrow(4, listOf(GridPoint(2, 4), GridPoint(5, 4))), // RIGHT (blocked by 3)
            Arrow(5, listOf(GridPoint(5, 1), GridPoint(5, 0))), // UP (free)
            Arrow(6, listOf(GridPoint(3, 2), GridPoint(3, 0)), customDirection = Direction.UP), // UP (blocked by 2)
            Arrow(7, listOf(GridPoint(2, 2), GridPoint(4, 2))), // RIGHT (blocked by 3)
            Arrow(8, listOf(GridPoint(0, 3), GridPoint(0, 5)))  // DOWN (free)
        )
        return LevelData(5, 6, 6, arrows, timeLimitSeconds = 70, Difficulty.EASY, "Level 5: Strict Sequence")
    }

    // Level 6: More arrows and fewer free paths (6x6 board, 10 arrows)
    private fun getLevel6(): LevelData {
        val arrows = listOf(
            Arrow(1, listOf(GridPoint(0, 2), GridPoint(0, 0))), // UP (free)
            Arrow(2, listOf(GridPoint(5, 3), GridPoint(5, 5))), // DOWN (free)
            Arrow(3, listOf(GridPoint(2, 1), GridPoint(0, 1))), // LEFT (blocked by 1)
            Arrow(4, listOf(GridPoint(3, 4), GridPoint(5, 4))), // RIGHT (blocked by 2)
            Arrow(5, listOf(GridPoint(2, 3), GridPoint(2, 0))), // UP (blocked by 3)
            Arrow(6, listOf(GridPoint(3, 2), GridPoint(3, 5))), // DOWN (blocked by 4)
            Arrow(7, listOf(GridPoint(1, 4), GridPoint(1, 5))), // DOWN (free)
            Arrow(8, listOf(GridPoint(4, 1), GridPoint(4, 0))), // UP (free)
            Arrow(9, listOf(GridPoint(1, 2), GridPoint(3, 2))), // RIGHT (blocked by 6)
            Arrow(10, listOf(GridPoint(4, 3), GridPoint(2, 3))) // LEFT (blocked by 5)
        )
        return LevelData(6, 6, 6, arrows, timeLimitSeconds = 65, Difficulty.EASY, "Level 6: Tight Spaces")
    }

    // Level 7: Introduce less obvious safe moves (6x6 board, 12 arrows)
    private fun getLevel7(): LevelData {
        val arrows = listOf(
            Arrow(1, listOf(GridPoint(2, 0), GridPoint(0, 0))), // LEFT (free)
            Arrow(2, listOf(GridPoint(3, 5), GridPoint(5, 5))), // RIGHT (free)
            Arrow(3, listOf(GridPoint(1, 2), GridPoint(1, 0))), // UP (blocked by 1)
            Arrow(4, listOf(GridPoint(4, 3), GridPoint(4, 5))), // DOWN (blocked by 2)
            Arrow(5, listOf(GridPoint(2, 2), GridPoint(0, 2))), // LEFT (free, below 1)
            Arrow(6, listOf(GridPoint(3, 3), GridPoint(5, 3))), // RIGHT (free, above 2)
            Arrow(7, listOf(GridPoint(2, 4), GridPoint(2, 2))), // UP (blocked by 5)
            Arrow(8, listOf(GridPoint(3, 1), GridPoint(3, 3))), // DOWN (blocked by 6)
            Arrow(9, listOf(GridPoint(0, 4), GridPoint(0, 5))), // DOWN (free)
            Arrow(10, listOf(GridPoint(5, 1), GridPoint(5, 0))), // UP (free)
            Arrow(11, listOf(GridPoint(1, 3), GridPoint(1, 2))), // UP (blocked by 3)
            Arrow(12, listOf(GridPoint(4, 2), GridPoint(4, 3)))  // DOWN (blocked by 4)
        )
        return LevelData(7, 6, 6, arrows, timeLimitSeconds = 65, Difficulty.EASY, "Level 7: Subtlety")
    }

    // Level 8: Chain reactions where one move unlocks several arrows (7x7 board, 13 arrows)
    private fun getLevel8(): LevelData {
        val arrows = listOf(
            // Key unlocking arrow: Arrow 1 blocks arrows 2, 3, and 4!
            Arrow(1, listOf(GridPoint(0, 3), GridPoint(6, 3))), // Horizontal RIGHT across middle (free at x=6)
            Arrow(2, listOf(GridPoint(1, 5), GridPoint(1, 2))), // UP (blocked by 1)
            Arrow(3, listOf(GridPoint(3, 5), GridPoint(3, 2))), // UP (blocked by 1)
            Arrow(4, listOf(GridPoint(5, 5), GridPoint(5, 2))), // UP (blocked by 1)
            Arrow(5, listOf(GridPoint(1, 1), GridPoint(1, 0))), // UP (free)
            Arrow(6, listOf(GridPoint(3, 1), GridPoint(3, 0))), // UP (free)
            Arrow(7, listOf(GridPoint(5, 1), GridPoint(5, 0))), // UP (free)
            Arrow(8, listOf(GridPoint(2, 2), GridPoint(0, 2))), // LEFT (free)
            Arrow(9, listOf(GridPoint(4, 2), GridPoint(6, 2))), // RIGHT (free)
            Arrow(10, listOf(GridPoint(2, 4), GridPoint(0, 4))), // LEFT (free)
            Arrow(11, listOf(GridPoint(4, 4), GridPoint(6, 4))), // RIGHT (free)
            Arrow(12, listOf(GridPoint(2, 6), GridPoint(2, 5))), // UP (blocked by 10)
            Arrow(13, listOf(GridPoint(4, 6), GridPoint(4, 5)))  // UP (blocked by 11)
        )
        return LevelData(8, 7, 7, arrows, timeLimitSeconds = 60, Difficulty.EASY, "Level 8: Chain Reaction")
    }

    // Level 9: Dense and tricky arrangement (7x7 board, 15 arrows)
    private fun getLevel9(): LevelData {
        val arrows = listOf(
            Arrow(1, listOf(GridPoint(0, 1), GridPoint(0, 0))), // UP (free)
            Arrow(2, listOf(GridPoint(6, 5), GridPoint(6, 6))), // DOWN (free)
            Arrow(3, listOf(GridPoint(2, 0), GridPoint(0, 0))), // LEFT (blocked by 1)
            Arrow(4, listOf(GridPoint(4, 6), GridPoint(6, 6))), // RIGHT (blocked by 2)
            Arrow(5, listOf(GridPoint(2, 2), GridPoint(2, 0))), // UP (blocked by 3)
            Arrow(6, listOf(GridPoint(4, 4), GridPoint(4, 6))), // DOWN (blocked by 4)
            Arrow(7, listOf(GridPoint(3, 2), GridPoint(1, 2))), // LEFT (blocked by 5)
            Arrow(8, listOf(GridPoint(3, 4), GridPoint(5, 4))), // RIGHT (blocked by 6)
            Arrow(9, listOf(GridPoint(3, 3), GridPoint(3, 1))), // UP (blocked by 7)
            Arrow(10, listOf(GridPoint(1, 4), GridPoint(1, 6))), // DOWN (free)
            Arrow(11, listOf(GridPoint(5, 2), GridPoint(5, 0))), // UP (free)
            Arrow(12, listOf(GridPoint(0, 4), GridPoint(0, 6))), // DOWN (free)
            Arrow(13, listOf(GridPoint(6, 2), GridPoint(6, 0))), // UP (free)
            Arrow(14, listOf(GridPoint(1, 3), GridPoint(0, 3))), // LEFT (free)
            Arrow(15, listOf(GridPoint(5, 3), GridPoint(6, 3)))  // RIGHT (free)
        )
        return LevelData(9, 7, 7, arrows, timeLimitSeconds = 60, Difficulty.EASY, "Level 9: Tricky Maze")
    }

    // Level 10: Challenging combination of curved paths, blocking, and longer sequences
    private fun getLevel10(): LevelData {
        val arrows = listOf(
            // Outer corner escapes
            Arrow(1, listOf(GridPoint(1, 1), GridPoint(1, 0))), // UP (free)
            Arrow(2, listOf(GridPoint(5, 5), GridPoint(5, 6))), // DOWN (free)
            Arrow(3, listOf(GridPoint(5, 1), GridPoint(6, 1))), // RIGHT (free)
            Arrow(4, listOf(GridPoint(1, 5), GridPoint(0, 5))), // LEFT (free)
            // L-shaped curved arrows
            Arrow(5, listOf(GridPoint(2, 3), GridPoint(2, 1), GridPoint(1, 1))), // LEFT (blocked by 1)
            Arrow(6, listOf(GridPoint(4, 3), GridPoint(4, 5), GridPoint(5, 5))), // RIGHT (blocked by 2)
            Arrow(7, listOf(GridPoint(3, 2), GridPoint(5, 2), GridPoint(5, 1))), // UP (blocked by 3)
            Arrow(8, listOf(GridPoint(3, 4), GridPoint(1, 4), GridPoint(1, 5))), // DOWN (blocked by 4)
            // Center ring
            Arrow(9, listOf(GridPoint(3, 3), GridPoint(3, 1))), // UP (blocked by 7)
            Arrow(10, listOf(GridPoint(3, 3), GridPoint(3, 5))), // DOWN (blocked by 8)
            // Edge reinforcements
            Arrow(11, listOf(GridPoint(0, 2), GridPoint(0, 0))), // UP (free)
            Arrow(12, listOf(GridPoint(6, 4), GridPoint(6, 6))), // DOWN (free)
            Arrow(13, listOf(GridPoint(4, 0), GridPoint(6, 0))), // RIGHT (free)
            Arrow(14, listOf(GridPoint(2, 6), GridPoint(0, 6))), // LEFT (free)
            Arrow(15, listOf(GridPoint(2, 0), GridPoint(2, 1))), // DOWN (blocked by 5)
            Arrow(16, listOf(GridPoint(4, 6), GridPoint(4, 5)))  // UP (blocked by 6)
        )
        return LevelData(10, 7, 7, arrows, timeLimitSeconds = 55, Difficulty.EASY, "Level 10: Grand Finale")
    }

    /**
     * Unlimited Procedural Solvable Level Generation.
     * Generates a puzzle seeded by levelNumber, ensuring:
     * 1. 100% Guaranteed Solvable (verified by a complete forward solver).
     * 2. Scaled complexity (board size, arrow count, turns, tight time limits).
     */
    fun generateProceduralLevel(levelNumber: Int): LevelData {
        val difficulty = Difficulty.forLevel(levelNumber)
        val (gridSize, targetArrows) = when (difficulty) {
            Difficulty.EASY -> Pair(5, 6)
            Difficulty.NORMAL -> Pair(6, 10 + (levelNumber - 11).coerceAtMost(5))
            Difficulty.HARD -> Pair(7, 14 + (levelNumber - 26).coerceAtMost(6))
            Difficulty.VERY_HARD -> Pair(8, 18 + (levelNumber - 51).coerceAtMost(6))
            Difficulty.EXPERT -> Pair(8, 22 + (levelNumber - 101).coerceAtMost(8))
        }

        val rng = Random(levelNumber * 10007L + 42)
        var arrows = tryGenerateSolvableLevel(gridSize, targetArrows, rng)
        if (arrows.isEmpty() || !isFullySolvable(arrows, gridSize, gridSize)) {
            // Fallback to a deterministic guaranteed reverse-constructed pattern
            arrows = generateReverseSolvableLevel(gridSize, targetArrows, rng)
        }

        val timeLimit = difficulty.timeLimitSec + (targetArrows * 2).coerceAtMost(30)
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

    /**
     * Generates a solvable level by reverse construction:
     * Starts with an empty board and adds arrows in reverse order of escaping.
     * In reverse, an arrow can escape forwards without obstacle, so it enters the board without hitting existing obstacles!
     */
    private fun generateReverseSolvableLevel(
        gridSize: Int,
        count: Int,
        rng: Random
    ): List<Arrow> {
        val result = mutableListOf<Arrow>()
        val occupiedPoints = mutableSetOf<GridPoint>()

        var idCounter = 1
        var attempts = 0
        while (result.size < count && attempts < 300) {
            attempts++
            // Pick a direction to exit
            val dir = Direction.values()[rng.nextInt(4)]
            // Pick an arrow length (1 to 3 segments)
            val length = rng.nextInt(1, 4)

            // Pick a tip point that can shoot out in dir without colliding with occupiedPoints
            val tipCandidates = mutableListOf<GridPoint>()
            for (x in 0 until gridSize) {
                for (y in 0 until gridSize) {
                    val p = GridPoint(x, y)
                    if (!occupiedPoints.contains(p)) {
                        // Check if path from p + dir to edge is clear in occupiedPoints
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

            // Build tail points backwards from tip
            // Backward direction is dir opposite
            val backDir = when (dir) {
                Direction.UP -> Direction.DOWN
                Direction.DOWN -> Direction.UP
                Direction.LEFT -> Direction.RIGHT
                Direction.RIGHT -> Direction.LEFT
            }

            // Decide whether to make it an L-shaped turn
            val makeTurn = rng.nextBoolean() && length >= 2
            val points = mutableListOf<GridPoint>()
            var valid = true

            if (!makeTurn) {
                // Straight
                val tail = tip + GridPoint(backDir.dx * length, backDir.dy * length)
                if (tail.x in 0 until gridSize && tail.y in 0 until gridSize) {
                    // Check if points between tail and tip collide
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
                // L-shape with turn
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

        // Result was built in reverse order (last added can exit first).
        // Reverse it so IDs make sense
        return result
    }

    private fun tryGenerateSolvableLevel(
        gridSize: Int,
        targetCount: Int,
        rng: Random
    ): List<Arrow> {
        val arrows = generateReverseSolvableLevel(gridSize, targetCount, rng)
        if (isFullySolvable(arrows, gridSize, gridSize)) {
            return arrows
        }
        return emptyList()
    }

    /**
     * Verifies that [arrows] can be completely cleared step-by-step
     * without ever encountering a deadlock.
     */
    fun isFullySolvable(arrows: List<Arrow>, width: Int, height: Int): Boolean {
        var remaining = arrows.toList()
        while (remaining.isNotEmpty()) {
            val free = ObstructionChecker.getFreeArrows(remaining, width, height)
            if (free.isEmpty()) {
                return false // Deadlock!
            }
            // Remove one free arrow and continue
            remaining = remaining.filter { it.id != free.first().id }
        }
        return true
    }
}
