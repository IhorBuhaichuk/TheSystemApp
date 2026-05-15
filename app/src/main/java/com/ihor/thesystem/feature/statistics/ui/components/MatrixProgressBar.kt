package com.ihor.thesystem.feature.statistics.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.ihor.thesystem.core.theme.SystemTheme

@Composable
fun MatrixProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    accentColor: Color = SystemTheme.colors.accentSuccess
) {
    val colors = SystemTheme.colors
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = SystemTheme.motion.progressMillis, easing = FastOutSlowInEasing),
        label = "matrixProgress"
    )

    Canvas(modifier = modifier.height(8.dp).fillMaxWidth()) {
        val cr = CornerRadius(4.dp.toPx())
        drawRoundRect(
            color = colors.overlayMedium,
            cornerRadius = cr
        )
        if (animatedProgress > 0f) {
            drawRoundRect(
                color = accentColor.copy(alpha = 0.85f),
                size = Size(size.width * animatedProgress, size.height),
                cornerRadius = cr
            )
        }
        drawRoundRect(
            color = accentColor.copy(alpha = 0.35f),
            cornerRadius = cr,
            style = Stroke(width = 1.dp.toPx())
        )
    }
}
