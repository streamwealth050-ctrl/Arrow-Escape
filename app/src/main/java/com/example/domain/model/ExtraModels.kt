package com.example.domain.model

enum class LoginType {
    GUEST,
    GOOGLE,
    FACEBOOK
}

data class UserProfile(
    val id: String = "guest_1",
    val name: String = "Guest Player #342",
    val loginType: LoginType = LoginType.GUEST,
    val avatarEmoji: String = "🐹",
    val coins: Int = 250,
    val hintsCount: Int = 3,
    val streakDays: Int = 3,
    val totalArrowsCleared: Int = 42
)

data class DailyMission(
    val id: Int,
    val title: String,
    val description: String,
    val target: Int,
    val current: Int,
    val rewardCoins: Int,
    val isClaimed: Boolean = false,
    val iconEmoji: String = "🎯"
) {
    val isComplete: Boolean get() = current >= target
    val progressFraction: Float get() = (current.toFloat() / target).coerceIn(0f, 1f)
}

data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val levelsCleared: Int,
    val stars: Int,
    val avatarEmoji: String,
    val isCurrentUser: Boolean = false
)
