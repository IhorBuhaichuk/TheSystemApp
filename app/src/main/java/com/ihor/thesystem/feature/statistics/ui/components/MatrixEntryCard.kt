package com.ihor.thesystem.feature.statistics.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ihor.thesystem.core.theme.SystemCardPadding
import com.ihor.thesystem.core.theme.SystemScreenPadding
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.toSystemSentenceCase
import com.ihor.thesystem.core.ui.components.OneRepMaxText
import com.ihor.thesystem.core.ui.components.RankBadge
import com.ihor.thesystem.core.ui.components.systemLargePanelShape
import com.ihor.thesystem.core.ui.components.systemClickable
import com.ihor.thesystem.presentation.common.model.MatrixEntryUiModel

@Composable
fun MatrixEntryCard(
    entry: MatrixEntryUiModel,
    onCardClick: () -> Unit,
    onSetupClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    val accentColor: Color = when {
        entry.progressPercent >= 1f -> colors.accentSuccess
        entry.progressPercent >= 0.5f -> colors.accentPrimary
        else -> colors.accentInfo
    }
    val shape = systemLargePanelShape()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (entry.isActive) 1f else 0.4f)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        colors.surfaceGlassStrong,
                        colors.surfaceGlass,
                        colors.surfaceGlassSoft
                    )
                )
            )
            .border(
                BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        listOf(
                            accentColor.copy(alpha = 0.28f),
                            colors.accentAi.copy(alpha = 0.22f),
                            colors.borderSubtle
                        )
                    )
                ),
                shape
            )
            .systemClickable(enabled = entry.isActive) { onCardClick() }
            .padding(SystemScreenPadding),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.exerciseName.toSystemSentenceCase(),
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (entry.usesExternalLoad) {
                    OneRepMaxText(
                        weight = entry.currentWeight.toDouble(),
                        reps = 8,
                        label = "Est. 1RM: "
                    )
                }
            }

            RankBadge(rank = entry.currentRank, size = 44.dp)
        }

        if (entry.usesExternalLoad && entry.weightHistory.size >= 2) {
            ExerciseProgressChart(
                history = entry.weightHistory,
                accentColor = accentColor,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        if (entry.usesExternalLoad) {
            MatrixProgressBar(
                progress = entry.progressPercent,
                accentColor = accentColor
            )
        }

        if (entry.nextRecommendedWeight != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Наступне тренування: ",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = colors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = "${entry.nextRecommendedWeight} кг, ${entry.nextRecommendedSets}x, ${entry.nextRecommendedReps}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = colors.accentPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }

        if (entry.lastAiFeedback != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = "Coach AI",
                    tint = colors.accentPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = entry.lastAiFeedback,
                    style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(SystemCardPadding)
            ) {
                WeightBlock("Старт", entry.displayStart, colors.textSecondary)
                WeightBlock("Зараз", entry.displayCurrent, accentColor)
                WeightBlock("Ціль", entry.displayTarget, colors.accentWarning)
            }

            IconButton(
                onClick = onSetupClick,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Flag,
                    contentDescription = "Setup Goals",
                    tint = colors.accentWarning,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun WeightBlock(label: String, value: String, valueColor: Color) {
    val colors = SystemTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = colors.textSecondary.copy(alpha = 0.62f),
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                color = valueColor,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
