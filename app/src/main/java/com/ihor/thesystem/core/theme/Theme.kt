package com.ihor.thesystem.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SystemColorScheme = darkColorScheme(
    primary          = Primary,
    secondary        = StatusWarning,
    tertiary         = StatusSuccess,
    background       = BackgroundDeep,
    surface          = Color.White.copy(alpha = 0.05f),
    onPrimary        = BackgroundDeep,
    onSecondary      = BackgroundDeep,
    onTertiary       = BackgroundDeep,
    onBackground     = OnBackground,
    onSurface        = OnBackground,
    onSurfaceVariant = OnSurfaceVariant,
    error            = StatusError,
    outline          = Color.White.copy(alpha = 0.1f)
)

@Composable
fun TheSystemTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SystemColorScheme,
        typography  = TheSystemTypography,
        content     = content
    )
}