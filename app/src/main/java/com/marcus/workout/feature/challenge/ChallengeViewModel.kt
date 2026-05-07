package com.marcus.workout.feature.challenge

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChallengeViewModel(
    private val repository: ChallengeRepository = ChallengeRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChallengeUiState())
    val uiState: StateFlow<ChallengeUiState> = _uiState.asStateFlow()

    private val _events = Channel<ChallengeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var isRollingInProgress = false

    fun onIntent(intent: ChallengeIntent) {
        when (intent) {
            is ChallengeIntent.SelectDifficulty -> {
                _uiState.update { it.copy(selectedDifficulty = intent.difficulty) }
            }
            is ChallengeIntent.RollChallenge -> startRolling()
        }
    }

    private fun startRolling() {
        if (isRollingInProgress) return
        isRollingInProgress = true

        viewModelScope.launch {
            val difficulty = _uiState.value.selectedDifficulty

            // Enter rolling state — clear current challenge
            _uiState.update {
                it.copy(isRolling = true, currentChallenge = null, rollingChallenge = null)
            }

            // Slot-machine cycling: ~12 rapid swaps with decelerating intervals
            val tickCount = 12
            for (i in 0 until tickCount) {
                val candidate = repository.getRandom(difficulty)
                _uiState.update { it.copy(rollingChallenge = candidate) }

                // Haptic tick on each cycle
                _events.send(ChallengeEvent.TriggerHapticTick)

                // Decelerate: starts fast (50ms), ends slow (~150ms)
                val progress = i.toFloat() / tickCount
                val intervalMs = (50 + (100 * progress * progress)).toLong()
                delay(intervalMs)
            }

            // Final challenge — the one that sticks
            val finalChallenge = repository.getRandom(difficulty)
            _uiState.update {
                it.copy(
                    currentChallenge = finalChallenge,
                    rollingChallenge = null,
                    isRolling = false
                )
            }

            // Strong haptic confirm on landing
            _events.send(ChallengeEvent.TriggerHapticSuccess)
            isRollingInProgress = false
        }
    }
}
