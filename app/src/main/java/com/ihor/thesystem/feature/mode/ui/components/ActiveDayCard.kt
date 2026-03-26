package com.ihor.thesystem.feature.mode.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.core.ui.components.sciPanel
import com.ihor.thesystem.feature.mode.viewmodel.ActiveDayUiModel

@Composable
fun ActiveDayCard(
    data: ActiveDayUiModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier            = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // ── Header ────────────────────────────────────────────────────
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                text          = "[ ДЕНЬ ${data.dayNumber} ]",
                color         = NeonCyan,
                fontFamily    = FontFamily.Monospace,
                fontWeight    = FontWeight.Bold,
                fontSize      = 16.sp,
                letterSpacing = 2.sp
            )
            data.debuffName?.let { debuff ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .sciPanel(NeonRed.copy(0.5f), NeonRed.copy(0.1f), 6.dp)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Filled.Warning, null, tint = NeonRed, modifier = Modifier.size(12.dp))
                    Text(debuff, color = NeonRed, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // ── Daily Tasks ───────────────────────────────────────────────
        if (data.dailyTasks.isNotEmpty()) {
            DaySection(
                title       = "ЩОДЕННІ ЗАВДАННЯ",
                accentColor = NeonCyan,
                content     = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        data.dailyTasks.forEach { task ->
                            DayTaskRow(name = task.name)
                        }
                    }
                }
            )
        }

        // ── Workout ───────────────────────────────────────────────────
        data.workoutName?.let { name ->
            DaySection(
                title       = "ОСНОВНИЙ КВЕСТ",
                accentColor = NeonGold,
                content     = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text       = name.uppercase(),
                            color      = NeonGold,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 13.sp
                        )
                        Spacer(Modifier.height(2.dp))
                        data.exercises.forEach { exercise ->
                            ExerciseRow(name = exercise)
                        }
                    }
                }
            )
        }

        if (data.workoutName == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .sciPanel(NeonCyanDim.copy(0.2f), PanelSurface, 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Bedtime, null, tint = NeonCyanDim, modifier = Modifier.size(16.dp))
                    Text(
                        text       = "ДЕНЬ ПАСИВНОГО ВІДНОВЛЕННЯ",
                        color      = NeonCyanDim,
                        fontFamily = FontFamily.Monospace,
                        fontSize   = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DaySection(
    title: String,
    accentColor: androidx.compose.ui.graphics.Color,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .sciPanel(accentColor.copy(0.4f), PanelSurface, 10.dp)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text          = title,
            color         = accentColor,
            fontFamily    = FontFamily.Monospace,
            fontWeight    = FontWeight.Bold,
            fontSize      = 11.sp,
            letterSpacing = 1.5.sp
        )
        content()
    }
}

@Composable
private fun DayTaskRow(name: String) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector        = Icons.Filled.Circle,
            contentDescription = null,
            tint               = NeonCyan.copy(alpha = 0.6f),
            modifier           = Modifier.size(6.dp)
        )
        Text(
            text       = name,
            color      = TextPrimary,
            fontFamily = FontFamily.Monospace,
            fontSize   = 12.sp
        )
    }
}

@Composable
private fun ExerciseRow(name: String) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .sciPanel(NeonGold.copy(0.15f), NeonGold.copy(0.04f), 4.dp)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector        = Icons.Filled.FitnessCenter,
            contentDescription = null,
            tint               = NeonGold.copy(alpha = 0.7f),
            modifier           = Modifier.size(13.dp)
        )
        Text(
            text       = name,
            color      = TextPrimary,
            fontFamily = FontFamily.Monospace,
            fontSize   = 12.sp
        )
    }
}