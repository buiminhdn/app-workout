package com.marcus.workout.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CyberFitColorScheme = darkColorScheme(
    background = Background,
    surface = Surface,
    primary = AccentLime,
    onPrimary = Color.Black,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = ErrorRed
)

@Composable
fun RandomWorkoutTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CyberFitColorScheme,
        typography = AppTypography,
        content = content
    )
}