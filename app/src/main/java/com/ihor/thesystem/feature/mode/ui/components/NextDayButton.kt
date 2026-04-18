package com.ihor.thesystem.feature.mode.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.*

@Composable
fun NextDayButton(
    currentDay: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val nextDay = if (currentDay >= 4) 1 else currentDay + 1
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue   = 0.3f,
        targetValue    = 0.8f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(1500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label          = "borderAlpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .drawBehind {
                val cut  = 16.dp.toPx()
                val path = Path().apply {
                    moveTo(cut, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width, size.height - cut)
                    lineTo(size.width - cut, size.height)
                    lineTo(0f, size.height)
                    lineTo(0f, cut)
                    close()
                }
                drawPath(path, NeonCyan.copy(alpha = 0.08f))
                drawPath(path, NeonCyan.copy(alpha = borderAlpha), style = Stroke(2.dp.toPx()))
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = Icons.Filled.ArrowForward,
                contentDescription = null,
                tint               = NeonCyan,
                modifier           = Modifier.size(20.dp)
            )
            Text(
                text          = "ПРОТОКОЛ: ЗАВЕРШИТИ ДЕНЬ $currentDay",
                color         = NeonCyan,
                fontFamily    = TekoFamily,
                fontWeight    = FontWeight.Bold,
                fontSize      = 18.sp,
                letterSpacing = 2.sp
            )
        }
    }
}
