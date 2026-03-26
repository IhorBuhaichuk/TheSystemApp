package com.ihor.thesystem.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SystemColorScheme = darkColorScheme(
    primary          = NeonCyan,
    secondary        = NeonGold,
    tertiary         = NeonGreen,
    background       = BackgroundDeep,
    surface          = PanelSurface,
    onPrimary        = BackgroundDeep,
    onSecondary      = BackgroundDeep,
    onTertiary       = BackgroundDeep,
    onBackground     = TextPrimary,
    onSurface        = TextPrimary,
    error            = NeonRed,
    outline          = PanelBorder
)

@Composable
fun TheSystemTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SystemColorScheme,
        typography  = TheSystemTypography,
        content     = content
    )
}