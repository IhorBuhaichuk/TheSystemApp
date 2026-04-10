package com.ihor.thesystem.feature.statistics.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.*

@Composable
fun AttributePanel(
    strValue: Int,
    endValue: Int,
    disValue: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Заголовок секції
        Text(
            text = "[ АТРИБУТИ СУТНОСТІ ]",
            color = TextSecondary,
            fontSize = 9.sp,
            fontFamily = RajdhaniFamily,
            letterSpacing = 1.5.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Три атрибути
        AttributeRow(label = "STR", value = strValue,  color = NeonGold)
        AttributeRow(label = "END", value = endValue,  color = NeonCyan)
        AttributeRow(label = "DIS", value = disValue,  color = Color(0xFF7B2FFF))
    }
}

@Composable
private fun AttributeRow(
    label: String,
    value: Int,         // 0-100
    color: Color
) {
    // Анімація значення при зміні
    val animatedValue by animateFloatAsState(
        targetValue = value / 100f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "attr_$label"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Мітка атрибуту
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontFamily = TekoFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(32.dp)
        )

        // Сегментований прогрес-бар (10 сегментів)
        Canvas(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
        ) {
            val segmentCount = 10
            val segmentSpacing = 3.dp.toPx()
            val totalSpacing = (segmentCount - 1) * segmentSpacing
            val segmentWidth = (size.width - totalSpacing) / segmentCount
            val filledSegments = (animatedValue * segmentCount).toInt()

            repeat(segmentCount) { index ->
                val xOffset = index * (segmentWidth + segmentSpacing)
                drawRoundRect(
                    color = if (index < filledSegments) color else color.copy(alpha = 0.15f),
                    topLeft = Offset(xOffset, 0f),
                    size = Size(segmentWidth, size.height),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
            }
        }

        // Числове значення
        Text(
            text = value.toString(),
            color = color,
            fontSize = 11.sp,
            fontFamily = TekoFamily,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(28.dp),
            textAlign = TextAlign.End
        )
    }
}
