package com.ihor.thesystem.presentation.common.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.ihor.thesystem.core.theme.SystemTheme

@Composable
fun RpgStatusBackdrop() {
    val colors = SystemTheme.colors
    val ambientStrength = 0.82f

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(colors.background)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    colors.backgroundSecondary.copy(alpha = 0.94f),
                    colors.background,
                    colors.background.copy(alpha = 0.98f)
                )
            )
        )

        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    colors.accentPrimary.copy(alpha = 0.025f * ambientStrength),
                    Color.Transparent,
                    colors.accentAi.copy(alpha = 0.032f * ambientStrength)
                ),
                start = Offset(size.width, 0f),
                end = Offset(0f, size.height)
            )
        )

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    colors.accentPrimary.copy(alpha = 0.05f * ambientStrength),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.86f, size.height * 0.12f),
                radius = size.maxDimension * 0.72f
            )
        )

        repeat(3) { index ->
            val y = size.height * (0.24f + index * 0.2f) + (index - 1) * 6.dp.toPx()
            val path = Path().apply {
                moveTo(-size.width * 0.08f, y)
                cubicTo(
                    size.width * 0.24f,
                    y - 34.dp.toPx(),
                    size.width * 0.54f,
                    y + 30.dp.toPx(),
                    size.width * 1.08f,
                    y - 8.dp.toPx()
                )
            }
            drawPath(
                path = path,
                color = if (index == 1) {
                    colors.accentAi.copy(alpha = 0.026f * ambientStrength)
                } else {
                    colors.accentPrimary.copy(alpha = 0.022f * ambientStrength)
                },
                style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}
