package com.ihor.thesystem.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.shape.RoundedCornerShape

private fun systemColorScheme(tokens: SystemThemeTokens): ColorScheme =
    darkColorScheme(
        primary = tokens.colors.accentPrimary,
        secondary = tokens.colors.accentAi,
        tertiary = tokens.colors.accentSuccess,
        background = tokens.colors.background,
        surface = tokens.colors.surfaceGlassStrong,
        surfaceVariant = tokens.colors.surfaceGlass,
        onPrimary = tokens.colors.background,
        onSecondary = tokens.colors.textPrimary,
        onTertiary = tokens.colors.background,
        onBackground = tokens.colors.textPrimary,
        onSurface = tokens.colors.textPrimary,
        onSurfaceVariant = tokens.colors.textSecondary,
        error = tokens.colors.accentError,
        outline = tokens.colors.borderSubtle
    )

private fun systemShapes(tokens: SystemThemeTokens): Shapes =
    Shapes(
        extraSmall = RoundedCornerShape(tokens.shapes.extraSmall),
        small = RoundedCornerShape(tokens.shapes.small),
        medium = RoundedCornerShape(tokens.shapes.medium),
        large = RoundedCornerShape(tokens.shapes.large),
        extraLarge = RoundedCornerShape(tokens.shapes.extraLarge)
    )

private fun systemTypography(tokens: SystemThemeTokens): Typography =
    TheSystemTypography.copy(
        displayLarge = TheSystemTypography.displayLarge.copy(color = tokens.colors.textPrimary),
        titleLarge = TheSystemTypography.titleLarge.copy(color = tokens.colors.textPrimary),
        titleMedium = TheSystemTypography.titleMedium.copy(color = tokens.colors.textPrimary),
        bodyLarge = TheSystemTypography.bodyLarge.copy(color = tokens.colors.textPrimary),
        bodyMedium = TheSystemTypography.bodyMedium.copy(color = tokens.colors.textSecondary),
        bodySmall = TheSystemTypography.bodySmall.copy(color = tokens.colors.textMuted),
        labelLarge = TheSystemTypography.labelLarge.copy(color = tokens.colors.textSecondary),
        labelMedium = TheSystemTypography.labelMedium.copy(color = tokens.colors.textSecondary),
        labelSmall = TheSystemTypography.labelSmall.copy(color = tokens.colors.textSecondary)
    )

@Composable
fun TheSystemTheme(
    theme: SystemThemeTokens = AiOrganismThemeTokens,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalSystemThemeTokens provides theme) {
        MaterialTheme(
            colorScheme = systemColorScheme(theme),
            typography = systemTypography(theme),
            shapes = systemShapes(theme),
            content = content
        )
    }
}
