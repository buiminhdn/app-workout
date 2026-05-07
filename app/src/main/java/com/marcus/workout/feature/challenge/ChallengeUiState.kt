package com.marcus.workout.feature.challenge

import com.marcus.workout.data.model.Challenge
import com.marcus.workout.data.model.Difficulty

data class ChallengeUiState(
    val selectedDifficulty: Difficulty = Difficulty.EASY,
    val currentChallenge: Challenge? = null,
    val rollingChallenge: Challenge? = null,
    val isRolling: Boolean = false
)
