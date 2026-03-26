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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
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
    val borderAlpha by rememberInfiniteTransition(label = "pulse")
        .animateFloat(
            initialValue   = 0.5f,
            targetValue    = 1.0f,
            animationSpec  = infiniteRepeatable(
                animation  = tween(1200, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label          = "borderAlpha"
        )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .drawBehind {
                val cut  = 14.dp.toPx()
                val path = Path().apply {
                    moveTo(cut, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width, size.height - cut)
                    lineTo(size.width - cut, size.height)
                    lineTo(0f, size.height)
                    lineTo(0f, cut)
                    close()
                }
                drawPath(path, NeonCyan.copy(alpha = 0.12f))
                drawPath(path, NeonCyan.copy(alpha = borderAlpha), style = Stroke(2.dp.toPx()))
            }
            .background(androidx.compose.ui.graphics.Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = Icons.Filled.ArrowForward,
                contentDescription = null,
                tint               = NeonCyan,
                modifier           = Modifier.size(18.dp)
            )
            Text(
                text          = "ЗАВЕРШИТИ ДЕНЬ ${currentDay} → ДЕНЬ ${nextDay}",
                color         = NeonCyan,
                fontFamily    = FontFamily.Monospace,
                fontWeight    = FontWeight.Bold,
                fontSize      = 12.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}