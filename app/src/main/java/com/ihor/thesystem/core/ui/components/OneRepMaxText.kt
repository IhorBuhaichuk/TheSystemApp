package com.ihor.thesystem.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.ihor.thesystem.R
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.domain.util.OneRepMaxCalculator

@Composable
fun OneRepMaxText(
    weight: Double,
    reps: Int,
    modifier: Modifier = Modifier,
    label: String = "Розрахунковий максимум: "
) {
    val oneRepMax = OneRepMaxCalculator.calculate(weight, reps)
    if (oneRepMax <= 0) return
    val colors = SystemTheme.colors

    Text(
        text = "$label${OneRepMaxCalculator.format(oneRepMax)}${stringResource(R.string.text_unit_kg)}",
        style = MaterialTheme.typography.labelSmall.copy(
            color = colors.accentPrimary.copy(alpha = 0.9f),
            fontWeight = FontWeight.Bold
        ),
        modifier = modifier
            .clip(RoundedCornerShape(SystemTheme.shapes.extraSmall))
            .background(colors.backgroundElevated.copy(alpha = 0.7f))
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
    val colors = SystemTheme.colors

    Text(
        text = "Найкращий розрахунковий максимум: ${OneRepMaxCalculator.format(max1RM)}${stringResource(R.string.text_unit_kg)}",
        style = MaterialTheme.typography.labelMedium.copy(
            color = colors.accentPrimary,
            fontWeight = FontWeight.Black
        ),
        modifier = modifier
            .clip(RoundedCornerShape(SystemTheme.shapes.extraSmall))
            .background(colors.backgroundElevated.copy(alpha = 0.84f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}
