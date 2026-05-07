package com.marcus.workout.feature.challenge

import com.marcus.workout.R
import com.marcus.workout.data.model.Challenge
import com.marcus.workout.data.model.Difficulty

class ChallengeRepository {

    private val challenges = listOf(
        // Easy
        Challenge(1, R.string.challenge_1_label, R.string.challenge_1_desc, Difficulty.EASY, null),
        Challenge(2, R.string.challenge_2_label, R.string.challenge_2_desc, Difficulty.EASY, null),
        Challenge(3, R.string.challenge_3_label, R.string.challenge_3_desc, Difficulty.EASY, null),
        Challenge(4, R.string.challenge_4_label, R.string.challenge_4_desc, Difficulty.EASY, 60),
        Challenge(5, R.string.challenge_5_label, R.string.challenge_5_desc, Difficulty.EASY, null),

        // Medium
        Challenge(6, R.string.challenge_6_label, R.string.challenge_6_desc, Difficulty.MEDIUM, null),
        Challenge(7, R.string.challenge_7_label, R.string.challenge_7_desc, Difficulty.MEDIUM, 60),
        Challenge(8, R.string.challenge_8_label, R.string.challenge_8_desc, Difficulty.MEDIUM, null),
        Challenge(9, R.string.challenge_9_label, R.string.challenge_9_desc, Difficulty.MEDIUM, null),
        Challenge(10, R.string.challenge_10_label, R.string.challenge_10_desc, Difficulty.MEDIUM, null),

        // Hard
        Challenge(11, R.string.challenge_11_label, R.string.challenge_11_desc, Difficulty.HARD, null),
        Challenge(12, R.string.challenge_12_label, R.string.challenge_12_desc, Difficulty.HARD, null),
        Challenge(13, R.string.challenge_13_label, R.string.challenge_13_desc, Difficulty.HARD, 120),
        Challenge(14, R.string.challenge_14_label, R.string.challenge_14_desc, Difficulty.HARD, null),
        Challenge(15, R.string.challenge_15_label, R.string.challenge_15_desc, Difficulty.HARD, null)
    )

    fun getRandom(difficulty: Difficulty): Challenge {
        return challenges.filter { it.difficulty == difficulty }.random()
    }
}
