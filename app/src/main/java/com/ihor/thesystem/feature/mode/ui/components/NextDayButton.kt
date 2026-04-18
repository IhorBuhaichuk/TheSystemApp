package com.ihor.thesystem.feature.mode.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val containerColor by animateColorAsState(
        targetValue = if (isPressed) Color(0xFF151515) else Color(0xFF0F0F0F),
        animationSpec = tween(100), label = "color"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isPressed) NeonGreen.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f),
        animationSpec = tween(200), label = "border"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Custom visual feedback via animateColorAsState
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "ЗАВЕРШИТИ ДЕНЬ",
            color = if (isPressed) NeonGreen else Color.White.copy(alpha = 0.9f),
            fontFamily = TekoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            letterSpacing = 2.sp
        )
    }
}

