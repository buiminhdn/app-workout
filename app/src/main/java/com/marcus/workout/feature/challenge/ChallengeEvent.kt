package com.marcus.workout.feature.challenge

sealed class ChallengeEvent {
    data object TriggerHapticTick : ChallengeEvent()
    data object TriggerHapticSuccess : ChallengeEvent()
}
