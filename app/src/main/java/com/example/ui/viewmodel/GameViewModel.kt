package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.LevelProgressEntity
import com.example.data.repository.GameRepository
import com.example.domain.logic.LevelGenerator
import com.example.domain.logic.ObstructionChecker
import com.example.domain.model.Arrow
import com.example.domain.model.LevelData
import com.example.ui.audio.AudioHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class GameOverReason {
    OUT_OF_LIVES,
    OUT_OF_TIME
}

data class GameUiState(
    val currentLevel: LevelData,
    val activeArrows: List<Arrow> = emptyList(),
    val totalArrowsCount: Int = 0,
    val escapingArrowIds: Set<Int> = emptySet(),
    val blockedArrowIds: Set<Int> = emptySet(),
    val hintedArrowId: Int? = null,
    val remainingLives: Int = 3,
    val maxLives: Int = 3,
    val timeRemainingSeconds: Int = 60,
    val totalTimeSeconds: Int = 60,
    val isTimerRunning: Boolean = false,
    val isLevelComplete: Boolean = false,
    val isGameOver: Boolean = false,
    val gameOverReason: GameOverReason? = null,
    val movesCount: Int = 0,
    val starsEarned: Int = 0,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val showSettingsDialog: Boolean = false,
    val showLevelSelectDialog: Boolean = false,
    val maxUnlockedLevel: Int = 1
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository(application)
    private val audioHelper = AudioHelper(application)

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    val allProgress: StateFlow<List<LevelProgressEntity>> = repository.getAllProgress()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var timerJob: Job? = null
    private var hintJob: Job? = null

    init {
        // Observe sound/haptics settings
        viewModelScope.launch {
            repository.soundEnabled.collect { enabled ->
                _uiState.update { it.copy(soundEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            repository.hapticsEnabled.collect { enabled ->
                _uiState.update { it.copy(hapticsEnabled = enabled) }
            }
        }
        // Observe highest unlocked level
        viewModelScope.launch {
            repository.getMaxCompletedLevel().collect { maxCompleted ->
                val highest = (maxCompleted ?: 0) + 1
                _uiState.update { it.copy(maxUnlockedLevel = maxOf(it.maxUnlockedLevel, highest)) }
            }
        }

        loadLevel(1)
    }

    private fun createInitialState(): GameUiState {
        val level1 = LevelGenerator.getLevel(1)
        return GameUiState(
            currentLevel = level1,
            activeArrows = level1.arrows,
            totalArrowsCount = level1.arrows.size,
            timeRemainingSeconds = level1.timeLimitSeconds,
            totalTimeSeconds = level1.timeLimitSeconds
        )
    }

    fun loadLevel(levelNumber: Int) {
        timerJob?.cancel()
        hintJob?.cancel()

        val levelData = LevelGenerator.getLevel(levelNumber)
        _uiState.update {
            it.copy(
                currentLevel = levelData,
                activeArrows = levelData.arrows,
                totalArrowsCount = levelData.arrows.size,
                escapingArrowIds = emptySet(),
                blockedArrowIds = emptySet(),
                hintedArrowId = null,
                remainingLives = 3,
                timeRemainingSeconds = levelData.timeLimitSeconds,
                totalTimeSeconds = levelData.timeLimitSeconds,
                isTimerRunning = true,
                isLevelComplete = false,
                isGameOver = false,
                gameOverReason = null,
                movesCount = 0,
                starsEarned = 0,
                maxUnlockedLevel = maxOf(it.maxUnlockedLevel, levelNumber)
            )
        }

        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000L)
                val current = _uiState.value
                if (!current.isTimerRunning || current.isLevelComplete || current.isGameOver) {
                    break
                }
                if (current.timeRemainingSeconds <= 1) {
                    _uiState.update {
                        it.copy(
                            timeRemainingSeconds = 0,
                            isTimerRunning = false,
                            isGameOver = true,
                            gameOverReason = GameOverReason.OUT_OF_TIME
                        )
                    }
                    audioHelper.playGameOver(current.soundEnabled, current.hapticsEnabled)
                    break
                } else {
                    _uiState.update { it.copy(timeRemainingSeconds = it.timeRemainingSeconds - 1) }
                }
            }
        }
    }

    fun onArrowTapped(arrow: Arrow) {
        val state = _uiState.value
        if (state.isLevelComplete || state.isGameOver) return
        if (state.escapingArrowIds.contains(arrow.id)) return

        val isFree = ObstructionChecker.isArrowFree(
            arrow = arrow,
            activeArrows = state.activeArrows,
            gridWidth = state.currentLevel.gridWidth,
            gridHeight = state.currentLevel.gridHeight
        )

        if (isFree) {
            // Unobstructed arrow: escapes cleanly!
            audioHelper.playArrowEscape(state.soundEnabled, state.hapticsEnabled)
            _uiState.update {
                it.copy(
                    escapingArrowIds = it.escapingArrowIds + arrow.id,
                    hintedArrowId = if (it.hintedArrowId == arrow.id) null else it.hintedArrowId,
                    movesCount = it.movesCount + 1
                )
            }

            viewModelScope.launch {
                // Allow escape animation to slide smoothly off the board
                delay(280L)
                val updatedArrows = _uiState.value.activeArrows.filter { it.id != arrow.id }
                val isComplete = updatedArrows.isEmpty()

                val stars = if (isComplete) {
                    calculateStars(_uiState.value.remainingLives, _uiState.value.timeRemainingSeconds, _uiState.value.totalTimeSeconds)
                } else 0

                _uiState.update {
                    it.copy(
                        activeArrows = updatedArrows,
                        escapingArrowIds = it.escapingArrowIds - arrow.id,
                        isLevelComplete = isComplete,
                        isTimerRunning = !isComplete,
                        starsEarned = stars
                    )
                }

                if (isComplete) {
                    timerJob?.cancel()
                    val timeTaken = state.totalTimeSeconds - _uiState.value.timeRemainingSeconds
                    repository.saveLevelSuccess(state.currentLevel.levelNumber, stars, timeTaken)
                    audioHelper.playLevelCompleted(state.soundEnabled, state.hapticsEnabled)
                }
            }
        } else {
            // Blocked arrow: shake warning, lose 1 heart
            audioHelper.playArrowBlocked(state.soundEnabled, state.hapticsEnabled)
            val newLives = state.remainingLives - 1
            val isDead = newLives <= 0

            _uiState.update {
                it.copy(
                    blockedArrowIds = it.blockedArrowIds + arrow.id,
                    remainingLives = maxOf(0, newLives),
                    isGameOver = isDead,
                    gameOverReason = if (isDead) GameOverReason.OUT_OF_LIVES else null,
                    isTimerRunning = !isDead,
                    movesCount = it.movesCount + 1
                )
            }

            if (isDead) {
                timerJob?.cancel()
                audioHelper.playGameOver(state.soundEnabled, state.hapticsEnabled)
            }

            viewModelScope.launch {
                delay(400L)
                _uiState.update { it.copy(blockedArrowIds = it.blockedArrowIds - arrow.id) }
            }
        }
    }

    private fun calculateStars(livesLeft: Int, timeLeft: Int, totalTime: Int): Int {
        val timeRatio = if (totalTime > 0) timeLeft.toFloat() / totalTime else 0f
        return when {
            livesLeft == 3 && timeRatio >= 0.25f -> 3
            livesLeft >= 2 -> 2
            else -> 1
        }
    }

    fun onHintRequested() {
        val state = _uiState.value
        if (state.isLevelComplete || state.isGameOver) return

        val freeArrows = ObstructionChecker.getFreeArrows(
            activeArrows = state.activeArrows.filter { !state.escapingArrowIds.contains(it.id) },
            gridWidth = state.currentLevel.gridWidth,
            gridHeight = state.currentLevel.gridHeight
        )

        if (freeArrows.isNotEmpty()) {
            val chosen = freeArrows.first()
            audioHelper.playHint(state.soundEnabled, state.hapticsEnabled)
            _uiState.update { it.copy(hintedArrowId = chosen.id) }

            hintJob?.cancel()
            hintJob = viewModelScope.launch {
                delay(4000L)
                _uiState.update {
                    if (it.hintedArrowId == chosen.id) it.copy(hintedArrowId = null) else it
                }
            }
        }
    }

    fun restartCurrentLevel() {
        loadLevel(_uiState.value.currentLevel.levelNumber)
    }

    fun nextLevel() {
        loadLevel(_uiState.value.currentLevel.levelNumber + 1)
    }

    fun toggleSound() {
        val next = !_uiState.value.soundEnabled
        repository.toggleSound(next)
    }

    fun toggleHaptics() {
        val next = !_uiState.value.hapticsEnabled
        repository.toggleHaptics(next)
    }

    fun openSettings(show: Boolean) {
        _uiState.update { it.copy(showSettingsDialog = show) }
    }

    fun openLevelSelect(show: Boolean) {
        _uiState.update { it.copy(showLevelSelectDialog = show) }
    }

    fun resetAllProgress() {
        viewModelScope.launch {
            repository.resetAllProgress()
            loadLevel(1)
        }
    }
}
