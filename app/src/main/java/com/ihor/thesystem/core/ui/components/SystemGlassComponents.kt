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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import com.ihor.thesystem.core.theme.SystemColorTokens
import com.ihor.thesystem.core.theme.SystemCardPadding
import com.ihor.thesystem.core.theme.SystemControlHeight
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemRadiusPill
import com.ihor.thesystem.core.theme.SystemTheme

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
    val colors = SystemTheme.colors
    val shapes = SystemTheme.shapes
    val glow = SystemTheme.glow
    val shape = RoundedCornerShape(shapes.medium)
    Box(
        modifier = modifier
            .shadow(
                elevation = if (active) glow.activeElevation else glow.restingElevation,
                shape = shape,
                ambientColor = if (active) glow.activeAmbient else glow.shadowAmbient,
                spotColor = glow.shadowSpot
            )
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
                            if (active) colors.overlayStrong else colors.overlayMedium,
                            if (active) colors.borderActive else colors.borderSubtle,
                            colors.borderMuted
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
    accent: Color = SystemTheme.colors.accentPrimary,
    enabled: Boolean = true,
    glow: Boolean = false
) {
    val colors = SystemTheme.colors
    val shapes = SystemTheme.shapes
    val glowTokens = SystemTheme.glow
    val shape = RoundedCornerShape(shapes.medium)
    Row(
        modifier = modifier
            .height(SystemControlHeight)
            .shadow(
                elevation = if (glow && enabled) glowTokens.buttonActiveElevation else glowTokens.buttonElevation,
                shape = shape,
                ambientColor = accent.copy(alpha = if (glow && enabled) 0.28f else 0.06f),
                spotColor = glowTokens.shadowSpot
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        accent.copy(alpha = if (enabled) 0.18f else 0.06f),
                        colors.overlayLight.copy(alpha = if (enabled) colors.overlayLight.alpha else 0.02f)
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
                color = if (enabled) colors.textPrimary else colors.textMuted,
                fontWeight = FontWeight.Bold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SystemIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    accent: Color = SystemTheme.colors.textSecondary,
    active: Boolean = false
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.small)
    Box(
        modifier = modifier
            .size(SystemControlHeight)
            .clip(shape)
            .background(if (active) accent.copy(alpha = 0.12f) else colors.overlayLight)
            .border(1.dp, if (active) accent.copy(alpha = 0.34f) else colors.borderSubtle, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (active) accent else colors.textSecondary,
            modifier = Modifier.size(18.dp)
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
    val colors = SystemTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary),
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
    accent: Color = SystemTheme.colors.accentPrimary
) {
    val colors = SystemTheme.colors
    val motion = SystemTheme.motion
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(motion.progressMillis, easing = EaseOutCubic),
        label = "system_progress"
    )
    Box(
        modifier = modifier
            .height(7.dp)
            .clip(CircleShape)
            .background(colors.overlayMedium)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress)
                .fillMaxHeight()
                .background(
                    Brush.horizontalGradient(
                        listOf(accent.copy(alpha = 0.72f), accent, colors.accentAi.copy(alpha = 0.72f))
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
    accent: Color = SystemTheme.colors.accentPrimary,
    subtitle: String? = null
) {
    val colors = SystemTheme.colors
    SystemCard(modifier = modifier, contentPadding = SystemItemSpacing) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(color = colors.textSecondary, fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(color = colors.textPrimary, fontWeight = FontWeight.Black),
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
    accent: Color = SystemTheme.colors.accentPrimary,
    active: Boolean = false
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemRadiusPill)
    Row(
        modifier = modifier
            .clip(shape)
            .background(if (active) accent.copy(alpha = 0.12f) else colors.overlayLight)
            .border(1.dp, if (active) accent.copy(alpha = 0.38f) else colors.borderSubtle, shape)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(accent.copy(alpha = if (active) 1f else 0.52f)))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (active) colors.textPrimary else colors.textSecondary,
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
    accent: Color = SystemTheme.colors.accentPrimary
) {
    SystemCard(modifier = modifier.clickable(onClick = onClick), contentPadding = SystemCardPadding) {
        Row(horizontalArrangement = Arrangement.spacedBy(SystemItemSpacing), verticalAlignment = Alignment.CenterVertically) {
            SystemIconBubble(icon = icon, accent = accent)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall.copy(color = SystemTheme.colors.textPrimary, fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(color = SystemTheme.colors.textSecondary), maxLines = 2, overflow = TextOverflow.Ellipsis)
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
    numberLabel: String? = null,
    onAddMicrotask: (() -> Unit)? = null,
    compact: Boolean = false,
    isDragging: Boolean = false,
    onRemove: (() -> Unit)? = null
) {
    val colors = SystemTheme.colors
    val shapes = SystemTheme.shapes
    val shape = RoundedCornerShape(if (compact) shapes.small else shapes.medium)
    val numberShape = RoundedCornerShape(shapes.extraSmall)
    val borderColor = when {
        isDragging -> colors.accentPrimary.copy(alpha = 0.72f)
        isCompleted -> colors.borderMuted
        else -> colors.overlayMedium
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                if (isDragging) {
                    colors.accentPrimary.copy(alpha = 0.1f)
                } else {
                    if (isCompleted) colors.overlayLight.copy(alpha = 0.026f) else colors.overlayLight
                }
            )
            .border(1.dp, borderColor, shape)
            .clickable(onClick = onToggle)
            .padding(horizontal = if (compact) 10.dp else 12.dp, vertical = if (compact) 8.dp else 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (compact) 9.dp else 10.dp)
    ) {
        if (numberLabel != null) {
            Box(
                modifier = Modifier
                    .height(24.dp)
                    .widthIn(min = if (compact) 34.dp else 32.dp)
                    .clip(numberShape)
                    .background(colors.accentPrimary.copy(alpha = if (compact) 0.06f else 0.09f))
                    .border(1.dp, colors.accentPrimary.copy(alpha = if (compact) 0.16f else 0.24f), numberShape)
                    .padding(horizontal = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = numberLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isCompleted) colors.textMuted else colors.accentPrimary,
                        fontWeight = FontWeight.Black
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Box(
            modifier = Modifier
                .size(if (compact) 22.dp else 24.dp)
                .clip(CircleShape)
                .background(if (isCompleted) colors.accentSuccess.copy(alpha = 0.14f) else Color.Transparent)
                .border(1.dp, if (isCompleted) colors.accentSuccess.copy(alpha = 0.52f) else colors.borderSubtle, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isCompleted) Icons.Filled.Check else Icons.Filled.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isCompleted) colors.accentSuccess else colors.textMuted,
                modifier = Modifier.size(if (compact) 14.dp else 15.dp)
            )
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = (if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium).copy(
                color = if (isCompleted) colors.textSecondary.copy(alpha = 0.58f) else colors.textPrimary,
                fontWeight = FontWeight.SemiBold,
                textDecoration = if (isCompleted) TextDecoration.LineThrough else null
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (onAddMicrotask != null) {
            TodoActionIcon(
                icon = Icons.Filled.Add,
                tint = colors.accentPrimary,
                onClick = onAddMicrotask,
                compact = compact
            )
        }
        if (onRemove != null) {
            TodoActionIcon(
                icon = Icons.Filled.Close,
                tint = colors.textMuted,
                onClick = onRemove,
                compact = compact
            )
        }
    }
}

@Composable
private fun TodoActionIcon(
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit,
    compact: Boolean
) {
    val size = if (compact) 28.dp else 30.dp
    val shape = RoundedCornerShape(SystemTheme.shapes.small)
    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .background(tint.copy(alpha = if (compact) 0.055f else 0.075f))
            .border(1.dp, tint.copy(alpha = if (compact) 0.12f else 0.2f), shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(if (compact) 15.dp else 16.dp)
        )
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
        accent = if (isAi) SystemTheme.colors.accentAi else SystemTheme.colors.accentPrimary
    )
}

@Composable
fun SystemInsightBlock(
    text: String,
    modifier: Modifier = Modifier,
    accent: Color = SystemTheme.colors.accentAi
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(accent.copy(alpha = 0.08f))
            .border(1.dp, accent.copy(alpha = 0.18f), shape)
            .padding(SystemItemSpacing),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(accent))
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary, fontWeight = FontWeight.Medium)
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
    val colors = SystemTheme.colors
    DarkGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenCalendar),
        contentPadding = SystemCardPadding
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(SystemRadiusPill))
                        .clickable(onClick = onOpenCalendar)
                        .padding(start = 10.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = colors.accentPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = colors.accentPrimary,
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
                        color = colors.borderMuted,
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
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    Column(
        modifier = modifier
            .height(78.dp)
            .clip(shape)
            .background(if (day.isToday) colors.overlayMedium else colors.overlayLight.copy(alpha = 0.022f))
            .border(1.dp, dayBorderColor(day, colors), shape)
            .clickable(onClick = onSelectDay)
            .padding(vertical = 7.dp, horizontal = 3.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = day.label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = if (day.isToday) colors.accentPrimary else colors.textMuted,
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
                            color = colors.workDay.copy(alpha = if (day.status == SystemWeekDayStatus.NoData) 0.11f else 0.28f),
                            topLeft = Offset(markerLeft, markerTop),
                            size = Size(markerWidth, markerHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner)
                        )
                    }
                    SystemWeekDayVisualType.Training -> {
                        drawRoundRect(
                            color = colors.trainingDay.copy(alpha = 0.22f),
                            topLeft = Offset(markerLeft, markerTop),
                            size = Size(markerWidth, markerHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner)
                        )
                    }
                    SystemWeekDayVisualType.Mixed -> {
                        drawRoundRect(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    colors.mixedDayStart.copy(alpha = 0.32f),
                                    colors.mixedDayEnd.copy(alpha = 0.34f)
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
                            color = colors.overlayLight.copy(alpha = 0.018f),
                            topLeft = Offset(markerLeft, markerTop),
                            size = Size(markerWidth, markerHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner)
                        )
                    }
                }

                drawRoundRect(
                    color = markerStrokeColor(day, colors),
                    topLeft = Offset(markerLeft, markerTop),
                    size = Size(markerWidth, markerHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(corner, corner),
                    style = Stroke(width = if (day.isToday) 1.6.dp.toPx() else 1.dp.toPx())
                )

                when (day.status) {
                    SystemWeekDayStatus.Completed -> {
                        drawCircle(
                            color = colors.accentSuccess.copy(alpha = 0.16f),
                            radius = 7.dp.toPx(),
                            center = Offset(markerLeft + markerWidth - 4.dp.toPx(), markerTop + 5.dp.toPx())
                        )
                    }
                    SystemWeekDayStatus.Partial -> drawCircle(
                        color = colors.accentWarning.copy(alpha = 0.9f),
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
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
            if (day.status == SystemWeekDayStatus.Completed) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = colors.accentSuccess,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 5.dp, top = 1.dp)
                        .size(11.dp)
                )
            }
        }
    }
}

private fun dayBorderColor(day: SystemWeekDayModel, colors: SystemColorTokens): Color =
    when {
        day.isToday -> colors.borderActive
        day.status == SystemWeekDayStatus.Missed -> colors.accentWarning.copy(alpha = 0.42f)
        else -> colors.borderSubtle
    }

private fun markerStrokeColor(day: SystemWeekDayModel, colors: SystemColorTokens): Color =
    when {
        day.isToday -> colors.accentPrimary.copy(alpha = 0.76f)
        day.status == SystemWeekDayStatus.Missed -> colors.accentWarning.copy(alpha = 0.5f)
        day.status == SystemWeekDayStatus.NoData -> colors.borderMuted
        day.visualType == SystemWeekDayVisualType.Training -> colors.trainingDay.copy(alpha = 0.38f)
        day.visualType == SystemWeekDayVisualType.Work -> colors.workDay.copy(alpha = 0.28f)
        day.visualType == SystemWeekDayVisualType.Mixed -> colors.accentPrimary.copy(alpha = 0.3f)
        else -> colors.borderSubtle
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
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.extraLarge)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surfaceGlassStrong)
            .border(1.dp, colors.borderSubtle, shape)
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
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.overlayLight.copy(alpha = 0.035f))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SystemIconBubble(icon = Icons.Filled.FitnessCenter, accent = colors.accentPrimary)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(color = colors.textPrimary, fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary), maxLines = 1, overflow = TextOverflow.Ellipsis)
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
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)
    Box(
        modifier = modifier
            .size(38.dp)
            .clip(shape)
            .background(
                if (accent == colors.accentAi) colors.accentAiSoft else colors.accentPrimarySoft
            )
            .border(1.dp, accent.copy(alpha = 0.28f), shape),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(19.dp))
    }
}
