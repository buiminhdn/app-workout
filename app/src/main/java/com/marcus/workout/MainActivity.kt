package com.marcus.workout

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.marcus.workout.feature.challenge.ChallengeScreen
import com.marcus.workout.feature.challenge.ChallengeViewModel
import com.marcus.workout.ui.theme.RandomWorkoutTheme
import com.marcus.workout.util.LocaleHelper

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Ensure English is the default before first frame
        LocaleHelper.ensureDefaultLocale()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // No delay. No loading state. Render immediately.
        setContent {
            RandomWorkoutTheme {
                ChallengeScreen(viewModel = viewModel<ChallengeViewModel>())
            }
        }
    }
}