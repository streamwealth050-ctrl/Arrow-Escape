package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ArrowEscapeDatabase
import com.example.data.model.UserGameStateEntity
import com.example.data.repository.GameRepository
import com.example.domain.logic.LevelGenerator
import com.example.domain.logic.ObstructionChecker
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Arrow Escape", appName)
    }

    @Test
    fun `verify level 1 to 10 are all solvable`() {
        for (lvl in 1..10) {
            val level = LevelGenerator.getLevel(lvl)
            assertTrue(
                "Level $lvl should be fully solvable with shape ${level.shapeType}",
                LevelGenerator.isFullySolvable(level.arrows, level.gridWidth, level.gridHeight, level.shapeType)
            )
        }
    }

    @Test
    fun `verify shape levels are solvable`() {
        for (lvl in listOf(3, 4, 6, 7, 8, 9)) {
            val level = LevelGenerator.getLevel(lvl)
            assertTrue(
                "Shape Level $lvl (${level.shapeType}) should be fully solvable",
                LevelGenerator.isFullySolvable(level.arrows, level.gridWidth, level.gridHeight, level.shapeType)
            )
            // Verify all arrows are valid multi-bend shapes with 2 to 6 points (Straight, L-bend, U-turn, Serpentine, Castle, Spiral, Frame, Extended Loop)
            for (arrow in level.arrows) {
                assertTrue(
                    "Arrow ${arrow.id} in level $lvl should have between 2 and 6 points, had ${arrow.points.size}",
                    arrow.points.size in 2..6
                )
            }
        }
    }

    @Test
    fun `verify level 5 has advanced benchmark challenge with limited master keys and deep dependencies`() {
        val level5 = LevelGenerator.getLevel(5)
        assertEquals("Level 5 should be EASY tier in user progression", com.example.domain.model.Difficulty.EASY, level5.difficulty)
        assertTrue("Level 5 should have at least 15 arrows, had ${level5.arrows.size}", level5.arrows.size >= 15)

        val complexity = LevelGenerator.calculateComplexity(level5.arrows, level5.gridWidth, level5.gridHeight, level5.shapeType)
        assertTrue("Level 5 must be 100% solvable", complexity.isSolvable)
        assertTrue("Level 5 must start with limited Master Keys, had ${complexity.initialFreeCount}", complexity.initialFreeCount in 1..4)
        assertTrue("Level 5 must require at least 5 wave layers of sequential deductions, had ${complexity.waveLayers}", complexity.waveLayers >= 5)
    }

    @Test
    fun `verify progressive difficulty mapping across all tiers`() {
        assertEquals(com.example.domain.model.Difficulty.EASY, com.example.domain.model.Difficulty.forLevel(1))
        assertEquals(com.example.domain.model.Difficulty.EASY, com.example.domain.model.Difficulty.forLevel(5))
        assertEquals(com.example.domain.model.Difficulty.MEDIUM, com.example.domain.model.Difficulty.forLevel(6))
        assertEquals(com.example.domain.model.Difficulty.MEDIUM, com.example.domain.model.Difficulty.forLevel(10))
        assertEquals(com.example.domain.model.Difficulty.HARD, com.example.domain.model.Difficulty.forLevel(11))
        assertEquals(com.example.domain.model.Difficulty.HARD, com.example.domain.model.Difficulty.forLevel(20))
        assertEquals(com.example.domain.model.Difficulty.VERY_HARD, com.example.domain.model.Difficulty.forLevel(21))
        assertEquals(com.example.domain.model.Difficulty.VERY_HARD, com.example.domain.model.Difficulty.forLevel(35))
        assertEquals(com.example.domain.model.Difficulty.EXPERT, com.example.domain.model.Difficulty.forLevel(36))
        assertEquals(com.example.domain.model.Difficulty.EXPERT, com.example.domain.model.Difficulty.forLevel(50))
        assertEquals(com.example.domain.model.Difficulty.EXPERT, com.example.domain.model.Difficulty.forLevel(100))
    }

    @Test
    fun `verify room saves and restores game state for resuming`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = GameRepository(context)

        // Simulate playing up to level 4 and saving progress
        repository.saveLevelSuccess(levelNumber = 1, stars = 3, timeSeconds = 25)
        repository.saveLevelSuccess(levelNumber = 2, stars = 2, timeSeconds = 30)
        repository.saveLevelSuccess(levelNumber = 3, stars = 3, timeSeconds = 28)
        repository.saveCurrentLevel(4)

        // Read back state from Room
        val savedState = repository.getUserGameStateSync()
        assertNotNull("Saved state should exist in Room", savedState)
        assertEquals(4, savedState?.currentLevelNumber)
        assertTrue("Max unlocked level should be at least 4", (savedState?.maxUnlockedLevel ?: 0) >= 4)

        val level1Progress = repository.getProgressForLevel(1)
        assertNotNull(level1Progress)
        assertEquals(3, level1Progress?.stars)
        assertTrue(level1Progress?.completed == true)
    }

    @Test
    fun `completing level 1 advances current level to level 2 and resumes on reopen`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = GameRepository(context)
        repository.resetAllProgress()
        repository.saveLevelSuccess(levelNumber = 1, stars = 3, timeSeconds = 20)

        val target = repository.sanitizeLevels()
        assertEquals(2, target)

        val state = repository.getUserGameStateSync()
        assertNotNull(state)
        assertEquals(2, state?.currentLevelNumber)
        assertTrue((state?.maxUnlockedLevel ?: 0) >= 2)
    }

    @Test
    fun `addLives grants exactly 1 life`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = com.example.ui.viewmodel.GameViewModel(app)

        viewModel.addLives()
        assertEquals(1, viewModel.uiState.value.remainingLives)
        assertEquals(false, viewModel.uiState.value.isGameOver)
    }

    @Test
    fun `completing level 1 and tapping nextLevel loads level 2 without skipping`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        kotlinx.coroutines.runBlocking {
            GameRepository(app).resetAllProgress()
        }
        val viewModel = com.example.ui.viewmodel.GameViewModel(app)

        viewModel.loadLevel(1)
        Thread.sleep(500)
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        assertEquals(1, viewModel.uiState.value.currentLevel.levelNumber)

        val level1Arrows = viewModel.uiState.value.activeArrows.toList()
        for (arrow in level1Arrows) {
            viewModel.onArrowAnimationFinished(arrow)
        }
        assertTrue(viewModel.uiState.value.isLevelComplete)

        // Calling nextLevel must load Level 2, NOT skip to Level 3
        viewModel.nextLevel()
        val deadline = System.currentTimeMillis() + 5000
        while (System.currentTimeMillis() < deadline && viewModel.uiState.value.currentLevel.levelNumber != 2) {
            Thread.sleep(100)
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
        }
        assertEquals(2, viewModel.uiState.value.currentLevel.levelNumber)
    }

    @Test
    fun `sequential level progression through multiple levels never skips any level`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        kotlinx.coroutines.runBlocking {
            GameRepository(app).resetAllProgress()
        }
        val viewModel = com.example.ui.viewmodel.GameViewModel(app)

        // 1. Play Level 1
        viewModel.loadLevel(1)
        var deadline = System.currentTimeMillis() + 5000
        while (System.currentTimeMillis() < deadline && viewModel.uiState.value.currentLevel.levelNumber != 1) {
            Thread.sleep(100)
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
        }
        assertEquals(1, viewModel.uiState.value.currentLevel.levelNumber)

        // Complete Level 1
        for (arrow in viewModel.uiState.value.activeArrows.toList()) {
            viewModel.onArrowAnimationFinished(arrow)
        }
        assertTrue(viewModel.uiState.value.isLevelComplete)

        // Tap Next Level -> must load Level 2
        viewModel.nextLevel()
        deadline = System.currentTimeMillis() + 5000
        while (System.currentTimeMillis() < deadline && viewModel.uiState.value.currentLevel.levelNumber != 2) {
            Thread.sleep(100)
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
        }
        assertEquals(2, viewModel.uiState.value.currentLevel.levelNumber)

        // Complete Level 2
        for (arrow in viewModel.uiState.value.activeArrows.toList()) {
            viewModel.onArrowAnimationFinished(arrow)
        }
        assertTrue(viewModel.uiState.value.isLevelComplete)

        // Tap Next Level -> must load Level 3, NOT Level 4!
        viewModel.nextLevel()
        deadline = System.currentTimeMillis() + 5000
        while (System.currentTimeMillis() < deadline && viewModel.uiState.value.currentLevel.levelNumber != 3) {
            Thread.sleep(100)
            org.robolectric.shadows.ShadowLooper.idleMainLooper()
        }
        assertEquals(3, viewModel.uiState.value.currentLevel.levelNumber)
    }
}
