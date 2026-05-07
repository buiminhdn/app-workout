package com.marcus.workout.feature.challenge

import com.marcus.workout.data.model.Difficulty

sealed class ChallengeIntent {
    data class SelectDifficulty(val difficulty: Difficulty) : ChallengeIntent()
    data object RollChallenge : ChallengeIntent()
}
