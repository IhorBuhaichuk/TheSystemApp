package com.ihor.thesystem.feature.status.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
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
import com.ihor.thesystem.feature.status.viewmodel.QuestUiModel
import com.ihor.thesystem.feature.status.viewmodel.StatusUiData
import com.ihor.thesystem.feature.status.viewmodel.StatusWeekDayStatus
import com.ihor.thesystem.feature.status.viewmodel.StatusWeekDayUiModel
import com.ihor.thesystem.feature.status.viewmodel.StatusWeekDayVisualType
import com.ihor.thesystem.feature.status.viewmodel.TaskUiModel
import com.ihor.thesystem.feature.status.viewmodel.TodoUiModel
import kotlinx.coroutines.withTimeoutOrNull
import java.time.LocalDate
import java.time.format.TextStyle
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
        StatusHeader(
            weekPreview = data.weekPreview,
            isRecoveryDay = data.mainQuest == null,
            onOpenCalendar = onOpenCalendar
        )
        XpLevelBlock(data = data)
        data.mainQuest?.let { mainQuest ->
            MainFocusBlock(
                mainQuest = mainQuest,
                onStartWorkout = onStartWorkout,
                onOpenWorkoutSettings = onOpenWorkoutSettings
            )
        }
        TodoBlock(
            todos = data.todos,
            onTaskToggled = onTaskToggled,
            onAddTask = onAddTask,
            onAddMicrotask = onAddMicrotask,
            onTodosReordered = onTodosReordered,
            onRemoveTask = onRemoveTask
        )
        SystemInfoBlock(data = data)
    }
}

@Composable
private fun StatusHeader(
    weekPreview: List<StatusWeekDayUiModel>,
    isRecoveryDay: Boolean,
    onOpenCalendar: () -> Unit
) {
    val colors = SystemTheme.colors
    val locale = Locale.getDefault()
    val today = LocalDate.now()
    val weekDay = today.dayOfWeek.getDisplayName(TextStyle.FULL_STANDALONE, locale)
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
    val month = today.month.getDisplayName(TextStyle.FULL_STANDALONE, locale)
    val dateText = "$weekDay, ${today.dayOfMonth} $month"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Статус",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Black
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (isRecoveryDay) {
                Text(
                    text = "Активне відновлення",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = colors.accentSuccess,
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.sp,
                        lineHeight = 9.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = dateText,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
private fun XpLevelBlock(data: StatusUiData) {
    val colors = SystemTheme.colors
    val progress = remember(data.xpTotal, data.xpMax) {
        if (data.xpMax > 0) (data.xpTotal.toFloat() / data.xpMax).coerceIn(0f, 1f) else 0f
    }
    val xpRemaining = (data.xpMax - data.xpTotal).coerceAtLeast(0)

    DarkGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = "Рівень ${data.level}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Black
                        )
                    )
                    Text(
                        text = "${data.xpTotal} / ${data.xpMax} XP",
                        style = MaterialTheme.typography.bodyMedium.copy(color = colors.textSecondary)
                    )
                }
                Text(
                    text = "${(progress * 100).roundToInt()}%",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = colors.accentPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            SystemProgressBar(progress = progress)
            Text(
                text = "До наступного рівня: $xpRemaining XP",
                style = MaterialTheme.typography.bodySmall.copy(color = colors.textMuted)
            )
        }
    }
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

    DarkGlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SystemSectionHeader(
                title = "To-do",
                subtitle = if (allTasks.isNotEmpty()) "$completed/${allTasks.size} виконано" else "Список на сьогодні порожній",
                trailing = {
                    SystemButton(
                        text = "Додати",
                        icon = Icons.Filled.Add,
                        onClick = { onAddTask(0) },
                        modifier = Modifier.height(38.dp)
                    )
                }
            )

            if (todos.isEmpty()) {
                Text(
                    text = "Додай будь-яке завдання, яке має бути зроблене сьогодні.",
                    style = MaterialTheme.typography.bodySmall.copy(color = colors.textMuted)
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
private fun SystemInfoBlock(data: StatusUiData) {
    val colors = SystemTheme.colors
    val monthProgress = remember(data.monthWorkoutsCompleted, data.monthWorkoutsTotal) {
        if (data.monthWorkoutsTotal > 0) {
            data.monthWorkoutsCompleted.toFloat() / data.monthWorkoutsTotal
        } else {
            0f
        }.coerceIn(0f, 1f)
    }

    DarkGlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = SystemCardPadding) {
        Column(verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)) {
            SystemSectionHeader(title = "Система", subtitle = "Короткий стан")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                SystemInfoMetric(
                    label = "Серія",
                    value = data.currentStreak.toString(),
                    subtitle = "днів без пропуску",
                    accent = colors.accentWarning,
                    modifier = Modifier.weight(1f)
                )
                SystemInfoMetric(
                    label = "Місяць",
                    value = "${data.monthWorkoutsCompleted}/${data.monthWorkoutsTotal.coerceAtLeast(0)}",
                    subtitle = "тренувальна ціль",
                    accent = colors.accentPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
            SystemProgressBar(progress = monthProgress, accent = colors.accentSuccess)
        }
    }
}

@Composable
private fun SystemInfoMetric(
    label: String,
    value: String,
    subtitle: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(SystemTheme.shapes.medium))
            .background(colors.overlayLight)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
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
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall.copy(color = accent.copy(alpha = 0.82f)),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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
