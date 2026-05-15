package com.ihor.thesystem.feature.statistics.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemScreenPadding
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.asUiText
import com.ihor.thesystem.feature.statistics.viewmodel.StatisticsUiData

@Composable
fun PlayerStatsHeaderPremium(
    data: StatisticsUiData,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.extraLarge)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surfaceGlassSoft)
            .border(1.dp, colors.borderSubtle, shape)
            .padding(SystemScreenPadding)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(SystemScreenPadding)) {
            // Top Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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
                                .border(2.dp, colors.borderSubtle, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(colors.backgroundElevated)
                                .border(1.dp, colors.borderSubtle, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = colors.textMuted
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = data.playerName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = colors.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Surface(
                            color = colors.accentSuccess.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(SystemTheme.shapes.extraSmall),
                            border = BorderStroke(1.dp, colors.accentSuccess.copy(alpha = 0.2f))
                        ) {
                            Text(
                                text = data.playerClass.asUiText().asString(context).uppercase(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = colors.accentSuccess,
                                    letterSpacing = 0.sp,
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
                            .background(colors.accentError.copy(alpha = 0.1f))
                            .border(1.dp, colors.accentError.copy(alpha = 0.3f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PriorityHigh, null, tint = colors.accentError, modifier = Modifier.size(20.dp))
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
                    color = colors.accentPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatItemPremium(
                    icon = Icons.Default.BarChart,
                    label = "ТИЖДЕНЬ",
                    value = "${data.currentWeek}",
                    color = colors.accentAi,
                    modifier = Modifier.weight(1f)
                )
                StatItemPremium(
                    icon = Icons.Default.HistoryToggleOff,
                    label = "ЦИКЛ",
                    value = "${data.currentCycleDay}/4",
                    color = colors.accentWarning,
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
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.large)
    Column(
        modifier = modifier
            .clip(shape)
            .background(color.copy(alpha = 0.05f))
            .padding(SystemItemSpacing),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, null, tint = color.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                color = colors.textPrimary,
                fontWeight = FontWeight.ExtraBold
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = colors.textMuted,
                fontSize = 9.sp,
                letterSpacing = 0.sp
            )
        )
    }
}
