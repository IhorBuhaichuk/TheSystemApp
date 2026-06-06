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
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.key
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ihor.thesystem.core.theme.SystemCardPadding
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemScreenPadding
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.components.DarkGlassCard
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemProgressBar
import com.ihor.thesystem.core.ui.components.SystemSectionHeader
import com.ihor.thesystem.core.ui.components.SystemTodoItem
import com.ihor.thesystem.core.ui.components.SystemWeekCalendarPreview
import com.ihor.thesystem.core.ui.components.SystemWeekDayModel
import com.ihor.thesystem.core.ui.components.SystemWeekDayStatus
import com.ihor.thesystem.core.ui.components.SystemWeekDayVisualType
import com.ihor.thesystem.domain.model.TodayTrainingDecision
import com.ihor.thesystem.domain.model.TodayTrainingDecisionType
import com.ihor.thesystem.feature.status.viewmodel.QuestUiModel
import com.ihor.thesystem.feature.status.viewmodel.StatusUiData
import com.ihor.thesystem.feature.status.viewmodel.StatusWeekDayStatus
import com.ihor.thesystem.feature.status.viewmodel.StatusWeekDayUiModel
import com.ihor.thesystem.feature.status.viewmodel.StatusWeekDayVisualType
import com.ihor.thesystem.feature.status.viewmodel.TaskUiModel
import com.ihor.thesystem.feature.status.viewmodel.TodoUiModel
import kotlinx.coroutines.withTimeoutOrNull
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
private fun TodayOrderBlock(
    decision: TodayTrainingDecision?,
    fallbackMainQuest: QuestUiModel?,
    onStartWorkout: () -> Unit
) {
    val colors = SystemTheme.colors
    val decisionType = decision?.decisionType
    val accent = decision.orderAccent()
    val readinessProgress = ((decision?.readinessScore ?: 0) / 100f).coerceIn(0f, 1f)
    val actionEnabled = decisionType != null && decisionType != TodayTrainingDecisionType.REST
    val actionText = when (decisionType) {
        TodayTrainingDecisionType.ACTIVE_RECOVERY -> "Почати відновлення"
        TodayTrainingDecisionType.REST -> "День без тренування"
        null -> "План формується"
        else -> "Почати тренування"
    }

    DarkGlassCard(modifier = Modifier.fillMaxWidth(), active = true, contentPadding = SystemCardPadding) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "Сьогоднішній наказ",
                subtitle = "Поточна дія дня"
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
                        text = "Сьогодні: ${decision.todayTitle(fallbackMainQuest)}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Black
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Рішення системи: ${decision.shortDecisionLabel()}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = accent,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SystemStateBadge(
                        text = "Готовність: ${decision?.readinessScore ?: 0}%",
                        accent = accent
                    )
                    SystemStateBadge(
                        text = decision.shortDecisionLabel(),
                        accent = accent
                    )
                }
                SystemProgressBar(
                    progress = readinessProgress,
                    accent = accent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                )
            }

            Text(
                text = decision.displayReason(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            SystemButton(
                text = actionText,
                icon = Icons.Filled.FitnessCenter,
                onClick = onStartWorkout,
                enabled = actionEnabled,
                accent = accent,
                glow = actionEnabled,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TodayTrainingDecision?.orderAccent(): Color {
    val colors = SystemTheme.colors
    return when (this?.decisionType) {
        TodayTrainingDecisionType.PROGRESS_ALLOWED -> colors.accentSuccess
        TodayTrainingDecisionType.STANDARD_TRAINING,
        TodayTrainingDecisionType.NO_EXCUSE -> colors.accentPrimary
        TodayTrainingDecisionType.REDUCED_LOAD -> colors.accentWarning
        TodayTrainingDecisionType.ACTIVE_RECOVERY,
        TodayTrainingDecisionType.DELOAD -> colors.accentAi
        TodayTrainingDecisionType.REST -> colors.textMuted
        null -> colors.textMuted
    }
}

private fun TodayTrainingDecision?.todayTitle(fallbackMainQuest: QuestUiModel?): String =
    when (this?.decisionType) {
        TodayTrainingDecisionType.ACTIVE_RECOVERY -> "Recovery Protocol"
        TodayTrainingDecisionType.REST -> "День без тренування"
        null -> fallbackMainQuest?.title ?: "Recovery Protocol"
        else -> workoutName ?: fallbackMainQuest?.title ?: "Recovery Protocol"
    }

private fun TodayTrainingDecision?.shortDecisionLabel(): String =
    when (this?.decisionType) {
        TodayTrainingDecisionType.PROGRESS_ALLOWED -> "Прогрес дозволено"
        TodayTrainingDecisionType.STANDARD_TRAINING -> "Планове тренування"
        TodayTrainingDecisionType.REDUCED_LOAD -> "Зменшити навантаження"
        TodayTrainingDecisionType.ACTIVE_RECOVERY -> "Відновлення"
        TodayTrainingDecisionType.NO_EXCUSE -> "План перераховано"
        TodayTrainingDecisionType.DELOAD -> "Делоад"
        TodayTrainingDecisionType.REST -> "Відпочинок"
        null -> "План синхронізується"
    }

private fun TodayTrainingDecision?.displayReason(): String =
    when (this?.decisionType) {
        TodayTrainingDecisionType.PROGRESS_ALLOWED ->
            "Готовність висока, борг відновлення низький."
        TodayTrainingDecisionType.STANDARD_TRAINING ->
            "План на сьогодні підходить під поточний стан."
        TodayTrainingDecisionType.REDUCED_LOAD ->
            "Сьогодні краще знизити вагу та обсяг."
        TodayTrainingDecisionType.ACTIVE_RECOVERY ->
            "Організму потрібне відновлення замість силового навантаження."
        TodayTrainingDecisionType.NO_EXCUSE ->
            "План перераховано. Наступна оптимальна дія: ${workoutName ?: "тренування за планом"}."
        TodayTrainingDecisionType.DELOAD ->
            "Навантаження накопичилось, сьогодні працюємо легше."
        TodayTrainingDecisionType.REST ->
            "Сьогодні день без тренування."
        null ->
            "Система готує сьогоднішній план."
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
private fun SystemStateBadge(
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
                text = if (mainQuest.isCompleted) "Тренування завершено" else "Почати тренування",
                icon = Icons.Filled.FitnessCenter,
                onClick = onStartWorkout,
                enabled = !mainQuest.isCompleted,
                glow = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
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
private fun TodoBlock(
    todos: List<TodoUiModel>,
    onTaskToggled: (TodoUiModel) -> Unit,
    onAddTask: (Int) -> Unit,
    onAddMicrotask: (TodoUiModel) -> Unit,
    onTodosReordered: (List<Int>) -> Unit,
    onRemoveTask: (Int) -> Unit
) {
    val colors = SystemTheme.colors
    val allTasks = remember(todos) { todos.flatMapWithMicrotasks() }
    val completed = allTasks.count { it.isCompleted }

    DarkGlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SystemSectionHeader(
                title = "To-do",
                subtitle = if (allTasks.isNotEmpty()) "$completed/${allTasks.size} виконано" else "Список порожній",
                trailing = {
                    SystemButton(
                        text = "Додати",
                        icon = Icons.Filled.Add,
                        onClick = { onAddTask(0) },
                        modifier = Modifier.height(34.dp)
                    )
                }
            )

            if (todos.isEmpty()) {
                Text(
                    text = "День чистий. Додай тільки те, що справді треба закрити.",
                    style = MaterialTheme.typography.bodySmall.copy(color = colors.textMuted.copy(alpha = 0.86f))
                )
            } else {
                ReorderableTodoList(
                    todos = todos,
                    onTaskToggled = onTaskToggled,
                    onAddMicrotask = onAddMicrotask,
                    onTodosReordered = onTodosReordered,
                    onRemoveTask = onRemoveTask
                )
            }
        }
    }
}

@Composable
private fun ReorderableTodoList(
    todos: List<TodoUiModel>,
    onTaskToggled: (TodoUiModel) -> Unit,
    onAddMicrotask: (TodoUiModel) -> Unit,
    onTodosReordered: (List<Int>) -> Unit,
    onRemoveTask: (Int) -> Unit
) {
    var visibleTodos by remember(todos) { mutableStateOf(todos) }
    var draggingTodoId by remember { mutableStateOf<Int?>(null) }
    var draggedCenterY by remember { mutableStateOf<Float?>(null) }
    val itemBounds = remember { mutableStateMapOf<Int, Rect>() }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        visibleTodos.forEachIndexed { index, task ->
            key(task.id) {
                val isDragging = draggingTodoId == task.id
                val dragTranslationY = if (isDragging) {
                    val baseCenterY = itemBounds[task.id]?.center?.y
                    val currentDraggedCenterY = draggedCenterY
                    if (baseCenterY != null && currentDraggedCenterY != null) {
                        currentDraggedCenterY - baseCenterY
                    } else {
                        0f
                    }
                } else {
                    0f
                }
                TodoTreeItem(
                    task = task,
                    number = "${index + 1}.",
                    onTaskToggled = onTaskToggled,
                    onAddMicrotask = onAddMicrotask,
                    onRemoveTask = onRemoveTask,
                    modifier = Modifier
                        .zIndex(if (isDragging) 1f else 0f)
                        .graphicsLayer {
                            translationY = dragTranslationY
                            shadowElevation = if (isDragging) 18f else 0f
                            scaleX = if (isDragging) 1.015f else 1f
                            scaleY = if (isDragging) 1.015f else 1f
                        }
                        .onGloballyPositioned { coordinates ->
                            itemBounds[task.id] = coordinates.boundsInParent()
                        }
                        .dragAfterOneSecond(
                            enabled = visibleTodos.size > 1,
                            onDragStart = {
                                draggingTodoId = task.id
                                draggedCenterY = itemBounds[task.id]?.center?.y
                            },
                            onDrag = { offset ->
                                if (draggingTodoId == task.id) {
                                    val nextDraggedCenterY = (draggedCenterY ?: itemBounds[task.id]?.center?.y)
                                        ?.plus(offset.y)
                                    draggedCenterY = nextDraggedCenterY
                                    val reordered = visibleTodos.reorderedByDrop(
                                        draggedTodoId = task.id,
                                        draggedCenterY = nextDraggedCenterY,
                                        itemBounds = itemBounds
                                    )
                                    if (reordered.map { it.id } != visibleTodos.map { it.id }) {
                                        visibleTodos = reordered
                                    }
                                }
                            },
                            onDragEnd = {
                                val reordered = visibleTodos.reorderedByDrop(
                                    draggedTodoId = task.id,
                                    draggedCenterY = draggedCenterY,
                                    itemBounds = itemBounds
                                )
                                visibleTodos = reordered
                                draggingTodoId = null
                                draggedCenterY = null
                                val orderedIds = reordered.map { it.id }
                                if (orderedIds != todos.map { it.id }) {
                                    onTodosReordered(orderedIds)
                                }
                            }
                        ),
                    isDragging = isDragging
                )
            }
        }
    }
}

@Composable
private fun TodoTreeItem(
    task: TodoUiModel,
    number: String,
    onTaskToggled: (TodoUiModel) -> Unit,
    onAddMicrotask: (TodoUiModel) -> Unit,
    onRemoveTask: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isDragging: Boolean = false
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SystemTodoItem(
            title = task.title,
            numberLabel = number,
            isCompleted = task.isCompleted,
            onToggle = { onTaskToggled(task) },
            onAddMicrotask = { onAddMicrotask(task) },
            onRemove = { onRemoveTask(task.id) },
            isDragging = isDragging
        )
        if (task.microtasks.isNotEmpty()) {
            Column(
                modifier = Modifier.padding(start = 18.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                task.microtasks.forEachIndexed { index, microtask ->
                    SystemTodoItem(
                        title = microtask.title,
                        numberLabel = "$number${index + 1}",
                        isCompleted = microtask.isCompleted,
                        onToggle = { onTaskToggled(microtask) },
                        onRemove = { onRemoveTask(microtask.id) },
                        compact = true
                    )
                }
            }
        }
    }
}

private fun List<TodoUiModel>.reorderedByDrop(
    draggedTodoId: Int,
    draggedCenterY: Float?,
    itemBounds: Map<Int, Rect>
): List<TodoUiModel> {
    if (draggedCenterY == null) return this
    val dragged = firstOrNull { it.id == draggedTodoId } ?: return this
    val remaining = filterNot { it.id == draggedTodoId }
    val insertionIndex = remaining.indexOfFirst { item ->
        val itemCenterY = itemBounds[item.id]?.center?.y ?: return@indexOfFirst false
        itemCenterY > draggedCenterY
    }.let { index ->
        if (index == -1) remaining.size else index
    }
    return remaining.toMutableList().apply {
        add(insertionIndex.coerceIn(0, size), dragged)
    }
}

private fun List<TodoUiModel>.flatMapWithMicrotasks(): List<TodoUiModel> =
    flatMap { task -> listOf(task) + task.microtasks }

private fun Modifier.dragAfterOneSecond(
    enabled: Boolean,
    onDragStart: () -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit
): Modifier =
    if (!enabled) {
        this
    } else {
        pointerInput(enabled) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                val activated = withTimeoutOrNull(1_000L) {
                    var isPressed = true
                    while (isPressed) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id }
                            ?: return@withTimeoutOrNull false
                        isPressed = change.pressed
                    }
                    false
                } ?: true

                if (!activated) return@awaitEachGesture

                onDragStart()
                try {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        val offset = change.positionChange()
                        change.consume()

                        if (offset != Offset.Zero) {
                            onDrag(offset)
                        }
                        if (!change.pressed) break
                    }
                } finally {
                    onDragEnd()
                }
            }
        }
    }

@Composable
private fun FocusIcon(icon: ImageVector, tint: Color) {
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
