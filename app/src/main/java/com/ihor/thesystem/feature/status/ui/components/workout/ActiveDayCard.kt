package com.ihor.thesystem.feature.status.ui.components.workout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.feature.status.viewmodel.ActiveDayUiModel
import com.ihor.thesystem.feature.statistics.viewmodel.MatrixEntryUiModel

@Composable
fun ActiveDayCard(
    data: ActiveDayUiModel,
    onOpenLogSets: (MatrixEntryUiModel) -> Unit,
    onOpenSetup: (MatrixEntryUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (data.exercises.isNotEmpty()) {
            data.exercises.forEach { exercise ->
                val matrixEntry = data.matrixEntries.find { it.exerciseId == exercise.exerciseId }
                
                ExerciseItem(
                    name = exercise.name,
                    recommendation = exercise.recommendation,
                    matrixEntry = matrixEntry,
                    onLogSets = { matrixEntry?.let(onOpenLogSets) },
                    onSetup = { matrixEntry?.let(onOpenSetup) }
                )
            }
        }
    }
}

@Composable
private fun ExerciseItem(
    name: String,
    recommendation: String?,
    matrixEntry: MatrixEntryUiModel?,
    onLogSets: () -> Unit,
    onSetup: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .clickable { isExpanded = !isExpanded }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(NeonCyan.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name.uppercase(),
                    color = Color.White,
                    fontSize = 18.sp,
                    fontFamily = RajdhaniFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                if (recommendation != null) {
                    Text(
                        text = recommendation,
                        color = NeonCyan,
                        fontSize = 13.sp,
                        fontFamily = RajdhaniFamily,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.3f)
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxWidth()
            ) {
                if (matrixEntry != null) {
                    // Progress Bar
                    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ПРОГРЕС ВПРАВИ",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "${(matrixEntry.progressPercent * 100).toInt()}%",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.05f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(matrixEntry.progressPercent.coerceIn(0f, 1f))
                                    .fillMaxHeight()
                                    .clip(CircleShape)
                                    .background(Brush.horizontalGradient(listOf(NeonCyan, Color(0xFFB257FF))))
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem("Поточна", matrixEntry.displayCurrent)
                        StatItem("Ціль", matrixEntry.displayTarget)
                        StatItem("Ранг", matrixEntry.currentRank.name)
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onLogSets,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("РОЗПОЧАТИ СЕТ", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        IconButton(
                            onClick = onSetup,
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                                .size(48.dp)
                        ) {
                            Icon(Icons.Default.Settings, null, tint = Color.White.copy(alpha = 0.5f))
                        }
                    }
                } else {
                    Text(
                        "Дані матриці прогресії не знайдені",
                        color = Color.White.copy(alpha = 0.3f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column {
        Text(label, color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
