package com.ihor.thesystem.feature.mode.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.R
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.feature.mode.viewmodel.CycleDayUiModel
import com.ihor.thesystem.feature.mode.viewmodel.DayType

@Composable
fun CycleDaySelector(
    days: List<CycleDayUiModel>,
    onTap: (Int) -> Unit,
    onLongPress: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        days.forEach { day ->
            CycleDayButton(
                day      = day,
                onTap    = { onTap(day.dayNumber) },
                onLongPress = { onLongPress(day.dayNumber) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CycleDayButton(
    day: CycleDayUiModel,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, iconColor, glowColor) = when (day.dayNumber) {
        1 -> Triple(Icons.Filled.WbSunny, Color(0xFFFFD600), Color(0xFFFFD600)) // Сонце (День)
        2 -> Triple(Icons.Filled.NightsStay, Color(0xFF81D4FA), Color(0xFF0288D1)) // Місяць (Ніч)
        3 -> Triple(Icons.Filled.Bedtime, Color(0xFFA5D6A7), Color(0xFF4CAF50)) // Людина спить / Zzz (Відсипний)
        4 -> Triple(Icons.Filled.Favorite, Color(0xFFF06292), Color(0xFFE91E63)) // Вихідний (Серце/Спорт/Релакс)
        else -> Triple(Icons.Filled.Circle, Color.White, Color.White)
    }

    val label = when (day.dayNumber) {
        1 -> stringResource(R.string.cycle_day_1)
        2 -> stringResource(R.string.cycle_day_2)
        3 -> stringResource(R.string.cycle_day_3)
        4 -> stringResource(R.string.cycle_day_4)
        else -> stringResource(R.string.cycle_day_default, day.dayNumber)
    }

    // Анімація пульсації для активного дня
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by if (day.isActive) {
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
            label = "alpha"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }
    
    val pulseScale by if (day.isActive) {
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
            label = "scale"
        )
    } else {
        remember { mutableFloatStateOf(1f) }
    }

    Column(
        modifier              = modifier,
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier         = Modifier
                .size(56.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onTap() },
                        onLongPress = { onLongPress() }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Glow effect
            if (day.isActive) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .blur(15.dp)
                        .background(glowColor.copy(alpha = pulseAlpha), CircleShape)
                )
            }

            // Selection indicator (bottom bar or ring)
            if (day.isSelected && !day.isActive) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.5.dp, iconColor.copy(alpha = 0.3f), CircleShape)
                )
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (day.isActive || day.isSelected) iconColor else iconColor.copy(alpha = 0.2f),
                modifier = Modifier
                    .size(if (day.isActive) 32.dp else 28.dp)
                    .then(if (day.isActive) Modifier.size(32.dp * pulseScale) else Modifier)
            )
        }

        Text(
            text       = label.uppercase(),
            color      = if (day.isActive) iconColor else if (day.isSelected) Color.White else Color.White.copy(alpha = 0.3f),
            fontSize   = 10.sp,
            fontFamily = RajdhaniFamily,
            fontWeight = if (day.isActive || day.isSelected) FontWeight.Bold else FontWeight.Medium,
            letterSpacing = 1.sp
        )
    }
}

