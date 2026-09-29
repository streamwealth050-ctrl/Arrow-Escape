package com.example.domain.model

data class LevelData(
    val levelNumber: Int,
    val gridWidth: Int,
    val gridHeight: Int,
    val arrows: List<Arrow>,
    val timeLimitSeconds: Int,
    val difficulty: Difficulty = Difficulty.forLevel(levelNumber),
    val title: String = "Level $levelNumber"
)
