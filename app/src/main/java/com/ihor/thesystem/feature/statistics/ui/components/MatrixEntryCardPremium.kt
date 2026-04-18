package com.ihor.thesystem.feature.statistics.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.core.ui.components.OneRepMaxText
import com.ihor.thesystem.core.ui.components.RankBadge
import com.ihor.thesystem.feature.statistics.viewmodel.MatrixEntryUiModel

@Composable
fun MatrixEntryCardPremium(
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

    val cardAlpha by animateFloatAsState(targetValue = if (entry.isActive) 1f else 0.4f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White.copy(alpha = 0.02f))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(28.dp))
            .clickable(enabled = entry.isActive) { onCardClick() }
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.exerciseName.uppercase(),
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    )
                    OneRepMaxText(
                        weight = entry.currentWeight.toDouble(),
                        reps = 8,
                        label = "EST. 1RM: "
                    )
                }
                RankBadge(rank = entry.currentRank, size = 48.dp)
            }

            // Progress Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("ПРОГРЕС МАТРИЦІ", style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.3f)))
                    Text("${(entry.progressPercent * 100).toInt()}%", style = MaterialTheme.typography.labelSmall.copy(color = accentColor, fontWeight = FontWeight.Bold))
                }
                
                // Custom Matrix Progress Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(entry.progressPercent.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(Brush.horizontalGradient(listOf(accentColor.copy(alpha = 0.7f), accentColor)))
                    )
                }
            }

            // AI Suggestion Panel
            if (entry.nextRecommendedWeight != null || entry.lastAiFeedback != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = accentColor.copy(alpha = 0.05f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (entry.nextRecommendedWeight != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, null, tint = accentColor, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "NEXT: ${entry.nextRecommendedWeight}kg | ${entry.nextRecommendedSets}x${entry.nextRecommendedReps}",
                                    style = MaterialTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                        if (entry.lastAiFeedback != null) {
                            Text(
                                text = entry.lastAiFeedback!!,
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.6f), lineHeight = 16.sp)
                            )
                        }
                    }
                }
            }

            // Weight Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    WeightInfoBlock("START", entry.displayStart, Color.White.copy(alpha = 0.4f))
                    WeightInfoBlock("CURRENT", entry.displayCurrent, accentColor)
                    WeightInfoBlock("TARGET", entry.displayTarget, NeonGold)
                }

                IconButton(
                    onClick = onSetupClick,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                ) {
                    Icon(Icons.Outlined.Flag, null, tint = NeonGold, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun WeightInfoBlock(label: String, value: String, color: Color) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.2f), fontSize = 8.sp))
        Text(text = value, style = MaterialTheme.typography.titleSmall.copy(color = color, fontWeight = FontWeight.Black))
    }
}
