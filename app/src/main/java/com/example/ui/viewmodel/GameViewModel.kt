package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.LevelProgressEntity
import com.example.data.repository.GameRepository
import com.example.domain.logic.LevelGenerator
import com.example.domain.logic.ObstructionChecker
import com.example.domain.model.Arrow
import com.example.domain.model.DailyMission
import com.example.domain.model.LeaderboardEntry
import com.example.domain.model.LevelData
import com.example.domain.model.LoginType
import com.example.domain.model.UserProfile
import com.example.ui.audio.SoundEffectManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppScreen {
    SPLASH,
    AUTH,
    HOME,
    GAME,
    LEADERBOARD,
    MISSIONS
}

enum class GameOverReason {
    OUT_OF_LIVES,
    OUT_OF_TIME
}

data class GameUiState(
    val currentScreen: AppScreen = AppScreen.SPLASH,
    val userProfile: UserProfile = UserProfile(),
    val isAuthenticated: Boolean = false,
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
    val maxUnlockedLevel: Int = 1,
    val dailyMissions: List<DailyMission> = emptyList(),
    val leaderboardEntries: List<LeaderboardEntry> = emptyList()
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository(application)
    val soundManager = SoundEffectManager(application)

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    val allProgress: StateFlow<List<LevelProgressEntity>> = repository.getAllProgress()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var timerJob: Job? = null
    private var hintJob: Job? = null

    init {
        // Observe sound/haptics preferences
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
        // Observe unlocked level
        viewModelScope.launch {
            repository.getMaxCompletedLevel().collect { maxCompleted ->
                val highest = (maxCompleted ?: 0) + 1
                _uiState.update {
                    it.copy(
                        maxUnlockedLevel = maxOf(it.maxUnlockedLevel, highest),
                        leaderboardEntries = generateLeaderboard(highest, it.userProfile)
                    )
                }
            }
        }

        // Setup daily missions
        setupDailyMissions()

        // Splash screen delay -> transitions to AUTH or HOME
        viewModelScope.launch {
            delay(1600L)
            _uiState.update {
                if (it.isAuthenticated) {
                    it.copy(currentScreen = AppScreen.HOME)
                } else {
                    it.copy(currentScreen = AppScreen.AUTH)
                }
            }
        }
    }

    private fun createInitialState(): GameUiState {
        val level1 = LevelGenerator.getLevel(1)
        val initialProfile = UserProfile()
        return GameUiState(
            currentScreen = AppScreen.SPLASH,
            userProfile = initialProfile,
            currentLevel = level1,
            activeArrows = level1.arrows,
            totalArrowsCount = level1.arrows.size,
            timeRemainingSeconds = level1.timeLimitSeconds,
            totalTimeSeconds = level1.timeLimitSeconds,
            leaderboardEntries = generateLeaderboard(1, initialProfile)
        )
    }

    private fun setupDailyMissions() {
        _uiState.update {
            it.copy(
                dailyMissions = listOf(
                    DailyMission(
                        id = 1,
                        title = "Escape 15 Arrows",
                        description = "Tap and escape arrows from any puzzle board",
                        target = 15,
                        current = 7,
                        rewardCoins = 50,
                        iconEmoji = "🏹"
                    ),
                    DailyMission(
                        id = 2,
                        title = "Flawless Clear",
                        description = "Clear 2 levels without losing a single heart",
                        target = 2,
                        current = 1,
                        rewardCoins = 100,
                        iconEmoji = "❤️"
                    ),
                    DailyMission(
                        id = 3,
                        title = "Speed Untangler",
                        description = "Complete any level with more than 30s remaining",
                        target = 1,
                        current = 0,
                        rewardCoins = 150,
                        iconEmoji = "⚡"
                    )
                )
            )
        }
    }

    // Auth actions
    fun loginWithGoogle() {
        soundManager.playClick(_uiState.value.soundEnabled, _uiState.value.hapticsEnabled)
        val updatedProfile = _uiState.value.userProfile.copy(
            name = "Alex River",
            loginType = LoginType.GOOGLE,
            avatarEmoji = "⭐"
        )
        _uiState.update {
            it.copy(
                isAuthenticated = true,
                userProfile = updatedProfile,
                currentScreen = AppScreen.HOME,
                leaderboardEntries = generateLeaderboard(it.maxUnlockedLevel, updatedProfile)
            )
        }
    }

    fun loginWithFacebook() {
        soundManager.playClick(_uiState.value.soundEnabled, _uiState.value.hapticsEnabled)
        val updatedProfile = _uiState.value.userProfile.copy(
            name = "Jordan Lee",
            loginType = LoginType.FACEBOOK,
            avatarEmoji = "🎯"
        )
        _uiState.update {
            it.copy(
                isAuthenticated = true,
                userProfile = updatedProfile,
                currentScreen = AppScreen.HOME,
                leaderboardEntries = generateLeaderboard(it.maxUnlockedLevel, updatedProfile)
            )
        }
    }

    fun loginAsGuest() {
        soundManager.playClick(_uiState.value.soundEnabled, _uiState.value.hapticsEnabled)
        val updatedProfile = _uiState.value.userProfile.copy(
            name = "Guest Player #342",
            loginType = LoginType.GUEST,
            avatarEmoji = "🐹"
        )
        _uiState.update {
            it.copy(
                isAuthenticated = true,
                userProfile = updatedProfile,
                currentScreen = AppScreen.HOME,
                leaderboardEntries = generateLeaderboard(it.maxUnlockedLevel, updatedProfile)
            )
        }
    }

    fun navigateTo(screen: AppScreen) {
        soundManager.playClick(_uiState.value.soundEnabled, _uiState.value.hapticsEnabled)
        if (screen == AppScreen.GAME && !_uiState.value.isTimerRunning && !_uiState.value.isLevelComplete && !_uiState.value.isGameOver) {
            startTimer()
        }
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun loadLevel(levelNumber: Int) {
        timerJob?.cancel()
        hintJob?.cancel()

        val levelData = LevelGenerator.getLevel(levelNumber)
        _uiState.update {
            it.copy(
                currentScreen = AppScreen.GAME,
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
                if (!current.isTimerRunning || current.isLevelComplete || current.isGameOver || current.currentScreen != AppScreen.GAME) {
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
                    soundManager.playBlocked(current.soundEnabled, current.hapticsEnabled)
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

        soundManager.playClick(state.soundEnabled, state.hapticsEnabled)

        val isFree = ObstructionChecker.isArrowFree(
            arrow = arrow,
            activeArrows = state.activeArrows,
            gridWidth = state.currentLevel.gridWidth,
            gridHeight = state.currentLevel.gridHeight
        )

        if (isFree) {
            // Unobstructed: start real-time slide-out animation!
            soundManager.playEscape(state.soundEnabled, state.hapticsEnabled)
            _uiState.update {
                it.copy(
                    escapingArrowIds = it.escapingArrowIds + arrow.id,
                    hintedArrowId = if (it.hintedArrowId == arrow.id) null else it.hintedArrowId,
                    movesCount = it.movesCount + 1,
                    userProfile = it.userProfile.copy(totalArrowsCleared = it.userProfile.totalArrowsCleared + 1),
                    // Update mission 1
                    dailyMissions = it.dailyMissions.map { m ->
                        if (m.id == 1 && m.current < m.target) m.copy(current = m.current + 1) else m
                    }
                )
            }
        } else {
            // Blocked: shake and lose 1 heart
            soundManager.playBlocked(state.soundEnabled, state.hapticsEnabled)
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

            viewModelScope.launch {
                delay(380L)
                _uiState.update { it.copy(blockedArrowIds = it.blockedArrowIds - arrow.id) }
            }
        }
    }

    /**
     * Called when the arrow's Canvas coordinate slide-out animation completes.
     */
    fun onArrowAnimationFinished(arrow: Arrow) {
        val updatedArrows = _uiState.value.activeArrows.filter { it.id != arrow.id }
        val isComplete = updatedArrows.isEmpty()
        val state = _uiState.value

        val stars = if (isComplete) {
            calculateStars(state.remainingLives, state.timeRemainingSeconds, state.totalTimeSeconds)
        } else 0

        _uiState.update {
            it.copy(
                activeArrows = updatedArrows,
                escapingArrowIds = it.escapingArrowIds - arrow.id,
                isLevelComplete = isComplete,
                isTimerRunning = !isComplete,
                starsEarned = stars,
                // Update mission 2 if full lives retained
                dailyMissions = if (isComplete && state.remainingLives == 3) {
                    it.dailyMissions.map { m ->
                        if (m.id == 2 && m.current < m.target) m.copy(current = m.current + 1) else m
                    }
                } else it.dailyMissions
            )
        }

        if (isComplete) {
            timerJob?.cancel()
            val timeTaken = state.totalTimeSeconds - _uiState.value.timeRemainingSeconds
            viewModelScope.launch {
                repository.saveLevelSuccess(state.currentLevel.levelNumber, stars, timeTaken)
            }
            soundManager.playSuccessJingle(state.soundEnabled, state.hapticsEnabled)
        }
    }

    private fun calculateStars(livesLeft: Int, timeLeft: Int, totalTime: Int): Int {
        val timeRatio = if (totalTime > 0) timeLeft.toFloat() / totalTime else 0f
        return when {
            livesLeft == 3 && timeRatio >= 0.2f -> 3
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
            soundManager.playHint(state.soundEnabled, state.hapticsEnabled)
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

    fun claimMissionReward(missionId: Int) {
        soundManager.playSuccessJingle(_uiState.value.soundEnabled, _uiState.value.hapticsEnabled)
        _uiState.update { current ->
            val mission = current.dailyMissions.find { it.id == missionId }
            if (mission != null && mission.isComplete && !mission.isClaimed) {
                current.copy(
                    userProfile = current.userProfile.copy(coins = current.userProfile.coins + mission.rewardCoins),
                    dailyMissions = current.dailyMissions.map {
                        if (it.id == missionId) it.copy(isClaimed = true) else it
                    }
                )
            } else current
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

    private fun generateLeaderboard(userLevel: Int, profile: UserProfile): List<LeaderboardEntry> {
        val baseUsers = listOf(
            LeaderboardEntry(1, "HammyKing", 168, 492, "👑"),
            LeaderboardEntry(2, "PuzzleNinja", 142, 418, "🥷"),
            LeaderboardEntry(3, "ArrowWizard", 125, 365, "🧙"),
            LeaderboardEntry(4, "ZenMaster", 98, 286, "🧘"),
            LeaderboardEntry(5, "SpeedTapper", 82, 238, "⚡"),
            LeaderboardEntry(6, "LogicWhiz", 64, 185, "🧠")
        )

        val userRank = when {
            userLevel >= 170 -> 1
            userLevel >= 145 -> 2
            userLevel >= 130 -> 3
            userLevel >= 100 -> 4
            userLevel >= 85 -> 5
            userLevel >= 65 -> 6
            else -> 7
        }

        val userEntry = LeaderboardEntry(
            rank = userRank,
            name = "${profile.name} (You)",
            levelsCleared = maxOf(userLevel - 1, 1),
            stars = maxOf((userLevel - 1) * 3 - 2, 3),
            avatarEmoji = profile.avatarEmoji,
            isCurrentUser = true
        )

        val list = (baseUsers + userEntry).sortedBy { it.rank }
        return list.mapIndexed { idx, item -> item.copy(rank = idx + 1) }
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}
