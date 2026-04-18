package com.ihor.thesystem.feature.mode.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.core.ui.components.OneRepMaxText
import com.ihor.thesystem.core.ui.components.sciPanel
import com.ihor.thesystem.feature.mode.viewmodel.ActiveDayUiModel
import com.ihor.thesystem.feature.mode.viewmodel.ExerciseWorkoutUiModel

@Composable
fun ActiveDayCard(
    data: ActiveDayUiModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .sciPanel(
                borderColor = NeonCyan.copy(alpha = 0.2f),
                backgroundColor = Color.White.copy(alpha = 0.03f),
                cornerCut = 16.dp
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text          = "[ ДЕНЬ ${data.dayNumber} ]",
                color         = NeonCyan,
                fontFamily    = TekoFamily,
                fontWeight    = FontWeight.Bold,
                fontSize      = 22.sp,
                letterSpacing = 2.sp
            )
            data.debuffName?.let { debuff ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .sciPanel(NeonRed.copy(0.4f), NeonRed.copy(0.05f), 8.dp)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(Icons.Filled.Warning, null, tint = NeonRed, modifier = Modifier.size(14.dp))
                    Text(
                        text = debuff.uppercase(),
                        color = NeonRed,
                        fontSize = 10.sp,
                        fontFamily = RajdhaniFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Daily Tasks
        if (data.dailyTasks.isNotEmpty()) {
            DaySection(
                title       = "DAILY PROTOCOL",
                accentColor = NeonCyan,
                content     = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        data.dailyTasks.forEach { quest ->
                            quest.tasks.forEach { task ->
                                DayTaskRow(name = task.name)
                            }
                        }
                    }
                }
            )
        }

        // Workout
        data.workoutName?.let { name ->
            DaySection(
                title       = "MAIN PROTOCOL",
                accentColor = NeonGold,
                content     = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text       = name.uppercase(),
                            color      = Color.White,
                            fontFamily = RajdhaniFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize   = 15.sp,
                            letterSpacing = 1.sp
                        )
                        data.exercises.forEach { exercise ->
                            ExerciseRow(exercise = exercise)
                        }
                    }
                }
            )
        }

        if (data.workoutName == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .sciPanel(Color.White.copy(0.1f), Color.White.copy(0.02f), 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.NightsStay, null, tint = NeonCyanDim, modifier = Modifier.size(20.dp))
                    Text(
                        text       = "ПАСИВНЕ ВІДНОВЛЕННЯ",
                        color      = Color.White.copy(alpha = 0.5f),
                        fontFamily = RajdhaniFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 13.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DaySection(
    title: String,
    accentColor: Color,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(width = 3.dp, height = 14.dp).background(accentColor))
            Spacer(Modifier.width(8.dp))
            Text(
                text          = title,
                color         = accentColor,
                fontFamily    = RajdhaniFamily,
                fontWeight    = FontWeight.Bold,
                fontSize      = 12.sp,
                letterSpacing = 2.sp
            )
        }
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .sciPanel(Color.White.copy(0.05f), Color.White.copy(0.01f), 10.dp)
                .padding(14.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun DayTaskRow(name: String) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(NeonCyan.copy(alpha = 0.6f), CircleShape)
                .border(1.dp, NeonCyan, CircleShape)
        )
        Text(
            text       = name,
            color      = Color.White.copy(alpha = 0.8f),
            fontFamily = RajdhaniFamily,
            fontSize   = 14.sp
        )
    }
}

@Composable
private fun ExerciseRow(exercise: ExerciseWorkoutUiModel) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(8.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector        = Icons.Filled.FitnessCenter,
            contentDescription = null,
            tint               = NeonGold,
            modifier           = Modifier.size(16.dp)
        )
        Column {
            Text(
                text       = exercise.name,
                color      = Color.White,
                fontFamily = RajdhaniFamily,
                fontWeight = FontWeight.Medium,
                fontSize   = 14.sp
            )
            exercise.recommendation?.let { rec ->
                val weightPart = rec.lowercase().split("кг").firstOrNull()?.trim()?.toDoubleOrNull()
                val repsPart = rec.lowercase().split("x").lastOrNull()?.trim()?.toIntOrNull()

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                    Text(
                        text = rec,
                        color = NeonCyan,
                        fontFamily = RajdhaniFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    
                    if (weightPart != null && repsPart != null) {
                        OneRepMaxText(weight = weightPart, reps = repsPart)
                    }
                }
            }
        }
    }
}
