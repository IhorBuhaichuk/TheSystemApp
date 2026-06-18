package com.ihor.thesystem.feature.status.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.SystemCardPadding
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemScreenPadding
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.components.DarkGlassCard
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemProgressBar
import com.ihor.thesystem.core.ui.components.SystemSectionHeader
import com.ihor.thesystem.core.ui.components.SystemWeekCalendarPreview
import com.ihor.thesystem.core.ui.components.SystemWeekDayModel
import com.ihor.thesystem.core.ui.components.SystemWeekDayStatus
import com.ihor.thesystem.core.ui.components.SystemWeekDayVisualType
import com.ihor.thesystem.domain.model.BossFight
import com.ihor.thesystem.domain.model.BossFightStatus
import com.ihor.thesystem.domain.model.BossFightTargetMetric
import com.ihor.thesystem.feature.status.viewmodel.QuestUiModel
import com.ihor.thesystem.feature.status.viewmodel.StatusUiData
import com.ihor.thesystem.feature.status.viewmodel.StatusWeekDayStatus
import com.ihor.thesystem.feature.status.viewmodel.StatusWeekDayUiModel
import com.ihor.thesystem.feature.status.viewmodel.StatusWeekDayVisualType
import com.ihor.thesystem.feature.status.viewmodel.TaskUiModel
import com.ihor.thesystem.feature.status.viewmodel.TodoUiModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun RpgStatusDashboard(
    data: StatusUiData,
    onAvatarSelected: (Uri) -> Unit,
    onEditNameTap: () -> Unit,
    onStartWorkout: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenWorkoutSettings: () -> Unit,
    onTaskToggled: (TodoUiModel) -> Unit,
    onAddTask: (Int) -> Unit,
    onAddMicrotask: (TodoUiModel) -> Unit,
    onTodosReordered: (List<Int>) -> Unit,
    onRemoveTask: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = SystemScreenPadding)
            .padding(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)
    ) {
        TodayOrderBlock(
            decision = data.todayDecision,
            fallbackMainQuest = data.mainQuest,
            onStartWorkout = onStartWorkout
        )
        data.activeBossFight?.let { bossFight ->
            BossFightBlock(
                bossFight = bossFight,
                onOpen = onStartWorkout,
                modifier = Modifier.fillMaxWidth()
            )
        }
        WeekPreviewBlock(
            days = data.weekPreview,
            onOpenCalendar = onOpenCalendar,
            modifier = Modifier.fillMaxWidth()
        )
        SystemLevelBlock(data = data)
        TodoBlock(
            todos = data.todos,
            onTaskToggled = onTaskToggled,
            onAddTask = onAddTask,
            onAddMicrotask = onAddMicrotask,
            onTodosReordered = onTodosReordered,
            onRemoveTask = onRemoveTask
        )
    }
}

@Composable
private fun BossFightBlock(
    bossFight: BossFight,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    val accent = colors.accentWarning
    val isActive = bossFight.status == BossFightStatus.ACTIVE

    DarkGlassCard(modifier = modifier, contentPadding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SystemSectionHeader(
                title = "Контрольний норматив",
                subtitle = "Ранг ${bossFight.rankFrom.name} -> ${bossFight.rankTo.name}"
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SystemItemSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FocusIcon(
                    icon = Icons.Filled.FitnessCenter,
                    tint = accent
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        text = bossFight.title.removePrefix("Контрольний норматив: ").ifBlank { bossFight.title },
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = bossFight.rulesText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = colors.textSecondary,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SystemStateBadge(
                    text = bossFight.targetLabel(),
                    accent = accent
                )
                SystemStateBadge(
                    text = "Нагорода: ${bossFight.rankTo.name}",
                    accent = colors.accentSuccess
                )
            }
            SystemButton(
                text = if (isActive) "Почати" else "Відкрити норматив",
                icon = Icons.Filled.FitnessCenter,
                onClick = onOpen,
                enabled = isActive,
                accent = accent,
                glow = isActive,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun BossFight.targetLabel(): String =
    when (targetMetric) {
        BossFightTargetMetric.WEIGHT -> "Ціль: ${targetValue.formatCompactTarget()} кг"
        BossFightTargetMetric.REPS -> "Ціль: ${targetValue.roundToInt()} повт."
        BossFightTargetMetric.TIME_SECONDS -> "Ціль: ${targetValue.roundToInt().formatTimeTarget()}"
        BossFightTargetMetric.DISTANCE_METERS -> "Ціль: ${targetValue.formatCompactTarget()} м"
    }

private fun Double.formatCompactTarget(): String =
    if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", this)
    }

private fun Int.formatTimeTarget(): String =
    if (this >= 60 && this % 60 == 0) {
        "${this / 60} хв"
    } else {
        "$this сек"
    }

@Composable
private fun StatusHeader(
    weekPreview: List<StatusWeekDayUiModel>,
    onOpenCalendar: () -> Unit
) {
    val colors = SystemTheme.colors
    val today = LocalDate.now()
    val dateText = today.format(DateTimeFormatter.ofPattern("dd.MM.yy"))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(124.dp)
                .height(88.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = dateText,
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = colors.textPrimary.copy(alpha = 0.96f),
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    fontSize = 25.sp,
                    lineHeight = 26.sp,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1,
                overflow = TextOverflow.Clip,
                modifier = Modifier.fillMaxWidth()
            )
        }
        WeekPreviewBlock(
            days = weekPreview,
            onOpenCalendar = onOpenCalendar,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SystemLevelBlock(data: StatusUiData) {
    val colors = SystemTheme.colors
    val progress = remember(data.xpTotal, data.xpMax) {
        if (data.xpMax > 0) (data.xpTotal.toFloat() / data.xpMax).coerceIn(0f, 1f) else 0f
    }
    val xpRemaining = (data.xpMax - data.xpTotal).coerceAtLeast(0)
    val isRecoveryDay = data.mainQuest == null
    val dayStatusText = remember(data.weekPreview, data.mainQuest) {
        if (data.mainQuest == null) {
            "Відновлення"
        } else {
            data.weekPreview
                .firstOrNull { it.isToday }
                ?.status
                ?.toCompactDayStatus()
                ?: if (data.mainQuest.isCompleted) "Виконано" else "Активний"
        }
    }
    val priorityText = data.mainQuest?.title ?: "Без тренування"

    DarkGlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Система · Рівень ${data.level}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SystemStateBadge(
                        text = dayStatusText,
                        accent = if (isRecoveryDay) colors.accentSuccess else colors.accentPrimary
                    )
                    Text(
                        text = "${(progress * 100).roundToInt()}%",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = colors.accentPrimary,
                            fontWeight = FontWeight.Black
                        )
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${data.xpTotal} / ${data.xpMax} XP",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = colors.textSecondary,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "До рівня ${data.level + 1}: $xpRemaining XP",
                    style = MaterialTheme.typography.bodySmall.copy(color = colors.textMuted),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            SystemProgressBar(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompactStatusItem(
                    icon = Icons.Filled.Whatshot,
                    text = "${data.currentStreak} днів",
                    accent = colors.accentWarning,
                    modifier = Modifier.weight(1f)
                )
                CompactStatusItem(
                    icon = Icons.Filled.FitnessCenter,
                    text = priorityText,
                    accent = colors.accentPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
internal fun SystemStateBadge(
    text: String,
    accent: Color
) {
    val colors = SystemTheme.colors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(SystemTheme.shapes.pill))
            .background(accent.copy(alpha = 0.09f))
            .border(1.dp, accent.copy(alpha = 0.18f), RoundedCornerShape(SystemTheme.shapes.pill))
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(RoundedCornerShape(SystemTheme.shapes.pill))
                .background(accent.copy(alpha = 0.88f))
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                color = colors.textPrimary.copy(alpha = 0.86f),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                lineHeight = 11.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CompactStatusItem(
    icon: ImageVector,
    text: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(SystemTheme.shapes.small))
                .background(accent.copy(alpha = 0.09f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(14.dp)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                color = colors.textSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                lineHeight = 12.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun StatusWeekDayStatus.toCompactDayStatus(): String =
    when (this) {
        StatusWeekDayStatus.COMPLETED -> "Виконано"
        StatusWeekDayStatus.PARTIAL -> "Частково"
        StatusWeekDayStatus.MISSED -> "Пропущено"
        StatusWeekDayStatus.PLANNED -> "Активний"
        StatusWeekDayStatus.NO_DATA -> "Немає даних"
    }

@Composable
private fun MainFocusBlock(
    mainQuest: QuestUiModel,
    onStartWorkout: () -> Unit,
    onOpenWorkoutSettings: () -> Unit
) {
    val colors = SystemTheme.colors
    DarkGlassCard(modifier = Modifier.fillMaxWidth(), active = true, contentPadding = SystemCardPadding) {
        Column(verticalArrangement = Arrangement.spacedBy(SystemCardPadding)) {
            SystemSectionHeader(
                title = "Головний фокус",
                subtitle = "Поточна дія дня",
                trailing = {
                    IconButton(onClick = onOpenWorkoutSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = null, tint = colors.textSecondary)
                    }
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SystemItemSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FocusIcon(
                    icon = Icons.Filled.FitnessCenter,
                    tint = colors.accentPrimary
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = mainQuest.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Black
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = mainQuest.subtitle.asString(),
                        style = MaterialTheme.typography.bodyMedium.copy(color = colors.textSecondary),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (mainQuest.tasks.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    mainQuest.tasks.take(3).forEach { task ->
                        FocusTaskLine(task)
                    }
                }
            }

            SystemButton(
                text = if (mainQuest.isCompleted) "Тренування завершено" else mainQuest.protocolActionText(),
                icon = Icons.Filled.FitnessCenter,
                onClick = onStartWorkout,
                enabled = !mainQuest.isCompleted,
                glow = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun QuestUiModel.protocolActionText(): String =
    when (title.uppercase()) {
        "NO EXCUSE PROTOCOL" -> "Почати 7 хв"
        "RECOVERY PROTOCOL" -> "Почати відновлення"
        "DELOAD SESSION" -> "Почати deload"
        else -> "Почати тренування"
    }

@Composable
private fun WeekPreviewBlock(
    days: List<StatusWeekDayUiModel>,
    onOpenCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    if (days.isEmpty()) {
        Box(modifier = modifier.height(58.dp), contentAlignment = Alignment.Center) {
            Text(
                text = "Календарний цикл синхронізується.",
                style = MaterialTheme.typography.bodySmall.copy(color = colors.textMuted)
            )
        }
    } else {
        SystemWeekCalendarPreview(
            days = days.map { it.toSystemWeekDayModel() },
            onOpenCalendar = onOpenCalendar,
            modifier = modifier
        )
    }
}

@Composable
internal fun FocusIcon(icon: ImageVector, tint: Color) {
    Box(
        modifier = Modifier
            .height(52.dp)
            .width(52.dp)
            .background(
                Brush.radialGradient(listOf(tint.copy(alpha = 0.22f), Color.Transparent))
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint)
    }
}

@Composable
private fun FocusTaskLine(task: TaskUiModel) {
    val colors = SystemTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .height(5.dp)
                .width(5.dp)
                .background(if (task.isCompleted) colors.accentSuccess else colors.accentPrimarySoft)
        )
        Text(
            text = task.nameUk ?: task.name,
            style = MaterialTheme.typography.bodySmall.copy(
                color = if (task.isCompleted) colors.textSecondary.copy(alpha = 0.58f) else colors.textSecondary,
                fontWeight = FontWeight.SemiBold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun StatusWeekDayUiModel.toSystemWeekDayModel(): SystemWeekDayModel =
    SystemWeekDayModel(
        date = date,
        label = weekDayLabel,
        dayNumber = dayNumber,
        visualType = when (visualType) {
            StatusWeekDayVisualType.WORK -> SystemWeekDayVisualType.Work
            StatusWeekDayVisualType.TRAINING -> SystemWeekDayVisualType.Training
            StatusWeekDayVisualType.MIXED -> SystemWeekDayVisualType.Mixed
            StatusWeekDayVisualType.REST -> SystemWeekDayVisualType.Rest
        },
        status = when (status) {
            StatusWeekDayStatus.COMPLETED -> SystemWeekDayStatus.Completed
            StatusWeekDayStatus.PARTIAL -> SystemWeekDayStatus.Partial
            StatusWeekDayStatus.MISSED -> SystemWeekDayStatus.Missed
            StatusWeekDayStatus.PLANNED -> SystemWeekDayStatus.Planned
            StatusWeekDayStatus.NO_DATA -> SystemWeekDayStatus.NoData
        },
        isToday = isToday
    )
