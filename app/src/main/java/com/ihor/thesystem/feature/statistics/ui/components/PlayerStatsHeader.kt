package com.ihor.thesystem.feature.statistics.ui.components

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
import com.ihor.thesystem.feature.statistics.viewmodel.StatisticsUiData

@Composable
fun PlayerStatsHeader(
    data: StatisticsUiData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .sciPanel(PanelBorder, PanelSurface, 12.dp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Name + class
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text       = data.playerName,
                    color      = NeonCyan,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 24.sp
                )
                Text(
                    text       = "[ ${data.playerClass} ]",
                    color      = TextSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 11.sp
                )
            }
            if (data.isPenaltyActive) {
                Row(
                    modifier = Modifier
                        .sciPanel(NeonRed.copy(0.5f), NeonRed.copy(0.1f), 6.dp)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Warning, null, tint = NeonRed, modifier = Modifier.size(13.dp))
                    Text("ШТРАФ", color = NeonRed, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
                }
            }
        }

        // Stats row
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatChip(
                icon  = Icons.Filled.CalendarMonth,
                label = "МІСЯЦЬ",
                value = "${data.currentMonth}/12",
                modifier = Modifier.weight(1f)
            )
            StatChip(
                icon  = Icons.Filled.BarChart,
                label = "ТИЖДЕНЬ",
                value = "${data.currentWeek}",
                modifier = Modifier.weight(1f)
            )
            StatChip(
                icon  = Icons.Filled.Loop,
                label = "ДЕНЬ ЦИКЛУ",
                value = "${data.currentCycleDay}/4",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .sciPanel(NeonCyan.copy(0.2f), NeonCyan.copy(0.05f), 8.dp)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, null, tint = NeonCyanDim, modifier = Modifier.size(14.dp))
        Text(
            text       = value,
            color      = TextPrimary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize   = 14.sp
        )
        Text(
            text       = label,
            color      = TextSecondary,
            fontFamily = FontFamily.Monospace,
            fontSize   = 8.sp
        )
    }
}