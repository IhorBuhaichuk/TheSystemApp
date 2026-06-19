package com.ihor.thesystem.presentation.common.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.ihor.thesystem.core.theme.SystemTheme

@Composable
fun RpgStatusBackdrop() {
    val colors = SystemTheme.colors
    val ambientStrength = 1f

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(Color(0xFF010204))
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF03070B),
                    Color(0xFF010306),
                    Color(0xFF000102)
                )
            )
        )

        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    colors.accentPrimary.copy(alpha = 0.032f * ambientStrength),
                    Color.Transparent,
                    colors.accentAi.copy(alpha = 0.022f * ambientStrength)
                ),
                start = Offset(size.width, 0f),
                end = Offset(0f, size.height)
            )
        )

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    colors.accentPrimary.copy(alpha = 0.052f * ambientStrength),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.86f, size.height * 0.12f),
                radius = size.maxDimension * 0.72f
            )
        )
    }
}
