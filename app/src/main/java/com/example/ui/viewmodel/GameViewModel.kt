package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.LevelProgressEntity
import com.example.data.model.UserGameStateEntity
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
import com.example.util.DateHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AppScreen {
    SPLASH,
    WELCOME,
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
    val campaignLevel: LevelData = currentLevel,
    val campaignLevelNumber: Int = 1,
    val isDailyChallenge: Boolean = false,
    val dailyChallengeDay: Int = DateHelper.getTodayDayOfMonth(),
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
    val leaderboardEntries: List<LeaderboardEntry> = emptyList(),
    val hasResumableGame: Boolean = false,
    val availableHints: Int = 2
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

        // Observe Room UserGameState: automatically restore saved progress, current level, and unlocked levels
        viewModelScope.launch {
            repository.sanitizeLevels()
            repository.getUserGameState().collect { savedState ->
                if (savedState != null) {
                    val loginType = try {
                        LoginType.valueOf(savedState.loginType)
                    } catch (_: Exception) {
                        LoginType.GUEST
                    }
                    val restoredProfile = _uiState.value.userProfile.copy(
                        name = savedState.playerName,
                        loginType = loginType,
                        avatarEmoji = savedState.avatarEmoji,
                        coins = savedState.totalCoins,
                        streakDays = savedState.streakDays,
                        totalArrowsCleared = savedState.totalArrowsCleared
                    )
                    val safeLevelNumber = if (savedState.currentLevelNumber >= 1000) 1 else maxOf(1, savedState.currentLevelNumber)
                    val safeMaxLevel = if (savedState.maxUnlockedLevel >= 1000) safeLevelNumber else maxOf(1, savedState.maxUnlockedLevel)
                    val restoredLevel = withContext(Dispatchers.Default) {
                        LevelGenerator.getLevel(safeLevelNumber)
                    }

                    _uiState.update { current ->
                        val isPlayingInGame = current.currentScreen == AppScreen.GAME
                        if (isPlayingInGame) {
                            // While actively on the GAME screen, only update player profile, authentication, and unlocked progress.
                            // Never overwrite currentLevel, activeArrows, timer, or completion status in the middle of gameplay or victory overlay!
                            current.copy(
                                isAuthenticated = savedState.isAuthenticated,
                                userProfile = restoredProfile,
                                maxUnlockedLevel = maxOf(current.maxUnlockedLevel, safeMaxLevel),
                                hasResumableGame = true,
                                leaderboardEntries = generateLeaderboard(
                                    maxOf(0, safeMaxLevel - 1),
                                    maxOf(0, safeMaxLevel - 1) * 3,
                                    restoredProfile.totalArrowsCleared,
                                    restoredProfile
                                )
                            )
                        } else {
                            current.copy(
                                isAuthenticated = savedState.isAuthenticated,
                                userProfile = restoredProfile,
                                campaignLevelNumber = safeLevelNumber,
                                campaignLevel = restoredLevel,
                                maxUnlockedLevel = maxOf(current.maxUnlockedLevel, safeMaxLevel),
                                currentLevel = restoredLevel,
                                activeArrows = restoredLevel.arrows,
                                totalArrowsCount = restoredLevel.arrows.size,
                                timeRemainingSeconds = restoredLevel.timeLimitSeconds,
                                totalTimeSeconds = restoredLevel.timeLimitSeconds,
                                hasResumableGame = true,
                                leaderboardEntries = generateLeaderboard(
                                    maxOf(0, safeMaxLevel - 1),
                                    maxOf(0, safeMaxLevel - 1) * 3,
                                    restoredProfile.totalArrowsCleared,
                                    restoredProfile
                                )
                            )
                        }
                    }
                }
            }
        }

        // Observe Room level progress and calculate real scores dynamically
        viewModelScope.launch {
            repository.getAllProgress().collect { progressList ->
                val regularProgress = progressList.filter { it.levelNumber < 1000 }
                val completedCount = regularProgress.count { it.completed }
                val totalStars = regularProgress.sumOf { it.stars }
                val highest = (regularProgress.filter { it.completed }.maxOfOrNull { it.levelNumber } ?: 0) + 1
                val currentLvl = _uiState.value.campaignLevelNumber
                val safeMax = if (_uiState.value.maxUnlockedLevel >= 1000) highest else maxOf(_uiState.value.maxUnlockedLevel, highest)
                val effectiveLevel = maxOf(currentLvl, highest)
                val levelData = if (effectiveLevel != currentLvl) {
                    withContext(Dispatchers.Default) { LevelGenerator.getLevel(effectiveLevel) }
                } else {
                    _uiState.value.campaignLevel
                }

                _uiState.update { current ->
                    val leaderboards = generateLeaderboard(completedCount, totalStars, current.userProfile.totalArrowsCleared, current.userProfile)
                    val isPlayingInGame = current.currentScreen == AppScreen.GAME
                    if (isPlayingInGame) {
                        current.copy(
                            maxUnlockedLevel = safeMax,
                            leaderboardEntries = leaderboards
                        )
                    } else {
                        current.copy(
                            maxUnlockedLevel = safeMax,
                            campaignLevelNumber = effectiveLevel,
                            campaignLevel = levelData,
                            currentLevel = levelData,
                            activeArrows = levelData.arrows,
                            totalArrowsCount = levelData.arrows.size,
                            timeRemainingSeconds = levelData.timeLimitSeconds,
                            totalTimeSeconds = levelData.timeLimitSeconds,
                            leaderboardEntries = leaderboards
                        )
                    }
                }
            }
        }

        // Setup daily missions
        setupDailyMissions()

        // Splash screen delay -> transitions to WELCOME if not accepted yet, else HOME
        viewModelScope.launch {
            delay(1200L)
            val hasAccepted = repository.hasAcceptedTerms()
            _uiState.update {
                it.copy(currentScreen = if (hasAccepted) AppScreen.HOME else AppScreen.WELCOME)
            }
        }
    }

    fun acceptTerms() {
        soundManager.playClick(_uiState.value.soundEnabled, _uiState.value.hapticsEnabled)
        repository.setAcceptedTerms(true)
        _uiState.update { it.copy(currentScreen = AppScreen.HOME) }
    }

    private fun createInitialState(): GameUiState {
        val level1 = LevelGenerator.getLevel(1)
        val initialProfile = UserProfile()
        return GameUiState(
            currentScreen = AppScreen.SPLASH,
            userProfile = initialProfile,
            currentLevel = level1,
            campaignLevel = level1,
            campaignLevelNumber = 1,
            isDailyChallenge = false,
            dailyChallengeDay = DateHelper.getTodayDayOfMonth(),
            activeArrows = level1.arrows,
            totalArrowsCount = level1.arrows.size,
            timeRemainingSeconds = level1.timeLimitSeconds,
            totalTimeSeconds = level1.timeLimitSeconds,
            leaderboardEntries = generateLeaderboard(0, 0, 0, initialProfile)
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

    // Auth actions with persistent Room storage
    fun loginWithGoogle() {
        soundManager.playClick(_uiState.value.soundEnabled, _uiState.value.hapticsEnabled)
        val updatedProfile = _uiState.value.userProfile.copy(
            name = "Alex River",
            loginType = LoginType.GOOGLE,
            avatarEmoji = "⭐"
        )
        persistAuthState(updatedProfile)
    }

    fun loginWithFacebook() {
        soundManager.playClick(_uiState.value.soundEnabled, _uiState.value.hapticsEnabled)
        val updatedProfile = _uiState.value.userProfile.copy(
            name = "Jordan Lee",
            loginType = LoginType.FACEBOOK,
            avatarEmoji = "🎯"
        )
        persistAuthState(updatedProfile)
    }

    fun loginAsGuest() {
        soundManager.playClick(_uiState.value.soundEnabled, _uiState.value.hapticsEnabled)
        val updatedProfile = _uiState.value.userProfile.copy(
            name = "Guest Player #342",
            loginType = LoginType.GUEST,
            avatarEmoji = "🐹"
        )
        persistAuthState(updatedProfile)
    }

    fun updatePlayerProfile(name: String, avatar: String) {
        soundManager.playClick(_uiState.value.soundEnabled, _uiState.value.hapticsEnabled)
        val validName = name.trim().ifEmpty { "Player 1" }
        val updatedProfile = _uiState.value.userProfile.copy(
            name = validName,
            avatarEmoji = avatar
        )
        persistAuthState(updatedProfile)
    }

    private fun persistAuthState(profile: UserProfile) {
        val currentLvl = _uiState.value.campaignLevelNumber
        val maxUnlocked = if (_uiState.value.maxUnlockedLevel >= 1000) currentLvl else _uiState.value.maxUnlockedLevel
        viewModelScope.launch {
            repository.saveUserGameState(
                UserGameStateEntity(
                    currentLevelNumber = currentLvl,
                    maxUnlockedLevel = maxUnlocked,
                    totalCoins = profile.coins,
                    streakDays = profile.streakDays,
                    playerName = profile.name,
                    loginType = profile.loginType.name,
                    avatarEmoji = profile.avatarEmoji,
                    totalArrowsCleared = profile.totalArrowsCleared,
                    isAuthenticated = true,
                    lastPlayedTimestamp = System.currentTimeMillis()
                )
            )
        }
        _uiState.update {
            val completed = maxOf(0, maxUnlocked - 1)
            val stars = completed * 3
            val arrows = profile.totalArrowsCleared
            it.copy(
                isAuthenticated = true,
                userProfile = profile,
                currentScreen = AppScreen.HOME,
                maxUnlockedLevel = maxUnlocked,
                currentLevel = it.campaignLevel,
                isDailyChallenge = false,
                leaderboardEntries = generateLeaderboard(completed, stars, arrows, profile)
            )
        }
    }

    fun navigateTo(screen: AppScreen) {
        soundManager.playClick(_uiState.value.soundEnabled, _uiState.value.hapticsEnabled)
        if (screen == AppScreen.GAME && !_uiState.value.isTimerRunning && !_uiState.value.isLevelComplete && !_uiState.value.isGameOver) {
            startTimer()
        }
        _uiState.update {
            if (screen == AppScreen.HOME) {
                it.copy(
                    currentScreen = screen,
                    currentLevel = it.campaignLevel,
                    isDailyChallenge = false
                )
            } else {
                it.copy(currentScreen = screen)
            }
        }
    }

    /**
     * Loads a specific level and saves current progress in Room so it can be resumed after closing.
     */
    fun loadLevel(levelNumber: Int) {
        val safeLevel = if (levelNumber >= 1000) 1 else maxOf(1, levelNumber)
        timerJob?.cancel()
        hintJob?.cancel()

        _uiState.update {
            it.copy(
                isLevelComplete = false,
                isGameOver = false,
                gameOverReason = null
            )
        }

        viewModelScope.launch {
            val levelData = withContext(Dispatchers.Default) {
                LevelGenerator.getLevel(safeLevel)
            }
            repository.saveCurrentLevel(safeLevel)

            _uiState.update {
                it.copy(
                    currentScreen = AppScreen.GAME,
                    currentLevel = levelData,
                    campaignLevel = levelData,
                    campaignLevelNumber = safeLevel,
                    isDailyChallenge = false,
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
                    maxUnlockedLevel = maxOf(it.maxUnlockedLevel, safeLevel),
                    hasResumableGame = true
                )
            }
            startTimer()

            // Background pre-fetch next level so Next Level transition is instantaneous
            withContext(Dispatchers.Default) {
                LevelGenerator.getLevel(safeLevel + 1)
            }
        }
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
                    soundManager.playLossGame(current.soundEnabled, current.hapticsEnabled)
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

        val nonEscapingActiveArrows = state.activeArrows.filter { !state.escapingArrowIds.contains(it.id) }
        val isFree = ObstructionChecker.isArrowFree(
            arrow = arrow,
            activeArrows = nonEscapingActiveArrows,
            gridWidth = state.currentLevel.gridWidth,
            gridHeight = state.currentLevel.gridHeight,
            shapeType = state.currentLevel.shapeType
        )

        if (isFree) {
            // Unobstructed: play user's custom Arrow Move sound effect immediately with 0ms delay!
            soundManager.playArrowMove(state.soundEnabled, state.hapticsEnabled)
            val updatedArrowsCleared = state.userProfile.totalArrowsCleared + 1
            _uiState.update {
                it.copy(
                    escapingArrowIds = it.escapingArrowIds + arrow.id,
                    hintedArrowId = if (it.hintedArrowId == arrow.id) null else it.hintedArrowId,
                    movesCount = it.movesCount + 1,
                    userProfile = it.userProfile.copy(totalArrowsCleared = updatedArrowsCleared),
                    dailyMissions = it.dailyMissions.map { m ->
                        if (m.id == 1 && m.current < m.target) m.copy(current = m.current + 1) else m
                    }
                )
            }
        } else {
            val newLives = state.remainingLives - 1
            val isDead = newLives <= 0

            if (isDead) {
                // User lost the game: play custom Loss the Game sound effect!
                soundManager.playLossGame(state.soundEnabled, state.hapticsEnabled)
            } else {
                soundManager.playBlocked(state.soundEnabled, state.hapticsEnabled)
            }

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

    fun onArrowAnimationFinished(arrow: Arrow) {
        val updatedArrows = _uiState.value.activeArrows.filter { it.id != arrow.id }
        val isComplete = updatedArrows.isEmpty()
        val state = _uiState.value

        val stars = if (isComplete) {
            calculateStars(state.remainingLives, state.timeRemainingSeconds, state.totalTimeSeconds)
        } else 0

        val updatedProfile = state.userProfile.copy(totalArrowsCleared = state.userProfile.totalArrowsCleared + 1)

        _uiState.update {
            it.copy(
                activeArrows = updatedArrows,
                escapingArrowIds = it.escapingArrowIds - arrow.id,
                isLevelComplete = isComplete,
                isTimerRunning = !isComplete,
                starsEarned = stars,
                userProfile = updatedProfile,
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

            if (state.isDailyChallenge) {
                // Daily challenge completed: reward bonus coins and arrows stats, do not alter regular campaign level!
                viewModelScope.launch {
                    val updatedCoins = state.userProfile.coins + 50
                    val updatedArrows = state.userProfile.totalArrowsCleared
                    repository.updatePlayerCoinsAndArrows(updatedCoins, updatedArrows)
                }
            } else {
                // Regular campaign level completed: save progress and unlock next level
                val completedLevelNumber = state.currentLevel.levelNumber
                val nextLvl = completedLevelNumber + 1
                viewModelScope.launch {
                    repository.saveLevelSuccess(completedLevelNumber, stars, timeTaken)
                    repository.saveCurrentLevel(nextLvl)
                    _uiState.update {
                        val newMax = maxOf(it.maxUnlockedLevel, nextLvl)
                        it.copy(
                            maxUnlockedLevel = newMax
                        )
                    }
                    // Background pre-fetch next level
                    withContext(Dispatchers.Default) {
                        LevelGenerator.getLevel(nextLvl)
                    }
                }
            }
            // Level completed: play custom Game Win sound effect!
            soundManager.playGameWin(state.soundEnabled, state.hapticsEnabled)
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
            gridHeight = state.currentLevel.gridHeight,
            shapeType = state.currentLevel.shapeType
        )

        if (freeArrows.isNotEmpty()) {
            // Pick the next most logical move: the arrow that unlocks the most downstream arrows
            val chosen = if (freeArrows.size == 1) {
                freeArrows.first()
            } else {
                freeArrows.maxByOrNull { candidate ->
                    val remainingAfter = state.activeArrows.filter { it.id != candidate.id && !state.escapingArrowIds.contains(it.id) }
                    ObstructionChecker.getFreeArrows(
                        activeArrows = remainingAfter,
                        gridWidth = state.currentLevel.gridWidth,
                        gridHeight = state.currentLevel.gridHeight,
                        shapeType = state.currentLevel.shapeType
                    ).size
                } ?: freeArrows.first()
            }
            soundManager.playHint(state.soundEnabled, state.hapticsEnabled)
            val hintsLeft = maxOf(0, state.availableHints - 1)
            _uiState.update {
                it.copy(
                    hintedArrowId = chosen.id,
                    availableHints = hintsLeft
                )
            }

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
                val updatedCoins = current.userProfile.coins + mission.rewardCoins
                val updatedProfile = current.userProfile.copy(coins = updatedCoins)

                // Persist new coin balance to Room
                viewModelScope.launch {
                    val state = repository.getUserGameStateSync()
                    if (state != null) {
                        repository.saveUserGameState(state.copy(totalCoins = updatedCoins))
                    }
                }

                current.copy(
                    userProfile = updatedProfile,
                    dailyMissions = current.dailyMissions.map {
                        if (it.id == missionId) it.copy(isClaimed = true) else it
                    }
                )
            } else current
        }
    }

    fun addLives() {
        soundManager.playClick(_uiState.value.soundEnabled, _uiState.value.hapticsEnabled)
        _uiState.update {
            it.copy(
                remainingLives = 1,
                isGameOver = false,
                gameOverReason = null,
                isTimerRunning = true
            )
        }
        startTimer()
    }

    fun clearSingleArrowWithEraser() {
        val state = _uiState.value
        if (state.isLevelComplete || state.isGameOver) return
        val freeArrows = ObstructionChecker.getFreeArrows(
            activeArrows = state.activeArrows.filter { !state.escapingArrowIds.contains(it.id) },
            gridWidth = state.currentLevel.gridWidth,
            gridHeight = state.currentLevel.gridHeight,
            shapeType = state.currentLevel.shapeType
        )
        val target = freeArrows.firstOrNull() ?: state.activeArrows.firstOrNull() ?: return
        soundManager.playEscape(state.soundEnabled, state.hapticsEnabled)
        val updatedArrowsCleared = state.userProfile.totalArrowsCleared + 1
        _uiState.update {
            it.copy(
                escapingArrowIds = it.escapingArrowIds + target.id,
                hintedArrowId = if (it.hintedArrowId == target.id) null else it.hintedArrowId,
                movesCount = it.movesCount + 1,
                userProfile = it.userProfile.copy(totalArrowsCleared = updatedArrowsCleared)
            )
        }
    }

    fun refreshDailyChallengeDate() {
        val today = DateHelper.getTodayDayOfMonth()
        if (_uiState.value.dailyChallengeDay != today) {
            _uiState.update { it.copy(dailyChallengeDay = today) }
        }
    }

    fun loadDailyChallenge(day: Int = DateHelper.getTodayDayOfMonth()) {
        timerJob?.cancel()
        hintJob?.cancel()

        val today = DateHelper.getTodayDayOfMonth()
        val safeDay = if (day > today) today else maxOf(1, day)
        viewModelScope.launch {
            val dailyLevel = withContext(Dispatchers.Default) {
                LevelGenerator.getDailyChallenge(safeDay)
            }
            _uiState.update {
                it.copy(
                    currentScreen = AppScreen.GAME,
                    currentLevel = dailyLevel,
                    isDailyChallenge = true,
                    dailyChallengeDay = safeDay,
                    activeArrows = dailyLevel.arrows,
                    totalArrowsCount = dailyLevel.arrows.size,
                    escapingArrowIds = emptySet(),
                    blockedArrowIds = emptySet(),
                    hintedArrowId = null,
                    remainingLives = 3,
                    timeRemainingSeconds = dailyLevel.timeLimitSeconds,
                    totalTimeSeconds = dailyLevel.timeLimitSeconds,
                    isTimerRunning = true,
                    isLevelComplete = false,
                    isGameOver = false,
                    gameOverReason = null,
                    movesCount = 0,
                    starsEarned = 0
                )
            }
            startTimer()
        }
    }

    fun restartCurrentLevel() {
        if (_uiState.value.isDailyChallenge) {
            loadDailyChallenge(_uiState.value.dailyChallengeDay)
        } else {
            loadLevel(_uiState.value.currentLevel.levelNumber)
        }
    }

    fun nextLevel() {
        if (_uiState.value.isDailyChallenge) {
            navigateTo(AppScreen.HOME)
        } else {
            val nextLvl = _uiState.value.currentLevel.levelNumber + 1
            loadLevel(nextLvl)
        }
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

    fun playClickSound() {
        soundManager.playClick(_uiState.value.soundEnabled, _uiState.value.hapticsEnabled)
    }

    /**
     * Triggered when quitting the game: plays user's custom Game Exit sound effect!
     */
    fun playGameExit() {
        soundManager.playGameExit(_uiState.value.soundEnabled, _uiState.value.hapticsEnabled)
    }

    private fun generateLeaderboard(
        completedLevels: Int,
        stars: Int,
        arrowsCleared: Int,
        profile: UserProfile
    ): List<LeaderboardEntry> {
        val userPoints = (completedLevels * 260) + (stars * 60) + (arrowsCleared * 8)
        val userEntry = LeaderboardEntry(
            rank = 1,
            name = "${profile.name} (You)",
            levelsCleared = completedLevels,
            stars = stars,
            points = userPoints,
            avatarEmoji = profile.avatarEmoji,
            isCurrentUser = true
        )

        val challengers = listOf(
            LeaderboardEntry(rank = 0, name = "GrandMaster_Kai", levelsCleared = 18, stars = 54, points = 4950, avatarEmoji = "👑"),
            LeaderboardEntry(rank = 0, name = "AuraArrow", levelsCleared = 15, stars = 45, points = 4120, avatarEmoji = "⚡"),
            LeaderboardEntry(rank = 0, name = "ZenLogician", levelsCleared = 12, stars = 36, points = 3380, avatarEmoji = "🧘"),
            LeaderboardEntry(rank = 0, name = "SwiftEscape", levelsCleared = 9, stars = 27, points = 2540, avatarEmoji = "🎯"),
            LeaderboardEntry(rank = 0, name = "PuzzleNinja_99", levelsCleared = 7, stars = 21, points = 1980, avatarEmoji = "🥷"),
            LeaderboardEntry(rank = 0, name = "PathExplorer", levelsCleared = 5, stars = 15, points = 1420, avatarEmoji = "🧭"),
            LeaderboardEntry(rank = 0, name = "MazeCrafter", levelsCleared = 3, stars = 9, points = 860, avatarEmoji = "🧩"),
            LeaderboardEntry(rank = 0, name = "ArrowRookie", levelsCleared = 2, stars = 6, points = 520, avatarEmoji = "🦊"),
            LeaderboardEntry(rank = 0, name = "LogicNovice", levelsCleared = 1, stars = 3, points = 280, avatarEmoji = "🌱")
        )

        val sorted = (challengers + userEntry).sortedWith(
            compareByDescending<LeaderboardEntry> { it.points }
                .thenByDescending { it.levelsCleared }
                .thenByDescending { it.stars }
        )

        return sorted.mapIndexed { idx, item -> item.copy(rank = idx + 1) }
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }
}
