# Random Challenge Workout — Project Overview

> A dark-mode-first, cyber-fitness Android app built with Kotlin, Jetpack Compose, MVI, and Material 3. Roll a random workout challenge. No excuses.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Features](#2-features)
3. [UI Screens](#3-ui-screens)
4. [App Flow](#4-app-flow)
5. [MVI Architecture Explanation](#5-mvi-architecture-explanation)
6. [Folder Structure](#6-folder-structure)
7. [State / Intent / Event Examples](#7-state--intent--event-examples)
8. [Data Models](#8-data-models)
9. [Challenge Randomization Logic](#9-challenge-randomization-logic)
10. [UI/UX Design System](#10-uiux-design-system)
11. [Dark Mode Support](#11-dark-mode-support)
12. [Recommended Libraries](#12-recommended-libraries)
13. [Performance Best Practices](#13-performance-best-practices)
14. [Clean Code Rules](#14-clean-code-rules)
15. [Naming Conventions](#15-naming-conventions)
16. [Release Checklist](#16-release-checklist)

---

## 1. Project Overview

**Random Challenge Workout** is a minimalist Android fitness app that generates a random workout challenge based on a selected difficulty level. The experience is fast, direct, and visually energetic — built for users who want a quick workout prompt without navigating complex menus.

| Property | Value |
|---|---|
| Platform | Android |
| Language | Kotlin |
| UI Framework | Jetpack Compose |
| Architecture | MVI (Model–View–Intent) |
| Design System | Material 3 + Custom Cyber-Fitness Theme |
| Minimum SDK | API 26 (Android 8.0) |
| Target SDK | API 35 |

---

## 2. Features

- Select a difficulty level: **Easy**, **Medium**, or **Hard**
- Press **Roll Challenge** to get a randomly generated workout
- Animated challenge reveal with neon-glow card display
- Haptic feedback on roll action
- Fully offline — no internet required
- Dark mode first, no light mode toggle needed
- Smooth, fast, no loading screens

---

## 3. UI Screens

### Screen 1 — Home / Challenge Screen

This is the only screen in the app. It contains:

- App title / logo at the top
- Difficulty selector (Easy / Medium / Hard) as segmented pill tabs
- A large challenge display card (empty or showing the current challenge)
- A **Roll Challenge** CTA button at the bottom
- Subtle animated background or gradient

### No other screens are needed at v1.

---

## 4. App Flow

```
App Launch
    │
    ▼
Home Screen loads
    │
    ▼
Default difficulty = Easy (preselected)
Challenge card = empty / idle state
    │
    ▼
User selects difficulty (Easy / Medium / Hard)
    │
    ▼
User taps "Roll Challenge"
    │
    ▼
ViewModel receives RollChallenge intent
    │
    ▼
Random challenge picked from filtered pool
    │
    ▼
UI state updates → challenge card animates in
    │
    ▼
Haptic feedback fires
    │
    ▼
User reads challenge → taps again to reroll
```

---

## 5. MVI Architecture Explanation

MVI stands for **Model–View–Intent**. It enforces a strict unidirectional data flow which makes the UI predictable and easy to test.

### How it works in this app

```
[UI / Composable]
      │
      │  fires Intent (user action)
      ▼
[ViewModel]
      │
      │  processes Intent → updates UiState
      ▼
[StateFlow<UiState>]
      │
      │  collected by Composable
      ▼
[UI re-renders based on new state]
```

### The three pieces

**Intent** — what the user wants to do. Sealed class. Examples: select a difficulty, roll a challenge.

**UiState** — a single immutable data class representing everything the screen needs to render. No logic lives here.

**Event** — one-time side effects that should not survive recomposition. Examples: trigger haptic feedback, show a snackbar.

### Why MVI fits here

- Single screen, clear state transitions
- Easy to add new challenge types without restructuring
- Testable ViewModel logic with no UI dependency
- Compose's `collectAsStateWithLifecycle` maps directly to this pattern

---

## 6. Folder Structure

Feature-based package structure. All code for a feature lives together.

```
com.yourname.randomworkout
│
├── MainActivity.kt                  # Single activity entry point
│
├── ui/
│   └── theme/
│       ├── Color.kt                 # Cyber-fitness color palette
│       ├── Theme.kt                 # MaterialTheme override
│       └── Type.kt                  # Typography scale
│
├── feature/
│   └── challenge/
│       ├── ChallengeScreen.kt       # Composable UI
│       ├── ChallengeViewModel.kt    # ViewModel with StateFlow
│       ├── ChallengeIntent.kt       # Sealed class — user actions
│       ├── ChallengeUiState.kt      # Immutable UI state data class
│       ├── ChallengeEvent.kt        # Sealed class — one-time events
│       └── ChallengeRepository.kt   # Challenge data source
│
├── data/
│   └── model/
│       ├── Challenge.kt             # Core data model
│       └── Difficulty.kt            # Enum for difficulty levels
│
└── util/
    └── HapticUtil.kt                # Haptic feedback helper
```

---

## 7. State / Intent / Event Examples

### ChallengeUiState.kt

```kotlin
data class ChallengeUiState(
    val selectedDifficulty: Difficulty = Difficulty.EASY,
    val currentChallenge: Challenge? = null,
    val isRolling: Boolean = false
)
```

### ChallengeIntent.kt

```kotlin
sealed class ChallengeIntent {
    data class SelectDifficulty(val difficulty: Difficulty) : ChallengeIntent()
    object RollChallenge : ChallengeIntent()
}
```

### ChallengeEvent.kt

```kotlin
sealed class ChallengeEvent {
    object TriggerHaptic : ChallengeEvent()
}
```

### ChallengeViewModel.kt (core logic)

```kotlin
class ChallengeViewModel(
    private val repository: ChallengeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChallengeUiState())
    val uiState: StateFlow<ChallengeUiState> = _uiState.asStateFlow()

    private val _events = Channel<ChallengeEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onIntent(intent: ChallengeIntent) {
        when (intent) {
            is ChallengeIntent.SelectDifficulty -> {
                _uiState.update { it.copy(selectedDifficulty = intent.difficulty) }
            }
            is ChallengeIntent.RollChallenge -> {
                val challenge = repository.getRandom(_uiState.value.selectedDifficulty)
                _uiState.update { it.copy(currentChallenge = challenge, isRolling = false) }
                viewModelScope.launch { _events.send(ChallengeEvent.TriggerHaptic) }
            }
        }
    }
}
```

---

## 8. Data Models

### Difficulty.kt

```kotlin
enum class Difficulty {
    EASY, MEDIUM, HARD
}
```

### Challenge.kt

```kotlin
data class Challenge(
    val id: Int,
    val label: String,          // e.g. "20 Push-ups"
    val description: String,    // e.g. "Standard push-up position"
    val difficulty: Difficulty,
    val durationSeconds: Int?   // null for rep-based challenges
)
```

---

## 9. Challenge Randomization Logic

All challenges are stored in memory as a hardcoded list inside `ChallengeRepository`. No database is needed at v1.

### ChallengeRepository.kt

```kotlin
class ChallengeRepository {

    private val challenges = listOf(
        // Easy
        Challenge(1, "20 Push-ups", "Standard push-up position, full range", Difficulty.EASY, null),
        Challenge(2, "30 Squats", "Feet shoulder-width, go below parallel", Difficulty.EASY, null),
        Challenge(3, "40 Jumping Jacks", "Full extension on each rep", Difficulty.EASY, null),
        Challenge(4, "1 Minute Wall Sit", "Back flat, thighs parallel to floor", Difficulty.EASY, 60),
        Challenge(5, "20 Lunges", "Alternating legs, knee just off the floor", Difficulty.EASY, null),

        // Medium
        Challenge(6, "15 Burpees", "Chest to floor, jump at the top", Difficulty.MEDIUM, null),
        Challenge(7, "1 Minute Plank", "Elbows under shoulders, core tight", Difficulty.MEDIUM, 60),
        Challenge(8, "10 Diamond Push-ups", "Index fingers and thumbs touching", Difficulty.MEDIUM, null),
        Challenge(9, "25 Pike Push-ups", "Hips high, target shoulders", Difficulty.MEDIUM, null),
        Challenge(10, "30 Mountain Climbers", "Drive knees to chest, keep hips low", Difficulty.MEDIUM, null),

        // Hard
        Challenge(11, "20 Clap Push-ups", "Explosive push, clap mid-air", Difficulty.HARD, null),
        Challenge(12, "10 Handstand Push-ups", "Wall-supported, full range", Difficulty.HARD, null),
        Challenge(13, "2 Minute Plank", "Maintain perfect form throughout", Difficulty.HARD, 120),
        Challenge(14, "30 Jump Squats", "Maximum height every rep", Difficulty.HARD, null),
        Challenge(15, "20 Archer Push-ups", "One arm carries the load", Difficulty.HARD, null)
    )

    fun getRandom(difficulty: Difficulty): Challenge {
        return challenges.filter { it.difficulty == difficulty }.random()
    }
}
```

---

## 10. UI/UX Design System

This is the official design system for the app. All UI decisions should reference this section.

---

### Visual Identity

The app uses a **Cyber-Fitness** aesthetic inspired by gaming UI, cyberpunk design, premium subscription apps, and anime visual culture.

The experience should feel: **immersive, energetic, futuristic, premium.**

---

### Color System

#### Primary Background Colors

| Role | Hex | Usage |
|---|---|---|
| Background | `#050505` | Root screen background |
| Surface | `#111111` | Bottom sheets, dialogs |
| Card Background | `#1A1A1A` | Challenge card, selector pills |
| Border | `rgba(255,255,255,0.06)` | Subtle card outlines |

#### Accent Colors

| Role | Hex | Usage |
|---|---|---|
| Primary Accent | `#D7FF2F` | CTA button, selected state, glow |
| Accent Hover | `#C6F11E` | Button press state |
| Glow Effect | `rgba(215,255,47,0.35)` | Shadow on accent elements |

**Why neon lime works as the primary accent:**
- Maximum contrast against near-black backgrounds
- Psychologically energetic — triggers alertness
- Associated with gaming HUDs and gamification reward moments
- Rare enough in fitness apps to feel distinctive and premium

#### Text Colors

| Role | Hex | Usage |
|---|---|---|
| Primary Text | `#FFFFFF` | Challenge label, headings |
| Secondary Text | `#B3B3B3` | Descriptions, hints |
| Muted Text | `#707070` | Captions, disabled states |

#### Semantic Colors

| Purpose | Color | Hex |
|---|---|---|
| Success / Active | Neon Lime | `#D7FF2F` |
| Error | Hot Pink | `#FF4D67` |
| Warning | Amber | `#FFC857` |

---

### Color Palette Implementation (Kotlin)

```kotlin
// ui/theme/Color.kt

val Background = Color(0xFF050505)
val Surface = Color(0xFF111111)
val CardBackground = Color(0xFF1A1A1A)
val Border = Color(0x0FFFFFFF)

val AccentLime = Color(0xFFD7FF2F)
val AccentLimeHover = Color(0xFFC6F11E)
val AccentGlow = Color(0x59D7FF2F)

val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB3B3B3)
val TextMuted = Color(0xFF707070)

val ErrorRed = Color(0xFFFF4D67)
val WarningAmber = Color(0xFFFFC857)
```

---

### Typography System

#### Recommended Fonts

Prefer **Inter** (free, widely used, excellent on Android). Fall back to the system default sans-serif.

```kotlin
// ui/theme/Type.kt

val AppTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp
    )
)
```

#### Typography Scale

| Role | Size | Weight | Usage |
|---|---|---|---|
| Screen Title | 28–34sp | Bold | App name, screen heading |
| Section Header | 18–22sp | SemiBold | Difficulty label, card heading |
| Card Title | 14–16sp | Medium/Bold | Challenge label text |
| Caption | 11–12sp | Medium | Descriptions, helper text |

---

### Layout System

All spacing uses an **8pt grid**.

| Token | Value |
|---|---|
| xs | 4.dp |
| sm | 8.dp |
| md | 12.dp |
| lg | 16.dp |
| xl | 24.dp |
| xxl | 32.dp |

#### Core Layout Values

```kotlin
object AppDimensions {
    val HorizontalPadding = 20.dp
    val CardRadius = 22.dp
    val SectionSpacing = 28.dp
    val ButtonHeight = 56.dp
    val DifficultyPillRadius = 50.dp
}
```

#### Layout Principles

- Large horizontal padding creates breathing room and a premium feel
- Cards float on the background — no full-bleed elements
- Vertically stacked content, no horizontal scroll at v1
- Generous vertical spacing between sections

---

### Compose Component Design

#### Challenge Card

```kotlin
@Composable
fun ChallengeCard(challenge: Challenge?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppDimensions.CardRadius))
            .background(CardBackground)
            .border(
                width = 1.dp,
                color = Border,
                shape = RoundedCornerShape(AppDimensions.CardRadius)
            )
            .padding(horizontal = 24.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        if (challenge == null) {
            Text(
                text = "Roll to get your challenge",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = challenge.label,
                    style = MaterialTheme.typography.displayLarge,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = challenge.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
```

#### Roll Button with Neon Glow

```kotlin
@Composable
fun RollButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(AppDimensions.ButtonHeight)
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(50),
                ambientColor = AccentGlow,
                spotColor = AccentGlow
            ),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = AccentLime,
            contentColor = Color.Black
        )
    ) {
        Text(
            text = "ROLL CHALLENGE",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.5.sp
        )
    }
}
```

---

### Animation Design — Roll Effect

When the user taps **Roll Challenge**, the challenge card should animate in with a satisfying reveal. Recommended approach:

```kotlin
// Fade + scale in animation on challenge reveal
val visible = challenge != null
val scale by animateFloatAsState(
    targetValue = if (visible) 1f else 0.9f,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
)
val alpha by animateFloatAsState(
    targetValue = if (visible) 1f else 0f,
    animationSpec = tween(durationMillis = 300)
)

Box(
    modifier = Modifier
        .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha)
) {
    ChallengeCard(challenge = challenge)
}
```

Additional animation ideas:
- Number/text counter spin effect before settling on the challenge
- Card flip (3D Y-axis rotation) to reveal the new challenge
- Neon pulse on the accent button when idle (subtle breathing glow loop)

---

### Neon Glow Effect Implementation

Use `Modifier.shadow()` with custom ambient and spot colors to create a neon glow:

```kotlin
Modifier.shadow(
    elevation = 20.dp,
    shape = RoundedCornerShape(AppDimensions.CardRadius),
    ambientColor = AccentGlow,    // rgba(215,255,47,0.35)
    spotColor = AccentGlow
)
```

For a stronger glow on important elements, layer a semi-transparent colored `Box` behind the element using `BlendMode.Screen` or use `drawBehind` with a radial gradient.

---

### Cyberpunk-Inspired Interaction Feedback

- Tapping a difficulty pill triggers a **subtle scale animation** (0.95 → 1.0 spring)
- The Roll button pulses with a **neon breathing animation** when no challenge is shown
- Challenge card appears with **spring-based scale + fade** — never a plain instant swap
- Difficulty pill border glows with accent color when selected

---

### Haptic Feedback

```kotlin
// util/HapticUtil.kt

fun performHapticClick(view: View) {
    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
}

fun performHapticSuccess(view: View) {
    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
}
```

Use `CONFIRM` haptic on challenge roll for a satisfying success-moment feedback. Use `VIRTUAL_KEY` for difficulty selection taps.

---

### Motion Design Principles

- All animations use spring physics, not linear or ease curves
- Duration for most transitions: 250–350ms
- Never animate two things simultaneously unless intentional (e.g. scale + fade together)
- Avoid abrupt snaps — every state change has a transition

---

### Accessibility Considerations

- All interactive elements have a minimum touch target of 48x48dp
- Accent lime (`#D7FF2F`) on black background passes WCAG AA at large text sizes
- Add `contentDescription` to all icon buttons and interactive cards
- Support dynamic font scaling — test at 200% font size
- Do not rely solely on color to communicate state (use icons or text labels alongside)

---

### Reusable UI Components Checklist

| Component | Purpose |
|---|---|
| `ChallengeCard` | Displays the rolled challenge |
| `DifficultySelector` | Segmented pill row for Easy/Medium/Hard |
| `RollButton` | Primary CTA with glow |
| `AppScaffold` | Wraps background color and padding |
| `NeonText` | Text with subtle glow shadow modifier |
| `PulseAnimation` | Reusable breathing/pulse Composable wrapper |

---

## 11. Dark Mode Support

The app is **dark mode only at v1**. There is no light theme. This simplifies the design system and aligns with the cyber-fitness aesthetic.

```kotlin
// ui/theme/Theme.kt

@Composable
fun RandomWorkoutTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Background,
            surface = Surface,
            primary = AccentLime,
            onPrimary = Color.Black,
            onBackground = TextPrimary,
            onSurface = TextPrimary
        ),
        typography = AppTypography,
        content = content
    )
}
```

Force dark mode in `AndroidManifest.xml` to prevent system theme override:

```xml
android:forceDarkAllowed="false"
```

---

## 12. Recommended Libraries

Keep dependencies minimal. Every library added increases build time and APK size.

| Library | Version | Purpose |
|---|---|---|
| Jetpack Compose BOM | latest stable | Compose UI toolkit |
| `androidx.lifecycle:lifecycle-viewmodel-compose` | latest | ViewModel in Composable |
| `androidx.lifecycle:lifecycle-runtime-compose` | latest | `collectAsStateWithLifecycle` |
| `kotlinx.coroutines` | latest | Coroutines + Flow + Channel |
| `androidx.activity:activity-compose` | latest | `ComponentActivity` + Compose |

No Hilt, no Room, no Retrofit needed for v1. Add only when a feature demands it.

---

## 13. Performance & Smooth UX

Performance is not optional — it is a core part of the user experience. Every frame drop, every delayed animation, every sluggish tap response breaks the premium cyber-fitness feeling this app is built around. The goal is **60fps minimum, 120fps where the device supports it**, with zero perceptible lag on any interaction.

---

### The UX Performance Contract

This app makes a silent promise to the user on every interaction:

| Interaction | Maximum acceptable delay |
|---|---|
| Tap "Roll Challenge" → challenge appears | < 16ms (instant, next frame) |
| Difficulty pill tap → visual feedback | < 32ms (within 2 frames) |
| App cold launch → interactive screen | < 400ms |
| Animation start → first frame rendered | 0ms delay — no pre-animation pause |

If any of these feel slow on a mid-range device, it must be fixed before release.

---

### Recomposition Control — The Most Critical Rule

Unnecessary recomposition is the number one performance killer in Compose. Every extra recomposition steals frame time from animations.

**Keep UiState completely flat.** Nested objects cause the entire subtree to recompose when any property changes.

```kotlin
// ❌ WRONG — nested object causes broad recomposition
data class ChallengeUiState(
    val difficultyState: DifficultyState,  // nested = bad
    val challengeState: ChallengeState     // nested = bad
)

// ✅ CORRECT — flat state, surgical recomposition
data class ChallengeUiState(
    val selectedDifficulty: Difficulty = Difficulty.EASY,
    val currentChallenge: Challenge? = null,
    val isRolling: Boolean = false
)
```

**Hoist lambdas out of Composables.** A new lambda object on every recomposition causes child Composables to recompose unnecessarily.

```kotlin
// ❌ WRONG — new lambda every recomposition
RollButton(onClick = { viewModel.onIntent(ChallengeIntent.RollChallenge) })

// ✅ CORRECT — stable reference, no recomposition churn
val onRoll = remember { { viewModel.onIntent(ChallengeIntent.RollChallenge) } }
RollButton(onClick = onRoll)
```

**Use `derivedStateOf` for computed values** that depend on state but shouldn't trigger recomposition on their own.

```kotlin
val isEasySelected by remember {
    derivedStateOf { uiState.selectedDifficulty == Difficulty.EASY }
}
```

---

### Animation Performance

Animations are the heartbeat of the UI. They must run on the **render thread**, never blocked by the main thread.

**Always use `animateFloatAsState` / `animateDpAsState` / `Animatable` — never manual frame loops.**

```kotlin
// Spring physics for organic feel — no duration tuning needed
val cardScale by animateFloatAsState(
    targetValue = if (challenge != null) 1f else 0.88f,
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    ),
    label = "cardScale"
)
```

**Rules for smooth animation:**

- Never start an animation inside a `LaunchedEffect` that waits on a coroutine delay — the delay eats frames
- All animation state reads must happen inside the `Composable` scope, not in lambdas or callbacks
- Use `graphicsLayer` for scale/alpha/rotation transforms — it runs on the render thread and skips recomposition entirely
- Never animate `Modifier.padding()` or `Modifier.size()` — these trigger layout passes on every frame; use `graphicsLayer(scaleX, scaleY)` instead

```kotlin
// ✅ GPU-accelerated — no layout pass on each frame
Box(
    modifier = Modifier.graphicsLayer {
        scaleX = cardScale
        scaleY = cardScale
        alpha = cardAlpha
    }
)

// ❌ CPU layout pass on every frame — causes jank
Box(modifier = Modifier.size((200 * cardScale).dp))
```

**The Roll Challenge animation must be immediate.** No artificial delay before the card appears. The state updates in the ViewModel synchronously — the Compose frame pipeline picks it up on the very next frame.

---

### State Collection — Lifecycle Awareness

```kotlin
// ✅ ALWAYS use this — stops collecting when app is in background
val uiState by viewModel.uiState.collectAsStateWithLifecycle()

// ❌ Never use this — keeps collecting even when screen is off
val uiState by viewModel.uiState.collectAsState()
```

The difference matters for battery life on fitness-oriented devices, and it prevents wasted recompositions when the app is not visible.

---

### Startup Performance

Cold launch must feel instant. The app has no data to load, no network calls, no disk reads — this advantage must be preserved.

- Do **not** add a splash screen beyond what the system provides via `SplashScreen API`
- Do **not** run any initialization in `Application.onCreate()` unless strictly necessary
- The `ChallengeRepository` challenge list is a compile-time constant — it initializes in nanoseconds
- `MainActivity` should set content immediately with no `delay()` or async initialization before first frame

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // No delay. No loading state. Render immediately.
        setContent {
            RandomWorkoutTheme {
                ChallengeScreen(viewModel = viewModel())
            }
        }
    }
}
```

---

### Haptic Timing

Haptic feedback must fire **at the moment of state change**, not after an animation completes. The physical sensation and the visual response should be simultaneous — this is what makes interactions feel premium and satisfying.

```kotlin
// In ViewModel — haptic event fires at the same moment state updates
is ChallengeIntent.RollChallenge -> {
    val challenge = repository.getRandom(_uiState.value.selectedDifficulty)
    _uiState.update { it.copy(currentChallenge = challenge) }
    viewModelScope.launch { _events.send(ChallengeEvent.TriggerHaptic) }  // same frame
}
```

---

### Profiling Checklist — Run Before Release

Use these tools to verify performance on a **physical mid-range device** (not emulator — GPU timing is different):

| Tool | What to check |
|---|---|
| **Recomposition Counter** (`countRenderingOperations`) | Zero unexpected recompositions during idle state |
| **Layout Inspector** | No deeply nested layout trees — keep depth under 8 levels |
| **Android Studio Profiler → CPU** | No main thread work during animation frames |
| **Systrace / Perfetto** | All frames under 16ms during roll animation |
| **`adb shell dumpsys gfxinfo`** | Janky frames = 0 across 3 roll interactions |

---

### The 60fps Rule

Every interaction in this app — tap, animation, state transition — must complete within **16ms per frame**. There is no exception. This app is simple enough that hitting 60fps everywhere is entirely achievable. On 90Hz and 120Hz devices, target those frame rates too by ensuring no synchronous work on the main thread.

If the profiler shows a frame over 16ms, the cause is almost always one of: an unexpected recomposition, an animated layout property, or work happening on the main thread. Fix it before shipping.

---

## 14. Clean Code Rules

- Every function does one thing
- No function longer than 30 lines — extract if it grows
- No business logic in Composables — only in ViewModel or Repository
- No hardcoded strings in Compose — use `stringResource()`
- UiState is always immutable — use `copy()` to update
- `when` expressions are always exhaustive — no missing branches
- No nullable types unless nullable is semantically meaningful
- Repository has no knowledge of ViewModel or UI layer
- ViewModel has no knowledge of Composable internals

---

## 15. Naming Conventions

| Element | Convention | Example |
|---|---|---|
| Composable functions | PascalCase | `ChallengeCard`, `RollButton` |
| ViewModel functions | camelCase, verb-first | `onIntent`, `getRandom` |
| UiState fields | camelCase nouns | `currentChallenge`, `isRolling` |
| Intent classes | PascalCase, action nouns | `RollChallenge`, `SelectDifficulty` |
| Event classes | PascalCase, action nouns | `TriggerHaptic` |
| Constants | SCREAMING_SNAKE_CASE | `DEFAULT_DIFFICULTY` |
| Files | PascalCase matching class | `ChallengeViewModel.kt` |
| Packages | lowercase, feature-based | `feature.challenge` |

---

## 16. Release Checklist

### Code Quality

- [ ] All `TODO` comments resolved or converted to issues
- [ ] No hardcoded strings — all in `strings.xml`
- [ ] ProGuard / R8 rules configured, tested on release build
- [ ] No logs (`Log.d`, `println`) in release code
- [ ] All `@Preview` Composables confirmed rendering correctly

### UX / Design

- [ ] All animations tested on a physical device (emulator timing differs)
- [ ] Haptic feedback tested on device (not emulator)
- [ ] Tested at 200% font scale — no text clipping or overflow
- [ ] All touch targets confirmed 48dp minimum
- [ ] `contentDescription` present on all interactive elements

### Build

- [ ] `versionCode` incremented
- [ ] `versionName` updated
- [ ] Release keystore configured and stored securely
- [ ] AAB (Android App Bundle) built, not APK, for Play Store
- [ ] `debuggable false` in release build config
- [ ] Baseline profile generated (optional but recommended)

### Testing

- [ ] ViewModel unit tests cover all intents
- [ ] Repository unit tests cover randomization and difficulty filter
- [ ] Manual smoke test on API 26, API 30, API 34 devices or emulators
- [ ] Rotation / configuration change tested — state preserved correctly

### Store Listing

- [ ] App icon (512x512 PNG, no alpha)
- [ ] Feature graphic (1024x500 PNG)
- [ ] At least 4 screenshots (phone, portrait)
- [ ] Short description (80 chars max)
- [ ] Full description written
- [ ] Content rating questionnaire completed
- [ ] Privacy policy URL provided (required even for apps with no data collection)

---

*Document version 1.0 — Random Challenge Workout*