package com.ihor.thesystem.feature.status.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.components.DarkGlassCard
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemSectionHeader
import com.ihor.thesystem.core.ui.components.SystemTodoItem
import com.ihor.thesystem.feature.status.viewmodel.TodoUiModel
import kotlinx.coroutines.withTimeoutOrNull

@Composable
internal fun TodoBlock(
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
