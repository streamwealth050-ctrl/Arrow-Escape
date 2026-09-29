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
    EASY("Easy", SuccessGreen, 75),
    NORMAL("Normal", SkyBlue600, 60),
    HARD("Hard", GoldAmber, 50),
    VERY_HARD("Very Hard", HeartRed, 45),
    EXPERT("Expert", PurpleExpert, 40);

    companion object {
        fun forLevel(level: Int): Difficulty {
            return when {
                level <= 10 -> EASY
                level <= 25 -> NORMAL
                level <= 50 -> HARD
                level <= 100 -> VERY_HARD
                else -> EXPERT
            }
        }
    }
}
