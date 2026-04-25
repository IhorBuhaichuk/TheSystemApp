package com.ihor.thesystem.feature.statistics.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.R
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.core.ui.components.RankBadge
import com.ihor.thesystem.core.ui.components.sciPanel
import com.ihor.thesystem.feature.statistics.viewmodel.StatisticsUiData
import coil.compose.AsyncImage
import coil.request.ImageRequest

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
        // Name + class + RANK
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (data.avatarUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(data.avatarUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(2.dp, NeonCyan.copy(alpha = 0.3f), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Black)
                            .border(1.dp, NeonCyan.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = NeonCyan.copy(alpha = 0.5f)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text       = data.playerName,
                        color      = TextPrimary,
                        fontFamily = RajdhaniFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp
                    )
                    Text(
                        text       = "[ ${data.playerClass} ]",
                        color      = TextSecondary,
                        fontFamily = RajdhaniFamily,
                        fontSize   = 11.sp
                    )
                }
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
                    Text(
                        text = stringResource(R.string.text_penalty), 
                        color = NeonRed, 
                        fontFamily = RajdhaniFamily, 
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Stats row
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatChip(
                icon = Icons.Filled.CalendarMonth, 
                label = stringResource(R.string.text_month), 
                value = "${data.currentMonth}/12"
            )
            StatChip(
                icon = Icons.Filled.BarChart, 
                label = stringResource(R.string.text_week), 
                value = "${data.currentWeek}"
            )
            StatChip(
                icon = Icons.Filled.Loop, 
                label = stringResource(R.string.text_day), 
                value = "${data.currentCycleDay}/4"
            )
        }
    }
}

@Composable
private fun RowScope.StatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .sciPanel(NeonCyan.copy(0.2f), NeonCyan.copy(0.05f), 8.dp)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, null, tint = NeonCyanDim, modifier = Modifier.size(14.dp))
        Text(
            text       = value,
            color      = TextPrimary,
            fontFamily = TekoFamily,
            fontWeight = FontWeight.Bold,
            fontSize   = 18.sp
        )
        Text(
            text       = label,
            color      = TextSecondary,
            fontFamily = RajdhaniFamily,
            fontSize   = 8.sp
        )
    }
}
