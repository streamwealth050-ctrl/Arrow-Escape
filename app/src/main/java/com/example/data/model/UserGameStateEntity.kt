package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_game_state")
data class UserGameStateEntity(
    @PrimaryKey
    val id: Int = 1,
    val currentLevelNumber: Int = 1,
    val maxUnlockedLevel: Int = 1,
    val totalCoins: Int = 250,
    val streakDays: Int = 3,
    val playerName: String = "Guest Player #342",
    val loginType: String = "GUEST",
    val avatarEmoji: String = "🐹",
    val totalArrowsCleared: Int = 0,
    val isAuthenticated: Boolean = false,
    val lastPlayedTimestamp: Long = System.currentTimeMillis()
)
