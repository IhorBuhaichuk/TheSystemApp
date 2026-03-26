package com.ihor.thesystem.feature.status.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.NeonCyan
import com.ihor.thesystem.core.ui.components.neonGlow

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SystemHeader(
    modifier: Modifier = Modifier,
    onLongPress: () -> Unit = {}
) {
    val dotAlpha by rememberInfiniteTransition(label = "dot")
        .animateFloat(
            initialValue  = 0.4f,
            targetValue   = 1.0f,
            animationSpec = infiniteRepeatable(
                tween(1000, easing = EaseInOutSine),
                RepeatMode.Reverse
            ),
            label = "dotAlpha"
        )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 18.dp)
            .combinedClickable(onClick = {}, onLongClick = onLongPress),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text          = "СИСТЕМА v1.0",
            color         = NeonCyan,
            fontFamily    = FontFamily.Monospace,
            fontWeight    = FontWeight.Bold,
            fontSize      = 18.sp,
            letterSpacing = 3.sp
        )
        Spacer(modifier = Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .size(9.dp)
                .neonGlow(NeonCyan.copy(alpha = dotAlpha), radius = 6.dp)
                .background(NeonCyan.copy(alpha = dotAlpha), CircleShape)
        )
    }
}