package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.domain.model.Arrow
import com.example.ui.viewmodel.GameUiState

@Composable
fun GameBoard(
    uiState: GameUiState,
    onArrowTapped: (Arrow) -> Unit,
    onArrowAnimationFinished: (Arrow) -> Unit,
    modifier: Modifier = Modifier
) {
    ArrowPuzzleBoard(
        uiState = uiState,
        onArrowTapped = onArrowTapped,
        onArrowAnimationFinished = onArrowAnimationFinished,
        modifier = modifier
    )
}
