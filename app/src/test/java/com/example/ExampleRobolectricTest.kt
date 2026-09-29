package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.domain.logic.LevelGenerator
import com.example.domain.logic.ObstructionChecker
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
                "Level $lvl should be fully solvable",
                LevelGenerator.isFullySolvable(level.arrows, level.gridWidth, level.gridHeight)
            )
        }
    }

    @Test
    fun `verify procedural levels are solvable`() {
        for (lvl in listOf(15, 30, 60, 105)) {
            val level = LevelGenerator.getLevel(lvl)
            assertTrue(
                "Procedural Level $lvl should be fully solvable",
                LevelGenerator.isFullySolvable(level.arrows, level.gridWidth, level.gridHeight)
            )
        }
    }
}
