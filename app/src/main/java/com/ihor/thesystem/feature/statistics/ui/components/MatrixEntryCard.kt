package com.ihor.thesystem.feature.statistics.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.core.ui.components.OneRepMaxText
import com.ihor.thesystem.core.ui.components.sciPanel
import com.ihor.thesystem.feature.statistics.viewmodel.MatrixEntryUiModel

@Composable
fun MatrixEntryCard(
    entry: MatrixEntryUiModel,
    onCardClick: () -> Unit,
    onSetupClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor: Color = when {
        entry.progressPercent >= 1f  -> NeonGreen
        entry.progressPercent >= 0.5f -> NeonCyan
        else                          -> NeonCyanDim
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (entry.isActive) 1f else 0.4f)
            .sciPanel(accentColor.copy(0.35f), PanelSurface, 8.dp)
            .clickable(enabled = entry.isActive) { onCardClick() }
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = entry.exerciseName.uppercase(),
                    color      = TextPrimary,
                    fontFamily = RajdhaniFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 13.sp
                )
                OneRepMaxText(
                    weight = entry.currentWeight.toDouble(), 
                    reps = 8,
                    label = "EST. 1RM: "
                )
            }
            IconButton(
                onClick = onSetupClick,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Flag,
                    contentDescription = "Setup Goals",
                    tint = NeonGold,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // ВСТАВКА ГРАФІКА ПРОГРЕСУ
        if (entry.weightHistory.size >= 2) {
            ExerciseProgressChart(
                history = entry.weightHistory,
                accentColor = accentColor,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        MatrixProgressBar(
            progress    = entry.progressPercent,
            accentColor = accentColor
        )

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            WeightBlock("СТАРТ",    entry.displayStart,   TextSecondary)
            WeightBlock("ЗАРАЗ",    entry.displayCurrent, accentColor)
            WeightBlock("ЦІЛЬ",     entry.displayTarget,  NeonGold)
        }
    }
}

@Composable
private fun WeightBlock(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text       = label,
            color      = TextSecondary.copy(alpha = 0.5f),
            fontFamily = RajdhaniFamily,
            fontSize   = 9.sp
        )
        Text(
            text       = value,
            color      = valueColor,
            fontFamily = TekoFamily,
            fontWeight = FontWeight.Bold,
            fontSize   = 14.sp
        )
    }
}
