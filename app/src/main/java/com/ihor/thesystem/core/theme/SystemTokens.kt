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
    val material: SystemMaterialTokens,
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
    val ambientShadow: Color,
    val contactShadow: Color,
    val raisedElevation: Dp,
    val activeElevation: Dp,
    val plateElevation: Dp,
    val buttonElevation: Dp,
    val buttonActiveElevation: Dp,
    val contactElevation: Dp
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
        surfaceGlass = Color(0xB2101721),
        surfaceGlassStrong = Color(0xE00D151F),
        surfaceGlassSoft = Color(0x74101824),
        surfaceRaised = Color(0xF308111A),
        overlayLight = Color.White.copy(alpha = 0.055f),
        overlayMedium = Color.White.copy(alpha = 0.082f),
        overlayStrong = Color.White.copy(alpha = 0.135f),
        borderSubtle = Color.White.copy(alpha = 0.13f),
        borderMuted = Color.White.copy(alpha = 0.075f),
        borderActive = Color(0x8A20E8FF),
        accentPrimary = Color(0xFF1FE1FF),
        accentPrimarySoft = Color(0x3025E6FF),
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
    material = SystemMaterialTokens(
        panelTop = Color(0xF40B141C),
        panelMid = Color(0xF00E1822),
        panelBottom = Color(0xEA03070C),
        plateTop = Color(0xE80A121A),
        plateMid = Color(0xE10B151E),
        plateBottom = Color(0xDA03070B),
        buttonTop = Color(0xF0062434),
        buttonBottom = Color(0xF003111A),
        edgeHighlight = Color.White.copy(alpha = 0.22f),
        edgeShade = Color.Black.copy(alpha = 0.58f),
        innerHighlight = Color.White.copy(alpha = 0.045f),
        innerShade = Color.Black.copy(alpha = 0.22f),
        reflectedPrimary = Color(0xFF1FE1FF).copy(alpha = 0.105f),
        reflectedAi = Color(0xFF9B6DFF).copy(alpha = 0.085f),
        ambientShadow = Color.Black.copy(alpha = 0.46f),
        contactShadow = Color.Black.copy(alpha = 0.68f),
        raisedElevation = 16.dp,
        activeElevation = 24.dp,
        plateElevation = 8.dp,
        buttonElevation = 9.dp,
        buttonActiveElevation = 20.dp,
        contactElevation = 3.dp
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

    val material: SystemMaterialTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalSystemThemeTokens.current.material

    val motion: SystemMotionTokens
        @Composable
        @ReadOnlyComposable
        get() = LocalSystemThemeTokens.current.motion
}
