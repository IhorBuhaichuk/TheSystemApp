package com.ihor.thesystem.feature.statistics.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.ihor.thesystem.core.ui.components.OneRepMaxText
import com.ihor.thesystem.core.ui.components.RankBadge
import com.ihor.thesystem.core.ui.components.systemLargePanelShape
import com.ihor.thesystem.presentation.common.model.MatrixEntryUiModel

@Composable
fun MatrixEntryCardPremium(
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
    val cardAlpha by animateFloatAsState(
        targetValue = if (entry.isActive) 1f else 0.4f,
        label = "matrix-card-alpha"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
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
                            colors.overlayStrong,
                            accentColor.copy(alpha = 0.22f),
                            colors.borderSubtle
                        )
                    )
                ),
                shape
            )
            .clickable(enabled = entry.isActive) { onCardClick() }
    ) {
        Column(
            modifier = Modifier.padding(SystemScreenPadding),
            verticalArrangement = Arrangement.spacedBy(SystemCardPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.exerciseName.uppercase(),
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Black
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (entry.usesExternalLoad) {
                        OneRepMaxText(
                            weight = entry.currentWeight.toDouble(),
                            reps = 8,
                            label = "EST. 1RM: "
                        )
                    }
                }
                RankBadge(rank = entry.currentRank, size = 48.dp)
            }

            if (entry.usesExternalLoad) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Прогрес матриці",
                            style = MaterialTheme.typography.labelSmall.copy(color = colors.textMuted)
                        )
                        Text(
                            text = "${(entry.progressPercent * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = accentColor,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(colors.overlayMedium)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(entry.progressPercent.coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(accentColor.copy(alpha = 0.7f), accentColor)
                                    )
                                )
                        )
                    }
                }
            }

            if (entry.nextRecommendedWeight != null || entry.lastAiFeedback != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(SystemTheme.shapes.medium),
                    color = accentColor.copy(alpha = 0.05f),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.12f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (entry.nextRecommendedWeight != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "NEXT: ${entry.nextRecommendedWeight}kg | ${entry.nextRecommendedSets}x${entry.nextRecommendedReps}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = colors.textPrimary,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        entry.lastAiFeedback?.let { feedback ->
                            Text(
                                text = feedback,
                                style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    WeightInfoBlock("START", entry.displayStart, colors.textSecondary)
                    WeightInfoBlock("CURRENT", entry.displayCurrent, accentColor)
                    WeightInfoBlock("TARGET", entry.displayTarget, colors.accentWarning)
                }

                IconButton(
                    onClick = onSetupClick,
                    modifier = Modifier
                        .size(36.dp)
                        .background(colors.overlayLight, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Flag,
                        contentDescription = null,
                        tint = colors.accentWarning,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeightInfoBlock(label: String, value: String, color: Color) {
    val colors = SystemTheme.colors
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = colors.textMuted,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall.copy(
                color = color,
                fontWeight = FontWeight.Black
            )
        )
    }
}
