package com.example.domain.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.GoldAmber
import com.example.ui.theme.HeartRed
import com.example.ui.theme.PurpleExpert
import com.example.ui.theme.SkyBlue600
import com.example.ui.theme.SuccessGreen

enum class Difficulty(
    val label: String,
    val badgeColor: Color,
    val timeLimitSec: Int
) {
    EASY("Easy", SuccessGreen, 120),
    MEDIUM("Medium", SkyBlue600, 160),
    HARD("Hard", GoldAmber, 220),
    VERY_HARD("Very Hard", HeartRed, 280),
    EXPERT("Expert", PurpleExpert, 360);

    companion object {
        // Backwards compatibility alias
        val NORMAL: Difficulty get() = MEDIUM

        fun forLevel(level: Int): Difficulty {
            return when {
                level <= 5 -> EASY
                level <= 10 -> MEDIUM
                level <= 20 -> HARD
                level <= 35 -> VERY_HARD
                else -> EXPERT
            }
        }
    }
}
