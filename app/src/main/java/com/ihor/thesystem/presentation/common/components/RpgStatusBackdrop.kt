package com.ihor.thesystem.presentation.common.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.ihor.thesystem.core.theme.SystemTheme

@Composable
fun RpgStatusBackdrop() {
    val colors = SystemTheme.colors
    val ambientStrength = 1f

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                val backgroundBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF03070B),
                        Color(0xFF010306),
                        Color(0xFF000102)
                    )
                )
                val ambientBrush = Brush.linearGradient(
                    colors = listOf(
                        colors.accentPrimary.copy(alpha = 0.028f * ambientStrength),
                        Color.Transparent,
                        colors.accentAi.copy(alpha = 0.014f * ambientStrength)
                    ),
                    start = Offset.Zero,
                    end = Offset(size.width, size.height)
                )
                val cornerGlowBrush = Brush.radialGradient(
                    colors = listOf(
                        colors.accentPrimary.copy(alpha = 0.044f * ambientStrength),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.08f, size.height * 0.04f),
                    radius = size.maxDimension * 0.68f
                )

                onDrawBehind {
                    drawRect(Color(0xFF010204))
                    drawRect(brush = backgroundBrush)
                    drawRect(brush = ambientBrush)
                    drawRect(brush = cornerGlowBrush)
                }
            }
    ) {}
}
