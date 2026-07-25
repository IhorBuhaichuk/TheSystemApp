package com.ihor.thesystem.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class SystemThemeId {
    AiOrganism
}

enum class SystemStatusRole {
    Progress,
    Recovery,
    Danger,
    Reward,
    Neutral,
    Ai
}

@Immutable
data class SystemThemeTokens(
    val id: SystemThemeId,
    val colors: SystemColorTokens,
    val shapes: SystemShapeTokens,
    val glow: SystemGlowTokens,
    val material: SystemMaterialTokens,
    val depth: SystemDepthTokens,
    val motion: SystemMotionTokens
)

@Immutable
data class SystemColorTokens(
    val background: Color,
    val backgroundSecondary: Color,
    val backgroundElevated: Color,
    val surfaceGlass: Color,
    val surfaceGlassStrong: Color,
    val surfaceGlassSoft: Color,
    val surfaceRaised: Color,
    val overlayLight: Color,
    val overlayMedium: Color,
    val overlayStrong: Color,
    val borderSubtle: Color,
    val borderMuted: Color,
    val borderActive: Color,
    val accentPrimary: Color,
    val accentPrimarySoft: Color,
    val accentAi: Color,
    val accentAiSoft: Color,
    val accentInfo: Color,
    val accentSuccess: Color,
    val accentWarning: Color,
    val accentError: Color,
    val statusProgress: Color,
    val statusRecovery: Color,
    val statusDanger: Color,
    val statusReward: Color,
    val statusNeutral: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textDisabled: Color,
    val workDay: Color,
    val trainingDay: Color,
    val mixedDayStart: Color,
    val mixedDayEnd: Color,
    val rankE: Color,
    val rankD: Color,
    val rankC: Color,
    val rankB: Color,
    val rankA: Color,
    val rankS: Color
) {
    fun forStatus(role: SystemStatusRole): Color = when (role) {
        SystemStatusRole.Progress -> statusProgress
        SystemStatusRole.Recovery -> statusRecovery
        SystemStatusRole.Danger -> statusDanger
        SystemStatusRole.Reward -> statusReward
        SystemStatusRole.Neutral -> statusNeutral
        SystemStatusRole.Ai -> accentAi
    }
}

@Immutable
data class SystemShapeTokens(
    val extraSmall: Dp,
    val small: Dp,
    val medium: Dp,
    val large: Dp,
    val extraLarge: Dp,
    val pill: Dp,
    val panelCut: Dp,
    val plateCut: Dp,
    val controlCut: Dp,
    val dialogCut: Dp
)

@Immutable
data class SystemGlowTokens(
    val primaryGlow: Color,
    val aiGlow: Color,
    val recoveryGlow: Color,
    val rewardGlow: Color,
    val dangerGlow: Color,
    val restingAlpha: Float,
    val activeAlpha: Float
)

@Immutable
data class SystemMaterialTokens(
    val panelTop: Color,
    val panelMid: Color,
    val panelBottom: Color,
    val plateTop: Color,
    val plateMid: Color,
    val plateBottom: Color,
    val buttonTop: Color,
    val buttonBottom: Color,
    val edgeHighlight: Color,
    val edgeShade: Color,
    val innerHighlight: Color,
    val innerShade: Color,
    val reflectedPrimary: Color,
    val reflectedAi: Color,
    val railInactive: Color
)

@Immutable
data class SystemDepthTokens(
    val ambientShadow: Color,
    val contactShadow: Color,
    val panelElevation: Dp,
    val activeElevation: Dp,
    val plateElevation: Dp,
    val buttonElevation: Dp,
    val buttonActiveElevation: Dp,
    val dialogElevation: Dp
)

@Immutable
data class SystemMotionTokens(
    val pressMillis: Int,
    val stateMillis: Int,
    val enterExitMillis: Int,
    val progressMillis: Int,
    val celebrationMillis: Int,
    val breathingMillis: Int,
    val pressedScale: Float,
    val pressedDepth: Dp,
    val activeScale: Float,
    val enterScale: Float,
    val enterOffset: Dp,
    val celebrationStartScale: Float,
    val celebrationPeakScale: Float,
    val glowIntensity: Float
) {
    val quickStateMillis: Int
        get() = stateMillis
}

val AiOrganismThemeTokens = SystemThemeTokens(
    id = SystemThemeId.AiOrganism,
    colors = SystemColorTokens(
        background = Color(0xFF020407),
        backgroundSecondary = Color(0xFF071018),
        backgroundElevated = Color(0xFF0A121C),
        surfaceGlass = Color(0xC0111821),
        surfaceGlassStrong = Color(0xF00D141D),
        surfaceGlassSoft = Color(0x8A101720),
        surfaceRaised = Color(0xF50A1119),
        overlayLight = Color.White.copy(alpha = 0.040f),
        overlayMedium = Color.White.copy(alpha = 0.064f),
        overlayStrong = Color.White.copy(alpha = 0.105f),
        borderSubtle = Color.White.copy(alpha = 0.105f),
        borderMuted = Color.White.copy(alpha = 0.060f),
        borderActive = Color(0x7020E8FF),
        accentPrimary = Color(0xFF1FE1FF),
        accentPrimarySoft = Color(0x3025E6FF),
        accentAi = Color(0xFF9B6DFF),
        accentAiSoft = Color(0x339B6DFF),
        accentInfo = Color(0xFF5CC8FF),
        accentSuccess = Color(0xFF35E6A8),
        accentWarning = Color(0xFFFFC766),
        accentError = Color(0xFFFF5F7D),
        statusProgress = Color(0xFF20DCF7),
        statusRecovery = Color(0xFF35E6A8),
        statusDanger = Color(0xFFFF5F7D),
        statusReward = Color(0xFFFFC766),
        statusNeutral = Color(0xFF8391A2),
        textPrimary = Color(0xFFF6FAFF),
        textSecondary = Color(0xFFA8B5C4),
        textMuted = Color(0xFF788596),
        textDisabled = Color(0xFF596574),
        workDay = Color(0xFF52647A),
        trainingDay = Color(0xFF25E6FF),
        mixedDayStart = Color(0xFF52647A),
        mixedDayEnd = Color(0xFF25E6FF),
        rankE = Color(0xFF7A8593),
        rankD = Color(0xFF5CC8FF),
        rankC = Color(0xFF35E6A8),
        rankB = Color(0xFFFFC766),
        rankA = Color(0xFF9B6DFF),
        rankS = Color(0xFFFF5F7D)
    ),
    shapes = SystemShapeTokens(
        extraSmall = 8.dp,
        small = 12.dp,
        medium = 18.dp,
        large = 24.dp,
        extraLarge = 30.dp,
        pill = 999.dp,
        panelCut = 12.dp,
        plateCut = 8.dp,
        controlCut = 7.dp,
        dialogCut = 14.dp
    ),
    glow = SystemGlowTokens(
        primaryGlow = Color(0xFF25E6FF),
        aiGlow = Color(0xFF9B6DFF),
        recoveryGlow = Color(0xFF35E6A8),
        rewardGlow = Color(0xFFFFC766),
        dangerGlow = Color(0xFFFF5F7D),
        restingAlpha = 0.024f,
        activeAlpha = 0.085f
    ),
    material = SystemMaterialTokens(
        panelTop = Color(0xFA141B24),
        panelMid = Color(0xF50D141D),
        panelBottom = Color(0xF0080D13),
        plateTop = Color(0xED111820),
        plateMid = Color(0xE80C1219),
        plateBottom = Color(0xE3070B10),
        buttonTop = Color(0xF20D2029),
        buttonBottom = Color(0xF4081118),
        edgeHighlight = Color(0xFFE7F0F7).copy(alpha = 0.22f),
        edgeShade = Color.Black.copy(alpha = 0.52f),
        innerHighlight = Color.White.copy(alpha = 0.030f),
        innerShade = Color.Black.copy(alpha = 0.28f),
        reflectedPrimary = Color(0xFF1FE1FF).copy(alpha = 0.070f),
        reflectedAi = Color(0xFF9B6DFF).copy(alpha = 0.055f),
        railInactive = Color(0xFF34414D).copy(alpha = 0.34f)
    ),
    depth = SystemDepthTokens(
        ambientShadow = Color.Black.copy(alpha = 0.34f),
        contactShadow = Color.Black.copy(alpha = 0.48f),
        panelElevation = 5.dp,
        activeElevation = 7.dp,
        plateElevation = 0.dp,
        buttonElevation = 2.dp,
        buttonActiveElevation = 4.dp,
        dialogElevation = 14.dp
    ),
    motion = SystemMotionTokens(
        pressMillis = 110,
        stateMillis = 200,
        enterExitMillis = 280,
        progressMillis = 720,
        celebrationMillis = 860,
        breathingMillis = 6400,
        pressedScale = 0.985f,
        pressedDepth = 1.dp,
        activeScale = 1.01f,
        enterScale = 0.985f,
        enterOffset = 8.dp,
        celebrationStartScale = 0.90f,
        celebrationPeakScale = 1.055f,
        glowIntensity = 1f
    )
)

val LocalSystemThemeTokens = staticCompositionLocalOf { AiOrganismThemeTokens }

object SystemTheme {
    val tokens: SystemThemeTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalSystemThemeTokens.current

    val colors: SystemColorTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalSystemThemeTokens.current.colors

    val shapes: SystemShapeTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalSystemThemeTokens.current.shapes

    val glow: SystemGlowTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalSystemThemeTokens.current.glow

    val material: SystemMaterialTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalSystemThemeTokens.current.material

    val depth: SystemDepthTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalSystemThemeTokens.current.depth

    val motion: SystemMotionTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalSystemThemeTokens.current.motion
}
