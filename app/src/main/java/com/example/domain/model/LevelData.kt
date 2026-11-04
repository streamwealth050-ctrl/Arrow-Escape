package com.example.domain.model

enum class ShapeType(val label: String, val iconEmoji: String) {
    RECTANGLE("Classic", "⬛"),
    HEART("Heart", "❤️"),
    TRIANGLE("Triangle", "🔺"),
    DIAMOND("Diamond", "🔷"),
    CROSS("Cross", "➕"),
    STAR("Star", "⭐"),
    SHIELD("Shield", "🛡️")
}

data class LevelData(
    val levelNumber: Int,
    val gridWidth: Int,
    val gridHeight: Int,
    val arrows: List<Arrow>,
    val timeLimitSeconds: Int,
    val difficulty: Difficulty = Difficulty.forLevel(levelNumber),
    val title: String = "Level $levelNumber",
    val shapeType: ShapeType = ShapeType.RECTANGLE
)
