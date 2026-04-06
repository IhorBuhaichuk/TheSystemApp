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
import com.ihor.thesystem.core.ui.components.RankBadge
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
        // --- Top Row: Name + Rank Badge ---
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.Top
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
            
            // Ранг вправи спозиціонований праворуч
            RankBadge(rank = entry.currentRank, size = 44.dp)
        }

        // --- Progress Chart ---
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

        // --- AI Recommendation Row ---
        if (entry.nextRecommendedWeight != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "НАСТУПНЕ ТРЕНУВАННЯ: ",
                    color = TextSecondary,
                    fontFamily = RajdhaniFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${entry.nextRecommendedWeight}кг, ${entry.nextRecommendedSets}x, ${entry.nextRecommendedReps}",
                    color = NeonCyan,
                    fontFamily = TekoFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // --- Bottom Row: Weights + Setup Button ---
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.Bottom
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                WeightBlock("СТАРТ",    entry.displayStart,   TextSecondary)
                WeightBlock("ЗАРАЗ",    entry.displayCurrent, accentColor)
                WeightBlock("ЦІЛЬ",     entry.displayTarget,  NeonGold)
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
