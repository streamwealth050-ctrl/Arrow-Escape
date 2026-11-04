package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.ArrowEscapeDatabase
import com.example.data.dao.LevelProgressDao
import com.example.data.dao.UserGameStateDao
import com.example.data.model.LevelProgressEntity
import com.example.data.model.UserGameStateEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class GameRepository(context: Context) {
    private val db = ArrowEscapeDatabase.getInstance(context)
    private val levelDao: LevelProgressDao = db.levelProgressDao()
    private val stateDao: UserGameStateDao = db.userGameStateDao()
    private val prefs: SharedPreferences =
        context.getSharedPreferences("arrow_escape_prefs", Context.MODE_PRIVATE)

    private val _soundEnabled = MutableStateFlow(prefs.getBoolean("sound_enabled", true))
    val soundEnabled = _soundEnabled.asStateFlow()

    private val _hapticsEnabled = MutableStateFlow(prefs.getBoolean("haptics_enabled", true))
    val hapticsEnabled = _hapticsEnabled.asStateFlow()

    fun hasAcceptedTerms(): Boolean = prefs.getBoolean("has_accepted_terms", false)

    fun setAcceptedTerms(accepted: Boolean) {
        prefs.edit().putBoolean("has_accepted_terms", accepted).apply()
    }

    // Level Progress streams
    fun getAllProgress(): Flow<List<LevelProgressEntity>> = levelDao.getAllProgress()

    fun getMaxCompletedLevel(): Flow<Int?> = levelDao.getMaxCompletedLevel()

    // Persistent User Game State streams (for resuming game and keeping unlocked levels)
    fun getUserGameState(): Flow<UserGameStateEntity?> = stateDao.getUserGameState()

    suspend fun getProgressForLevel(level: Int): LevelProgressEntity? = withContext(Dispatchers.IO) {
        levelDao.getProgressForLevel(level)
    }

    suspend fun getUserGameStateSync(): UserGameStateEntity? = withContext(Dispatchers.IO) {
        stateDao.getUserGameStateSync()
    }

    suspend fun saveCurrentLevel(levelNumber: Int) = withContext(Dispatchers.IO) {
        if (levelNumber >= 1000) return@withContext
        val existing = stateDao.getUserGameStateSync()
        if (existing == null) {
            stateDao.saveUserGameState(
                UserGameStateEntity(
                    currentLevelNumber = levelNumber,
                    maxUnlockedLevel = levelNumber,
                    lastPlayedTimestamp = System.currentTimeMillis()
                )
            )
        } else {
            stateDao.updateCurrentLevel(levelNumber, System.currentTimeMillis())
            if (levelNumber > existing.maxUnlockedLevel && levelNumber < 1000) {
                stateDao.updateMaxUnlockedLevel(levelNumber)
            }
        }
    }

    suspend fun saveUserGameState(state: UserGameStateEntity) = withContext(Dispatchers.IO) {
        stateDao.saveUserGameState(state)
    }

    suspend fun saveLevelSuccess(levelNumber: Int, stars: Int, timeSeconds: Int) = withContext(Dispatchers.IO) {
        if (levelNumber >= 1000) return@withContext
        val existing = levelDao.getProgressForLevel(levelNumber)
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
        levelDao.saveProgress(entity)

        // Also update the persistent user game state so next level is saved as the current level and maxUnlockedLevel is bumped
        val nextLevel = levelNumber + 1
        val state = stateDao.getUserGameStateSync()
        if (state == null) {
            stateDao.saveUserGameState(
                UserGameStateEntity(
                    currentLevelNumber = nextLevel,
                    maxUnlockedLevel = nextLevel,
                    totalCoins = 250 + (stars * 20),
                    lastPlayedTimestamp = System.currentTimeMillis()
                )
            )
        } else {
            val newMax = maxOf(state.maxUnlockedLevel, nextLevel)
            val newCurrent = maxOf(state.currentLevelNumber, nextLevel)
            stateDao.saveUserGameState(
                state.copy(
                    currentLevelNumber = newCurrent,
                    maxUnlockedLevel = newMax,
                    totalCoins = state.totalCoins + (stars * 20),
                    lastPlayedTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun sanitizeLevels(): Int = withContext(Dispatchers.IO) {
        levelDao.deleteCorruptedDailyLevels()
        val regular = levelDao.getRegularProgressSync()
        val maxCompleted = regular.filter { it.completed }.maxOfOrNull { it.levelNumber } ?: 0
        val targetLevel = maxCompleted + 1
        val state = stateDao.getUserGameStateSync()
        if (state != null) {
            val safeCurrent = if (state.currentLevelNumber >= 1000) targetLevel else maxOf(state.currentLevelNumber, targetLevel)
            val safeMax = if (state.maxUnlockedLevel >= 1000) maxOf(safeCurrent, targetLevel) else maxOf(state.maxUnlockedLevel, targetLevel)
            if (safeCurrent != state.currentLevelNumber || safeMax != state.maxUnlockedLevel) {
                stateDao.saveUserGameState(
                    state.copy(
                        currentLevelNumber = safeCurrent,
                        maxUnlockedLevel = safeMax,
                        lastPlayedTimestamp = System.currentTimeMillis()
                    )
                )
            }
        }
        targetLevel
    }

    suspend fun updatePlayerCoinsAndArrows(coins: Int, arrowsCleared: Int) = withContext(Dispatchers.IO) {
        val state = stateDao.getUserGameStateSync()
        if (state != null) {
            stateDao.saveUserGameState(
                state.copy(
                    totalCoins = coins,
                    totalArrowsCleared = arrowsCleared,
                    lastPlayedTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun resetAllProgress() = withContext(Dispatchers.IO) {
        levelDao.clearAllProgress()
        stateDao.clearGameState()
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
