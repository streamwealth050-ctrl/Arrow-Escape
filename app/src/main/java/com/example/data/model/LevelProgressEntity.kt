package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_progress")
data class LevelProgressEntity(
    @PrimaryKey
    val levelNumber: Int,
    val completed: Boolean = false,
    val stars: Int = 0,
    val bestTimeSeconds: Int = 0,
    val completedAt: Long = System.currentTimeMillis()
)
