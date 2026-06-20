package com.ihor.thesystem.feature.status.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ihor.thesystem.core.ui.components.SystemCutCornerShape
import com.ihor.thesystem.core.ui.components.TechSurfaceRole
import com.ihor.thesystem.core.ui.components.systemLargePanelShape
import com.ihor.thesystem.core.ui.components.techSurface
import com.ihor.thesystem.core.theme.SystemTheme
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
    val cardShape = systemLargePanelShape()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .techSurface(
                shape = cardShape,
                active = false,
                accent = colors.accentPrimary,
                role = TechSurfaceRole.Panel
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 22.dp, top = 20.dp, end = 22.dp, bottom = 22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TO-DO",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = colors.accentPrimary,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        lineHeight = 18.sp,
                        letterSpacing = 3.0.sp
                    ),
                    maxLines = 1
                )
                TodoAddButton(onClick = { onAddTask(0) })
            }
            Spacer(modifier = Modifier.height(14.dp))
            if (todos.isNotEmpty()) {
                ReorderableTodoList(
                    todos = todos,
                    onTaskToggled = onTaskToggled,
                    onTodosReordered = onTodosReordered,
                    onRemoveTask = onRemoveTask
                )
                Spacer(modifier = Modifier.height(13.dp))
            }
            TodoAddTaskRow(onClick = { onAddTask(todos.size) })
        }
    }
}

@Composable
private fun ReorderableTodoList(
    todos: List<TodoUiModel>,
    onTaskToggled: (TodoUiModel) -> Unit,
    onTodosReordered: (List<Int>) -> Unit,
    onRemoveTask: (Int) -> Unit
) {
    var visibleTodos by remember(todos) { mutableStateOf(todos) }
    var draggingTodoId by remember { mutableStateOf<Int?>(null) }
    var draggedCenterY by remember { mutableStateOf<Float?>(null) }
    val itemBounds = remember { mutableStateMapOf<Int, Rect>() }

    Column {
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
                TodoListItem(
                    task = task,
                    isLast = index == visibleTodos.lastIndex,
                    onTaskToggled = onTaskToggled,
                    onRemoveTask = onRemoveTask,
                    modifier = Modifier
                        .zIndex(if (isDragging) 1f else 0f)
                        .graphicsLayer {
                            translationY = dragTranslationY
                            shadowElevation = if (isDragging) 14f else 0f
                            scaleX = if (isDragging) 1.01f else 1f
                            scaleY = if (isDragging) 1.01f else 1f
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
                        )
                )
            }
        }
    }
}

@Composable
private fun TodoListItem(
    task: TodoUiModel,
    isLast: Boolean,
    onTaskToggled: (TodoUiModel) -> Unit,
    onRemoveTask: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TodoCheckBox(
                checked = task.isCompleted,
                onClick = { onTaskToggled(task) }
            )
            Text(
                text = task.title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 17.sp,
                    lineHeight = 21.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            TodoDeleteButton(onClick = { onRemoveTask(task.id) })
            TodoDragHandle(modifier = Modifier.size(width = 29.dp, height = 24.dp))
        }
        if (!isLast) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 43.dp, end = 42.dp)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.overlayStrong.copy(alpha = 0.32f))
            )
        }
    }
}

@Composable
private fun TodoAddButton(onClick: () -> Unit) {
    val colors = SystemTheme.colors
    Box(
        modifier = Modifier
            .size(40.dp)
            .techSurface(
                shape = CircleShape,
                active = true,
                accent = colors.accentPrimary,
                role = TechSurfaceRole.Button
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(17.dp)) {
            drawLine(
                color = colors.accentPrimary,
                start = Offset(size.width * 0.50f, size.height * 0.14f),
                end = Offset(size.width * 0.50f, size.height * 0.86f),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Square
            )
            drawLine(
                color = colors.accentPrimary,
                start = Offset(size.width * 0.14f, size.height * 0.50f),
                end = Offset(size.width * 0.86f, size.height * 0.50f),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Square
            )
        }
    }
}

@Composable
private fun TodoAddTaskRow(onClick: () -> Unit) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(shape)
            .drawBehind {
                val stroke = Stroke(
                    width = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 8.dp.toPx()))
                )
                drawRoundRect(
                    color = colors.accentPrimary.copy(alpha = 0.28f),
                    topLeft = Offset(0.5.dp.toPx(), 0.5.dp.toPx()),
                    size = Size(size.width - 1.dp.toPx(), size.height - 1.dp.toPx()),
                    cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                    style = stroke
                )
            }
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(20.dp)) {
            drawLine(
                color = colors.accentPrimary,
                start = Offset(size.width * 0.50f, size.height * 0.08f),
                end = Offset(size.width * 0.50f, size.height * 0.92f),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Square
            )
            drawLine(
                color = colors.accentPrimary,
                start = Offset(size.width * 0.08f, size.height * 0.50f),
                end = Offset(size.width * 0.92f, size.height * 0.50f),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Square
            )
        }
        Text(
            text = "Додати завдання",
            style = MaterialTheme.typography.titleMedium.copy(
                color = colors.accentPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                lineHeight = 22.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TodoDeleteButton(onClick: () -> Unit) {
    val colors = SystemTheme.colors
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(SystemCutCornerShape(6.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(16.dp)) {
            val deleteColor = colors.accentError.copy(alpha = 0.72f)
            drawLine(
                color = deleteColor,
                start = Offset(size.width * 0.20f, size.height * 0.20f),
                end = Offset(size.width * 0.80f, size.height * 0.80f),
                strokeWidth = 1.9.dp.toPx(),
                cap = StrokeCap.Square
            )
            drawLine(
                color = deleteColor,
                start = Offset(size.width * 0.80f, size.height * 0.20f),
                end = Offset(size.width * 0.20f, size.height * 0.80f),
                strokeWidth = 1.9.dp.toPx(),
                cap = StrokeCap.Square
            )
        }
    }
}

@Composable
private fun TodoCheckBox(
    checked: Boolean,
    onClick: () -> Unit
) {
    val colors = SystemTheme.colors
    val shape = SystemCutCornerShape(3.dp)
    Box(
        modifier = Modifier
            .size(27.dp)
            .clip(shape)
            .background(if (checked) colors.accentPrimary.copy(alpha = 0.16f) else Color.Transparent)
            .border(
                width = 1.5.dp,
                color = if (checked) colors.accentPrimary else colors.textSecondary.copy(alpha = 0.72f),
                shape = shape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawLine(
                    color = colors.accentPrimary,
                    start = Offset(size.width * 0.22f, size.height * 0.53f),
                    end = Offset(size.width * 0.42f, size.height * 0.73f),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = colors.accentPrimary,
                    start = Offset(size.width * 0.42f, size.height * 0.73f),
                    end = Offset(size.width * 0.80f, size.height * 0.27f),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun TodoDragHandle(modifier: Modifier = Modifier) {
    val colors = SystemTheme.colors
    Canvas(modifier = modifier) {
        val startX = size.width * 0.18f
        val endX = size.width * 0.86f
        repeat(3) { index ->
            val y = size.height * (0.30f + index * 0.22f)
            drawLine(
                color = colors.textSecondary.copy(alpha = 0.70f),
                start = Offset(startX, y),
                end = Offset(endX, y),
                strokeWidth = 1.8.dp.toPx(),
                cap = StrokeCap.Square
            )
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
