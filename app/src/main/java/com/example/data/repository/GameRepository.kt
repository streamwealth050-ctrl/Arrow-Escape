package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.ArrowEscapeDatabase
import com.example.data.dao.LevelProgressDao
import com.example.data.model.LevelProgressEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class GameRepository(context: Context) {
    private val db = ArrowEscapeDatabase.getInstance(context)
    private val dao: LevelProgressDao = db.levelProgressDao()
    private val prefs: SharedPreferences =
        context.getSharedPreferences("arrow_escape_prefs", Context.MODE_PRIVATE)

    private val _soundEnabled = MutableStateFlow(prefs.getBoolean("sound_enabled", true))
    val soundEnabled = _soundEnabled.asStateFlow()

    private val _hapticsEnabled = MutableStateFlow(prefs.getBoolean("haptics_enabled", true))
    val hapticsEnabled = _hapticsEnabled.asStateFlow()

    fun getAllProgress(): Flow<List<LevelProgressEntity>> = dao.getAllProgress()

    fun getMaxCompletedLevel(): Flow<Int?> = dao.getMaxCompletedLevel()

    suspend fun getProgressForLevel(level: Int): LevelProgressEntity? = withContext(Dispatchers.IO) {
        dao.getProgressForLevel(level)
    }

    suspend fun saveLevelSuccess(levelNumber: Int, stars: Int, timeSeconds: Int) = withContext(Dispatchers.IO) {
        val existing = dao.getProgressForLevel(levelNumber)
        val bestStars = maxOf(stars, existing?.stars ?: 0)
        val bestTime = if (existing != null && existing.bestTimeSeconds > 0) {
            minOf(timeSeconds, existing.bestTimeSeconds)
        } else {
            timeSeconds
        }
        val entity = LevelProgressEntity(
            levelNumber = levelNumber,
            completed = true,
            stars = bestStars,
            bestTimeSeconds = bestTime,
            completedAt = System.currentTimeMillis()
        )
        dao.saveProgress(entity)
    }

    suspend fun resetAllProgress() = withContext(Dispatchers.IO) {
        dao.clearAllProgress()
    }

    fun toggleSound(enabled: Boolean) {
        _soundEnabled.value = enabled
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
    }

    fun toggleHaptics(enabled: Boolean) {
        _hapticsEnabled.value = enabled
        prefs.edit().putBoolean("haptics_enabled", enabled).apply()
    }
}
