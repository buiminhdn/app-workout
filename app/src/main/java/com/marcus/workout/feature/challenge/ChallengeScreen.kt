package com.marcus.workout.feature.challenge

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.marcus.workout.R
import com.marcus.workout.data.model.Challenge
import com.marcus.workout.data.model.Difficulty
import com.marcus.workout.feature.settings.SettingsSheet
import com.marcus.workout.ui.theme.AccentGlow
import com.marcus.workout.ui.theme.AccentLime
import com.marcus.workout.ui.theme.Background
import com.marcus.workout.ui.theme.Border
import com.marcus.workout.ui.theme.CardBackground
import com.marcus.workout.ui.theme.TextMuted
import com.marcus.workout.ui.theme.TextPrimary
import com.marcus.workout.ui.theme.TextSecondary
import com.marcus.workout.util.HapticUtil
import kotlinx.coroutines.flow.collectLatest

// Layout constants — 8pt grid aligned
private object AppDimensions {
    val HorizontalPadding = 20.dp
    val CardRadius = 22.dp
    val SectionSpacing = 28.dp
    val ButtonHeight = 56.dp
    val DifficultyPillRadius = 50.dp
}

@Composable
fun ChallengeScreen(viewModel: ChallengeViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val view = LocalView.current
    var showSettings by remember { mutableStateOf(false) }

    // Stable lambda references — no recomposition churn
    val onRoll = remember { { viewModel.onIntent(ChallengeIntent.RollChallenge) } }
    val onSelectDifficulty: (Difficulty) -> Unit = remember {
        { difficulty: Difficulty ->
            viewModel.onIntent(ChallengeIntent.SelectDifficulty(difficulty))
        }
    }

    // One-time event handler
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ChallengeEvent.TriggerHapticTick -> {
                    HapticUtil.performHapticClick(view)
                }
                is ChallengeEvent.TriggerHapticSuccess -> {
                    HapticUtil.performHapticSuccess(view)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        // Subtle gradient overlay for depth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                AccentGlow.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            center = Offset(size.width * 0.5f, size.height * 0.3f),
                            radius = size.width * 0.8f
                        )
                    )
                }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = AppDimensions.HorizontalPadding,
                    end = AppDimensions.HorizontalPadding,
                    top = 32.dp,
                    bottom = 24.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Title + Settings button
            AppHeader(onSettingsClick = { showSettings = true })

            Spacer(modifier = Modifier.height(AppDimensions.SectionSpacing))

            // Difficulty Selector
            DifficultySelector(
                selected = uiState.selectedDifficulty,
                onSelect = onSelectDifficulty
            )

            Spacer(modifier = Modifier.height(AppDimensions.SectionSpacing + 8.dp))

            // Challenge Card
            AnimatedChallengeCard(
                currentChallenge = uiState.currentChallenge,
                rollingChallenge = uiState.rollingChallenge,
                isRolling = uiState.isRolling
            )

            Spacer(modifier = Modifier.height(AppDimensions.SectionSpacing))

            // Roll Button
            RollButton(
                onClick = onRoll,
                isRolling = uiState.isRolling
            )
        }
    }

    // Settings bottom sheet
    if (showSettings) {
        SettingsSheet(onDismiss = { showSettings = false })
    }
}

// ─── Header ──────────────────────────────────────────────────────────────────

@Composable
private fun AppHeader(onSettingsClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        // Centered title
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.app_title),
                style = MaterialTheme.typography.displayLarge,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.app_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp
            )
        }

        // Settings button — top end
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(44.dp)
                .clip(CircleShape)
                .background(CardBackground)
                .border(1.5.dp, AccentLime.copy(alpha = 0.4f), CircleShape)
                .clickable(onClick = onSettingsClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⚙",
                fontSize = 20.sp,
                color = AccentLime
            )
        }
    }
}

// ─── Difficulty Selector ─────────────────────────────────────────────────────

@Composable
private fun DifficultySelector(
    selected: Difficulty,
    onSelect: (Difficulty) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppDimensions.DifficultyPillRadius))
            .background(CardBackground)
            .border(
                width = 1.dp,
                color = Border,
                shape = RoundedCornerShape(AppDimensions.DifficultyPillRadius)
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Difficulty.entries.forEach { difficulty ->
            DifficultyPill(
                difficulty = difficulty,
                isSelected = difficulty == selected,
                onClick = { onSelect(difficulty) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DifficultyPill(
    difficulty: Difficulty,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "pillScale"
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) AccentLime else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "pillBg"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.Black else TextSecondary,
        animationSpec = tween(durationMillis = 200),
        label = "pillText"
    )

    val label = when (difficulty) {
        Difficulty.EASY -> stringResource(R.string.difficulty_easy)
        Difficulty.MEDIUM -> stringResource(R.string.difficulty_medium)
        Difficulty.HARD -> stringResource(R.string.difficulty_hard)
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(AppDimensions.DifficultyPillRadius))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
        )
    }
}

// ─── Challenge Card with Rolling Animation ───────────────────────────────────

@Composable
private fun AnimatedChallengeCard(
    currentChallenge: Challenge?,
    rollingChallenge: Challenge?,
    isRolling: Boolean
) {
    val displayChallenge = if (isRolling) rollingChallenge else currentChallenge
    val hasContent = displayChallenge != null

    val cardScale by animateFloatAsState(
        targetValue = when {
            isRolling -> 0.97f
            hasContent -> 1f
            else -> 0.88f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cardScale"
    )

    val cardAlpha by animateFloatAsState(
        targetValue = if (hasContent || isRolling) 1f else 0.5f,
        animationSpec = tween(durationMillis = 200),
        label = "cardAlpha"
    )

    val glowIntensity by animateFloatAsState(
        targetValue = when {
            isRolling -> 0.35f
            hasContent -> 0.15f
            else -> 0f
        },
        animationSpec = if (isRolling) tween(150) else tween(500),
        label = "glowIntensity"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "rollingPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isRolling -> AccentLime.copy(alpha = 0.5f)
            hasContent -> AccentLime.copy(alpha = 0.2f)
            else -> Border
        },
        animationSpec = tween(durationMillis = 200),
        label = "borderColor"
    )

    val effectiveGlow = if (isRolling) glowIntensity * pulseAlpha else glowIntensity

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
                alpha = cardAlpha
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (hasContent || isRolling) 20.dp else 0.dp,
                    shape = RoundedCornerShape(AppDimensions.CardRadius),
                    ambientColor = AccentGlow,
                    spotColor = AccentGlow
                )
                .clip(RoundedCornerShape(AppDimensions.CardRadius))
                .background(CardBackground)
                .border(
                    width = if (isRolling) 1.5.dp else 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(AppDimensions.CardRadius)
                )
                .drawBehind {
                    if (effectiveGlow > 0f) {
                        drawRoundRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    AccentGlow.copy(alpha = effectiveGlow),
                                    Color.Transparent
                                ),
                                center = Offset(size.width / 2f, size.height * 0.3f),
                                radius = size.width * 0.7f
                            ),
                            cornerRadius = CornerRadius(
                                AppDimensions.CardRadius.toPx(),
                                AppDimensions.CardRadius.toPx()
                            )
                        )
                    }
                }
                .padding(horizontal = 24.dp, vertical = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                !isRolling && currentChallenge == null -> IdleCardContent()
                isRolling && rollingChallenge != null -> RollingCardContent(challenge = rollingChallenge)
                isRolling -> RollingPlaceholder()
                currentChallenge != null -> LandedCardContent(challenge = currentChallenge)
            }
        }
    }
}

@Composable
private fun IdleCardContent() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "⚡", fontSize = 40.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.card_idle_title),
            style = MaterialTheme.typography.headlineMedium,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.card_idle_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun RollingPlaceholder() {
    Text(
        text = "⚡",
        fontSize = 40.sp,
        modifier = Modifier.graphicsLayer { alpha = 0.5f }
    )
}

@Composable
private fun RollingCardContent(challenge: Challenge) {
    AnimatedContent(
        targetState = challenge.id,
        transitionSpec = {
            (fadeIn(animationSpec = tween(80)) +
                    scaleIn(initialScale = 0.92f, animationSpec = tween(80)))
                .togetherWith(
                    fadeOut(animationSpec = tween(60)) +
                            scaleOut(targetScale = 1.06f, animationSpec = tween(60))
                )
        },
        contentAlignment = Alignment.Center,
        label = "rollingContent"
    ) { _ ->
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(challenge.labelResId),
                style = MaterialTheme.typography.displayLarge,
                color = AccentLime.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(challenge.descriptionResId),
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LandedCardContent(challenge: Challenge) {
    val revealScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "revealScale"
    )

    val revealAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 400),
        label = "revealAlpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.graphicsLayer {
            scaleX = revealScale
            scaleY = revealScale
            alpha = revealAlpha
        }
    ) {
        challenge.durationSeconds?.let {
            Text(
                text = stringResource(R.string.timed_challenge_label),
                style = MaterialTheme.typography.labelSmall,
                color = AccentLime,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Text(
            text = stringResource(challenge.labelResId),
            style = MaterialTheme.typography.displayLarge,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(challenge.descriptionResId),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        challenge.durationSeconds?.let { seconds ->
            Spacer(modifier = Modifier.height(16.dp))
            DurationBadge(seconds = seconds)
        }
    }
}

@Composable
private fun DurationBadge(seconds: Int) {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    val timeText = if (remainingSeconds == 0) {
        stringResource(R.string.duration_minutes, minutes)
    } else {
        stringResource(R.string.duration_seconds, seconds)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(AppDimensions.DifficultyPillRadius))
            .background(AccentLime.copy(alpha = 0.1f))
            .border(
                width = 1.dp,
                color = AccentLime.copy(alpha = 0.3f),
                shape = RoundedCornerShape(AppDimensions.DifficultyPillRadius)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "⏱ $timeText",
            style = MaterialTheme.typography.labelSmall,
            color = AccentLime,
            fontWeight = FontWeight.Bold
        )
    }
}

// ─── Roll Button ─────────────────────────────────────────────────────────────

@Composable
private fun RollButton(onClick: () -> Unit, isRolling: Boolean) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            isRolling -> 0.94f
            isPressed -> 0.96f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "btnScale"
    )

    val buttonText = if (isRolling) {
        stringResource(R.string.rolling_button)
    } else {
        stringResource(R.string.roll_button)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "btnPulse")
    val glowElevation by infiniteTransition.animateFloat(
        initialValue = 16f,
        targetValue = 32f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowElevation"
    )

    val elevation = if (isRolling) glowElevation.dp else 24.dp

    Button(
        onClick = onClick,
        enabled = !isRolling,
        modifier = Modifier
            .fillMaxWidth()
            .height(AppDimensions.ButtonHeight)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(50),
                ambientColor = AccentGlow,
                spotColor = AccentGlow
            ),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = AccentLime,
            contentColor = Color.Black,
            disabledContainerColor = AccentLime.copy(alpha = 0.7f),
            disabledContentColor = Color.Black.copy(alpha = 0.7f)
        ),
        interactionSource = interactionSource
    ) {
        Text(
            text = buttonText,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.5.sp
        )
    }
}
