package com.ihor.thesystem.core.ui.components

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import com.ihor.thesystem.core.theme.AccentAi
import com.ihor.thesystem.core.theme.AccentAiSoft
import com.ihor.thesystem.core.theme.AccentPrimary
import com.ihor.thesystem.core.theme.AccentPrimarySoft
import com.ihor.thesystem.core.theme.AccentSuccess
import com.ihor.thesystem.core.theme.AccentWarning
import com.ihor.thesystem.core.theme.BorderActive
import com.ihor.thesystem.core.theme.BorderSubtle
import com.ihor.thesystem.core.theme.MixedDayGradientEnd
import com.ihor.thesystem.core.theme.MixedDayGradientStart
import com.ihor.thesystem.core.theme.SystemCardPadding
import com.ihor.thesystem.core.theme.SystemRadiusMedium
import com.ihor.thesystem.core.theme.SystemSurfaceGlass
import com.ihor.thesystem.core.theme.SystemSurfaceGlassStrong
import com.ihor.thesystem.core.theme.TextMuted
import com.ihor.thesystem.core.theme.TextPrimary
import com.ihor.thesystem.core.theme.TextSecondary
import com.ihor.thesystem.core.theme.TrainingDayColor
import com.ihor.thesystem.core.theme.WorkDayColor

@Composable
fun DarkGlassCard(
    modifier: Modifier = Modifier,
    active: Boolean = false,
    contentPadding: Dp = SystemCardPadding,
    content: @Composable () -> Unit
) {
    SystemCard(
        modifier = modifier,
        active = active,
        contentPadding = contentPadding,
        content = content
    )
}

@Composable
fun SystemCard(
    modifier: Modifier = Modifier,
    active: Boolean = false,
    contentPadding: Dp = SystemCardPadding,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(SystemRadiusMedium)
    Box(
        modifier = modifier
            .shadow(
                elevation = if (active) 22.dp else 14.dp,
                shape = shape,
                ambientColor = if (active) AccentPrimary.copy(alpha = 0.16f) else Color.Black.copy(alpha = 0.36f),
                spotColor = Color.Black.copy(alpha = 0.44f)
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        SystemSurfaceGlassStrong,
                        SystemSurfaceGlass,
                        SystemSurfaceGlass.copy(alpha = 0.46f)
                    )
                )
            )
            .border(
                BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = if (active) 0.18f else 0.11f),
                            if (active) BorderActive else BorderSubtle,
                            Color.White.copy(alpha = 0.04f)
                        )
                    )
                ),
                shape
            )
            .padding(contentPadding)
    ) {
        content()
    }
}

@Composable
fun SystemButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    accent: Color = AccentPrimary,
    enabled: Boolean = true,
    glow: Boolean = false
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = modifier
            .height(48.dp)
            .shadow(
                elevation = if (glow && enabled) 18.dp else 6.dp,
                shape = shape,
                ambientColor = accent.copy(alpha = if (glow && enabled) 0.28f else 0.06f),
                spotColor = Color.Black.copy(alpha = 0.4f)
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        accent.copy(alpha = if (enabled) 0.18f else 0.06f),
                        Color.White.copy(alpha = if (enabled) 0.045f else 0.02f)
                    )
                )
            )
            .border(1.dp, accent.copy(alpha = if (enabled) 0.52f else 0.16f), shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                color = if (enabled) TextPrimary else TextMuted,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SystemSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        trailing?.invoke()
    }
}

@Composable
fun SystemProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    accent: Color = AccentPrimary
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(700, easing = EaseOutCubic),
        label = "system_progress"
    )
    Box(
        modifier = modifier
            .height(7.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.07f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        listOf(accent.copy(alpha = 0.72f), accent, AccentAi.copy(alpha = 0.72f))
                    )
                )
        )
    }
}

@Composable
fun SystemMetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color = AccentPrimary,
    subtitle: String? = null
) {
    SystemCard(modifier = modifier, contentPadding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(color = TextPrimary, fontWeight = FontWeight.Black),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = accent),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SystemStatusChip(
    text: String,
    modifier: Modifier = Modifier,
    accent: Color = AccentPrimary,
    active: Boolean = false
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (active) accent.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.045f))
            .border(1.dp, if (active) accent.copy(alpha = 0.38f) else BorderSubtle, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(accent.copy(alpha = if (active) 1f else 0.52f)))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (active) TextPrimary else TextSecondary,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SystemActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = AccentPrimary
) {
    SystemCard(modifier = modifier.clickable(onClick = onClick), contentPadding = 14.dp) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            SystemIconBubble(icon = icon, accent = accent)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary), maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun SystemTodoItem(
    title: String,
    isCompleted: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    onRemove: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(Color.White.copy(alpha = if (isCompleted) 0.026f else 0.04f))
            .border(1.dp, Color.White.copy(alpha = if (isCompleted) 0.045f else 0.075f), RoundedCornerShape(13.dp))
            .clickable(onClick = onToggle)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isCompleted) AccentSuccess.copy(alpha = 0.14f) else Color.Transparent)
                .border(1.dp, if (isCompleted) AccentSuccess.copy(alpha = 0.52f) else BorderSubtle, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isCompleted) Icons.Filled.Check else Icons.Filled.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isCompleted) AccentSuccess else TextMuted,
                modifier = Modifier.size(15.dp)
            )
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = if (isCompleted) TextSecondary.copy(alpha = 0.58f) else TextPrimary,
                fontWeight = FontWeight.SemiBold,
                textDecoration = if (isCompleted) TextDecoration.LineThrough else null
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (onRemove != null) {
            Text(
                text = "x",
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onRemove)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelMedium.copy(color = TextMuted, fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun SystemModuleButton(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.FitnessCenter,
    isAi: Boolean = false
) {
    SystemActionTile(
        title = title,
        subtitle = subtitle,
        icon = icon,
        onClick = onClick,
        modifier = modifier,
        accent = if (isAi) AccentAi else AccentPrimary
    )
}

@Composable
fun SystemInsightBlock(
    text: String,
    modifier: Modifier = Modifier,
    accent: Color = AccentAi
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(accent.copy(alpha = 0.08f))
            .border(1.dp, accent.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
            .padding(13.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(accent))
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontWeight = FontWeight.Medium)
        )
    }
}

enum class SystemWeekDayVisualType {
    Work,
    Training,
    Mixed,
    Rest
}

enum class SystemWeekDayStatus {
    Completed,
    Partial,
    Missed,
    Planned,
    NoData
}

data class SystemWeekDayModel(
    val date: LocalDate,
    val label: String,
    val dayNumber: String,
    val visualType: SystemWeekDayVisualType,
    val status: SystemWeekDayStatus,
    val isToday: Boolean
)

@Composable
fun SystemWeekCalendarPreview(
    days: List<SystemWeekDayModel>,
    onOpenCalendar: () -> Unit,
    onSelectDay: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Твій тиждень",
    actionLabel: String = "Відкрити"
) {
    DarkGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenCalendar),
        contentPadding = 14.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .clickable(onClick = onOpenCalendar)
                        .padding(start = 10.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = AccentPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = AccentPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Box(modifier = Modifier.fillMaxWidth()) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(74.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    val y = size.height * 0.53f
                    drawLine(
                        color = Color.White.copy(alpha = 0.055f),
                        start = Offset(size.width * 0.04f, y),
                        end = Offset(size.width * 0.96f, y),
                        strokeWidth = 1.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    days.forEach { day ->
                        SystemWeekDayCell(
                            day = day,
                            onSelectDay = { onSelectDay(day.date) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SystemWeekDayCell(
    day: SystemWeekDayModel,
    onSelectDay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(13.dp)
    Column(
        modifier = modifier
            .height(78.dp)
            .clip(shape)
            .background(Color.White.copy(alpha = if (day.isToday) 0.05f else 0.022f))
            .border(1.dp, dayBorderColor(day), shape)
            .clickable(onClick = onSelectDay)
            .padding(vertical = 7.dp, horizontal = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = day.label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (day.isToday) AccentPrimary else TextMuted,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val markerWidth = size.width * 0.72f
                val markerHeight = 28.dp.toPx()
                val markerTop = (size.height - markerHeight) / 2f
                val markerLeft = (size.width - markerWidth) / 2f
                val corner = 12.dp.toPx()

                when (day.visualType) {
                    SystemWeekDayVisualType.Work -> {
                        drawRoundRect(
                            color = WorkDayColor.copy(alpha = if (day.status == SystemWeekDayStatus.NoData) 0.11f else 0.28f),
                            topLeft = Offset(markerLeft, markerTop),
                            size = Size(markerWidth, markerHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner)
                        )
                    }
                    SystemWeekDayVisualType.Training -> {
                        drawRoundRect(
                            color = TrainingDayColor.copy(alpha = 0.22f),
                            topLeft = Offset(markerLeft, markerTop),
                            size = Size(markerWidth, markerHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner)
                        )
                    }
                    SystemWeekDayVisualType.Mixed -> {
                        drawRoundRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MixedDayGradientStart.copy(alpha = 0.32f),
                                    MixedDayGradientEnd.copy(alpha = 0.34f)
                                ),
                                start = Offset(markerLeft, markerTop),
                                end = Offset(markerLeft + markerWidth, markerTop + markerHeight)
                            ),
                            topLeft = Offset(markerLeft, markerTop),
                            size = Size(markerWidth, markerHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner)
                        )
                    }
                    SystemWeekDayVisualType.Rest -> {
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.018f),
                            topLeft = Offset(markerLeft, markerTop),
                            size = Size(markerWidth, markerHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner)
                        )
                    }
                }

                drawRoundRect(
                    color = markerStrokeColor(day),
                    topLeft = Offset(markerLeft, markerTop),
                    size = Size(markerWidth, markerHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner),
                    style = Stroke(width = if (day.isToday) 1.6.dp.toPx() else 1.dp.toPx())
                )

                when (day.status) {
                    SystemWeekDayStatus.Completed -> {
                        drawCircle(
                            color = AccentSuccess.copy(alpha = 0.16f),
                            radius = 7.dp.toPx(),
                            center = Offset(markerLeft + markerWidth - 4.dp.toPx(), markerTop + 5.dp.toPx())
                        )
                    }
                    SystemWeekDayStatus.Partial -> drawCircle(
                        color = AccentWarning.copy(alpha = 0.9f),
                        radius = 2.6.dp.toPx(),
                        center = Offset(markerLeft + markerWidth - 5.dp.toPx(), markerTop + 5.dp.toPx())
                    )
                    SystemWeekDayStatus.Missed,
                    SystemWeekDayStatus.Planned,
                    SystemWeekDayStatus.NoData -> Unit
                }
            }
            Text(
                text = day.dayNumber,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
            if (day.status == SystemWeekDayStatus.Completed) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = AccentSuccess,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 5.dp, top = 1.dp)
                        .size(11.dp)
                )
            }
        }
    }
}

private fun dayBorderColor(day: SystemWeekDayModel): Color =
    when {
        day.isToday -> BorderActive
        day.status == SystemWeekDayStatus.Missed -> AccentWarning.copy(alpha = 0.42f)
        else -> BorderSubtle
    }

private fun markerStrokeColor(day: SystemWeekDayModel): Color =
    when {
        day.isToday -> AccentPrimary.copy(alpha = 0.76f)
        day.status == SystemWeekDayStatus.Missed -> AccentWarning.copy(alpha = 0.5f)
        day.status == SystemWeekDayStatus.NoData -> Color.White.copy(alpha = 0.055f)
        day.visualType == SystemWeekDayVisualType.Training -> TrainingDayColor.copy(alpha = 0.38f)
        day.visualType == SystemWeekDayVisualType.Work -> WorkDayColor.copy(alpha = 0.28f)
        day.visualType == SystemWeekDayVisualType.Mixed -> AccentPrimary.copy(alpha = 0.3f)
        else -> BorderSubtle
    }

@Composable
fun SystemMonthCalendar(
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    SystemCard(modifier = modifier, content = content)
}

@Composable
fun SystemBottomNav(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(SystemSurfaceGlassStrong)
            .border(1.dp, BorderSubtle, RoundedCornerShape(26.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
fun SystemExerciseRow(
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(Color.White.copy(alpha = 0.035f))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SystemIconBubble(icon = Icons.Filled.FitnessCenter, accent = AccentPrimary)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun SystemIconBubble(
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (accent == AccentAi) AccentAiSoft else AccentPrimarySoft
            )
            .border(1.dp, accent.copy(alpha = 0.28f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(19.dp))
    }
}
