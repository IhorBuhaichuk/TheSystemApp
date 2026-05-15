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

@Immutable
data class SystemThemeTokens(
    val id: SystemThemeId,
    val colors: SystemColorTokens,
    val shapes: SystemShapeTokens,
    val glow: SystemGlowTokens,
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
)

@Immutable
data class SystemShapeTokens(
    val extraSmall: Dp,
    val small: Dp,
    val medium: Dp,
    val large: Dp,
    val extraLarge: Dp,
    val pill: Dp
)

@Immutable
data class SystemGlowTokens(
    val restingElevation: Dp,
    val activeElevation: Dp,
    val buttonElevation: Dp,
    val buttonActiveElevation: Dp,
    val shadowAmbient: Color,
    val shadowSpot: Color,
    val activeAmbient: Color,
    val primaryGlow: Color,
    val aiGlow: Color,
    val successGlow: Color
)

@Immutable
data class SystemMotionTokens(
    val progressMillis: Int,
    val quickStateMillis: Int,
    val breathingMillis: Int,
    val activeScale: Float,
    val glowIntensity: Float
)

val AiOrganismThemeTokens = SystemThemeTokens(
    id = SystemThemeId.AiOrganism,
    colors = SystemColorTokens(
        background = Color(0xFF020407),
        backgroundSecondary = Color(0xFF071018),
        backgroundElevated = Color(0xFF0A121C),
        surfaceGlass = Color(0xA4121720),
        surfaceGlassStrong = Color(0xD3161D29),
        surfaceGlassSoft = Color(0x6B111824),
        surfaceRaised = Color(0xF20B1320),
        overlayLight = Color.White.copy(alpha = 0.045f),
        overlayMedium = Color.White.copy(alpha = 0.075f),
        overlayStrong = Color.White.copy(alpha = 0.12f),
        borderSubtle = Color.White.copy(alpha = 0.10f),
        borderMuted = Color.White.copy(alpha = 0.055f),
        borderActive = Color(0x7A20E8FF),
        accentPrimary = Color(0xFF25E6FF),
        accentPrimarySoft = Color(0x2E25E6FF),
        accentAi = Color(0xFF9B6DFF),
        accentAiSoft = Color(0x339B6DFF),
        accentInfo = Color(0xFF5CC8FF),
        accentSuccess = Color(0xFF35E6A8),
        accentWarning = Color(0xFFFFC766),
        accentError = Color(0xFFFF5F7D),
        textPrimary = Color(0xFFF6FAFF),
        textSecondary = Color(0xFFA8B5C4),
        textMuted = Color(0xFF657181),
        textDisabled = Color(0xFF48525F),
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
        small = 10.dp,
        medium = 16.dp,
        large = 22.dp,
        extraLarge = 28.dp,
        pill = 999.dp
    ),
    glow = SystemGlowTokens(
        restingElevation = 14.dp,
        activeElevation = 22.dp,
        buttonElevation = 6.dp,
        buttonActiveElevation = 18.dp,
        shadowAmbient = Color.Black.copy(alpha = 0.36f),
        shadowSpot = Color.Black.copy(alpha = 0.44f),
        activeAmbient = Color(0xFF25E6FF).copy(alpha = 0.16f),
        primaryGlow = Color(0xFF25E6FF).copy(alpha = 0.28f),
        aiGlow = Color(0xFF9B6DFF).copy(alpha = 0.24f),
        successGlow = Color(0xFF35E6A8).copy(alpha = 0.22f)
    ),
    motion = SystemMotionTokens(
        progressMillis = 720,
        quickStateMillis = 220,
        breathingMillis = 6400,
        activeScale = 1.015f,
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

    val motion: SystemMotionTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalSystemThemeTokens.current.motion
}
