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
import com.ihor.thesystem.core.theme.AccentPrimary
import com.ihor.thesystem.core.theme.SystemBackground
import com.ihor.thesystem.core.theme.SystemBackgroundSecondary

@Composable
fun RpgStatusBackdrop() {
    val infiniteTransition = rememberInfiniteTransition(label = "system_backdrop")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(6400, easing = LinearEasing), RepeatMode.Reverse),
        label = "ambient_pulse"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(SystemBackground)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    SystemBackgroundSecondary.copy(alpha = 0.9f),
                    SystemBackground,
                    Color.Black.copy(alpha = 0.98f)
                )
            )
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(AccentPrimary.copy(alpha = 0.055f * pulse), Color.Transparent),
                center = Offset(size.width * 0.82f, size.height * 0.08f),
                radius = size.width * 0.72f
            )
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF8B5CF6).copy(alpha = 0.04f * pulse), Color.Transparent),
                center = Offset(size.width * 0.08f, size.height * 0.7f),
                radius = size.width * 0.84f
            )
        )
    }
}
