package com.marcus.workout.data.model

import androidx.annotation.StringRes

data class Challenge(
    val id: Int,
    @param:StringRes val labelResId: Int,
    @param:StringRes val descriptionResId: Int,
    val difficulty: Difficulty,
    val durationSeconds: Int?
)
