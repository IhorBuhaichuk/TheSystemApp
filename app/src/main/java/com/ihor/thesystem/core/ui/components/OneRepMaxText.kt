package com.ihor.thesystem.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.BackgroundDeep
import com.ihor.thesystem.core.theme.NeonCyan
import com.ihor.thesystem.core.theme.TextSecondary
import com.ihor.thesystem.core.util.OneRepMaxCalculator

@Composable
fun OneRepMaxText(
    weight: Double,
    reps: Int,
    modifier: Modifier = Modifier,
    label: String = "1RM: "
) {
    val oneRepMax = OneRepMaxCalculator.calculate(weight, reps)
    if (oneRepMax <= 0) return

    Text(
        text = "$label${OneRepMaxCalculator.format(oneRepMax)}",
        color = NeonCyan.copy(alpha = 0.9f),
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color.Black.copy(alpha = 0.3f))
            .padding(horizontal = 4.dp, vertical = 1.dp)
    )
}

@Composable
fun MaxOneRepMaxText(
    sets: List<Pair<Double, Int>>,
    modifier: Modifier = Modifier
) {
    val max1RM = OneRepMaxCalculator.calculateMax(sets)
    if (max1RM <= 0) return

    Text(
        text = "MAX 1RM: ${OneRepMaxCalculator.format(max1RM)}",
        color = NeonCyan,
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp,
        fontWeight = FontWeight.Black,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}
