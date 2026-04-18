package com.ihor.thesystem.feature.statistics.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.core.ui.components.RankBadge
import com.ihor.thesystem.feature.statistics.viewmodel.StatisticsUiData

@Composable
fun PlayerStatsHeaderPremium(
    data: StatisticsUiData,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White.copy(alpha = 0.03f))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(32.dp))
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
            // Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RankBadge(rank = data.globalRank, size = 60.dp)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = data.playerName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Surface(
                            color = NeonGreen.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonGreen.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = data.playerClass.uppercase(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NeonGreen,
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                if (data.isPenaltyActive) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(NeonRed.copy(alpha = 0.1f))
                            .border(1.dp, NeonRed.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PriorityHigh, null, tint = NeonRed, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItemPremium(
                    icon = Icons.Default.CalendarToday,
                    label = "МІСЯЦЬ",
                    value = "${data.currentMonth}/12",
                    color = Color(0xFF00F0FF),
                    modifier = Modifier.weight(1f)
                )
                StatItemPremium(
                    icon = Icons.Default.BarChart,
                    label = "ТИЖДЕНЬ",
                    value = "${data.currentWeek}",
                    color = Color(0xFFB257FF),
                    modifier = Modifier.weight(1f)
                )
                StatItemPremium(
                    icon = Icons.Default.HistoryToggleOff,
                    label = "ЦИКЛ",
                    value = "${data.currentCycleDay}/4",
                    color = Color(0xFFFFB300),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatItemPremium(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.05f))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, null, tint = color.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 9.sp,
                letterSpacing = 1.sp
            )
        )
    }
}
