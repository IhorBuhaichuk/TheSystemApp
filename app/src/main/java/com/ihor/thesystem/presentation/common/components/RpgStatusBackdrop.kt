package com.ihor.thesystem.presentation.common.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    val motion = SystemTheme.motion
    val infiniteTransition = rememberInfiniteTransition(label = "system_backdrop")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.62f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(motion.breathingMillis, easing = LinearEasing), RepeatMode.Reverse),
        label = "ambient_pulse"
    )
    val signalPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(motion.breathingMillis * 3, easing = LinearEasing)),
        label = "ambient_signal_phase"
    )

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
                    colors.accentPrimary.copy(alpha = 0.025f * pulse),
                    Color.Transparent,
                    colors.accentAi.copy(alpha = 0.032f * pulse)
                ),
                start = Offset(size.width, 0f),
                end = Offset(0f, size.height)
            )
        )

        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    colors.accentPrimary.copy(alpha = 0.05f * pulse),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.86f, size.height * 0.12f),
                radius = size.maxDimension * 0.72f
            )
        )

        repeat(3) { index ->
            val phase = (signalPhase + index * 0.18f) % 1f
            val y = size.height * (0.24f + index * 0.2f) + (phase - 0.5f) * 26.dp.toPx()
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
                    colors.accentAi.copy(alpha = 0.026f * pulse)
                } else {
                    colors.accentPrimary.copy(alpha = 0.022f * pulse)
                },
                style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}
