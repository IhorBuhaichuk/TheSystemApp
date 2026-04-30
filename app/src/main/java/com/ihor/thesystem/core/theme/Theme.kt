package com.ihor.thesystem.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SystemColorScheme = darkColorScheme(
    primary          = AccentPrimary,
    secondary        = AccentAi,
    tertiary         = AccentSuccess,
    background       = SystemBackground,
    surface          = SystemSurfaceGlassStrong,
    surfaceVariant   = SystemSurfaceGlass,
    onPrimary        = SystemBackground,
    onSecondary      = TextPrimary,
    onTertiary       = SystemBackground,
    onBackground     = TextPrimary,
    onSurface        = TextPrimary,
    onSurfaceVariant = OnSurfaceVariant,
    error            = AccentError,
    outline          = BorderSubtle
)

@Composable
fun TheSystemTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SystemColorScheme,
        typography  = TheSystemTypography,
        content     = content
    )
}
