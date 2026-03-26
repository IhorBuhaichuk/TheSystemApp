package com.ihor.thesystem.feature.statistics.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.ihor.thesystem.core.theme.*

@Composable
fun MatrixProgressBar(
    progress: Float,        // 0f..1f
    accentColor: Color = NeonGreen,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue    = progress.coerceIn(0f, 1f),
        animationSpec  = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label          = "matrixProgress"
    )

    Canvas(modifier = modifier.height(8.dp).fillMaxWidth()) {
        val cr = CornerRadius(4.dp.toPx())
        // Track
        drawRoundRect(
            color        = BackgroundDeep,
            cornerRadius = cr
        )
        // Fill
        if (animatedProgress > 0f) {
            drawRoundRect(
                color        = accentColor.copy(alpha = 0.85f),
                size         = Size(size.width * animatedProgress, size.height),
                cornerRadius = cr
            )
        }
        // Border
        drawRoundRect(
            color        = accentColor.copy(alpha = 0.35f),
            cornerRadius = cr,
            style        = Stroke(width = 1.dp.toPx())
        )
    }
}