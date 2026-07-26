package com.ihor.thesystem.feature.status.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ihor.thesystem.core.ui.components.TechSurfaceRole
import com.ihor.thesystem.core.ui.components.systemClickable
import com.ihor.thesystem.core.ui.components.systemEnterMotion
import com.ihor.thesystem.core.ui.components.systemToggleable
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
    val motion = SystemTheme.motion
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = motion.spatialDampingRatio,
                    stiffness = motion.spatialStiffness
                )
            )
            .systemEnterMotion(
                initialScale = 0.995f,
                initialOffset = 10.dp
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Завдання",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 25.sp,
                    lineHeight = 29.sp,
                    letterSpacing = (-0.2).sp
                ),
                maxLines = 1
            )
            TodoAddButton(onClick = { onAddTask(0) })
        }
        Spacer(modifier = Modifier.height(12.dp))

        AnimatedContent(
            targetState = todos,
            transitionSpec = {
                (
                    fadeIn() +
                        expandVertically(
                            expandFrom = Alignment.Top,
                            animationSpec = spring(
                                dampingRatio = motion.spatialDampingRatio,
                                stiffness = motion.spatialStiffness
                            )
                        )
                    ).togetherWith(
                    fadeOut() +
                        shrinkVertically(
                            shrinkTowards = Alignment.Top,
                            animationSpec = spring(
                                dampingRatio = motion.spatialDampingRatio,
                                stiffness = motion.spatialStiffness
                            )
                        )
                )
            },
            contentKey = { list -> list.map(TodoUiModel::id) },
            label = "todo_list_content"
        ) { visibleTodos ->
            Column {
                if (visibleTodos.isNotEmpty()) {
                    ReorderableTodoList(
                        todos = visibleTodos,
                        onTaskToggled = onTaskToggled,
                        onTodosReordered = onTodosReordered,
                        onRemoveTask = onRemoveTask
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
                TodoAddTaskRow(onClick = { onAddTask(visibleTodos.size) })
            }
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

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        visibleTodos.forEach { task ->
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
    onTaskToggled: (TodoUiModel) -> Unit,
    onRemoveTask: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    val titleColor by animateColorAsState(
        targetValue = if (task.isCompleted) {
            colors.textMuted
        } else {
            colors.textSecondary
        },
        label = "todo_title_color"
    )
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp)
            .techSurface(
                shape = shape,
                active = false,
                accent = colors.accentPrimary,
                role = TechSurfaceRole.Plate
            )
            .padding(start = 3.dp, end = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
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
                color = titleColor,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        TodoDeleteButton(onClick = { onRemoveTask(task.id) })
        TodoDragHandle(modifier = Modifier.size(width = 24.dp, height = 24.dp))
    }
}

@Composable
private fun TodoAddButton(onClick: () -> Unit) {
    val colors = SystemTheme.colors
    Box(
        modifier = Modifier
            .size(48.dp)
            .techSurface(
                shape = CircleShape,
                active = false,
                accent = colors.accentPrimary,
                role = TechSurfaceRole.Button
            )
            .systemClickable(onClick = onClick)
            .semantics { contentDescription = "Додати завдання" },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(17.dp)) {
            drawLine(
                color = colors.accentPrimary,
                start = Offset(size.width * 0.50f, size.height * 0.14f),
                end = Offset(size.width * 0.50f, size.height * 0.86f),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = colors.accentPrimary,
                start = Offset(size.width * 0.14f, size.height * 0.50f),
                end = Offset(size.width * 0.86f, size.height * 0.50f),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun TodoAddTaskRow(onClick: () -> Unit) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .techSurface(
                shape = shape,
                active = false,
                accent = colors.accentPrimary,
                role = TechSurfaceRole.Plate
            )
            .systemClickable(onClick = onClick)
            .padding(horizontal = 15.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(colors.overlayLight)
                .border(1.dp, colors.borderSubtle, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(18.dp)) {
                drawLine(
                    color = colors.accentPrimary,
                    start = Offset(size.width * 0.50f, size.height * 0.12f),
                    end = Offset(size.width * 0.50f, size.height * 0.88f),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = colors.accentPrimary,
                    start = Offset(size.width * 0.12f, size.height * 0.50f),
                    end = Offset(size.width * 0.88f, size.height * 0.50f),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
        Text(
            text = "Додати завдання",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium.copy(
                color = colors.textPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 20.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun TodoDeleteButton(onClick: () -> Unit) {
    val colors = SystemTheme.colors
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .systemClickable(onClick = onClick)
            .semantics { contentDescription = "Видалити завдання" },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(16.dp)) {
            val deleteColor = colors.accentError.copy(alpha = 0.72f)
            drawLine(
                color = deleteColor,
                start = Offset(size.width * 0.20f, size.height * 0.20f),
                end = Offset(size.width * 0.80f, size.height * 0.80f),
                strokeWidth = 1.9.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = deleteColor,
                start = Offset(size.width * 0.80f, size.height * 0.20f),
                end = Offset(size.width * 0.20f, size.height * 0.80f),
                strokeWidth = 1.9.dp.toPx(),
                cap = StrokeCap.Round
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
    val motion = SystemTheme.motion
    val shape = RoundedCornerShape(8.dp)
    val fillColor by animateColorAsState(
        targetValue = if (checked) {
            colors.accentPrimary.copy(alpha = 0.18f)
        } else {
            colors.background.copy(alpha = 0.28f)
        },
        label = "todo_checkbox_fill"
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked) colors.accentPrimary else colors.textSecondary.copy(alpha = 0.56f),
        label = "todo_checkbox_border"
    )
    val checkProgress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(
            dampingRatio = motion.spatialDampingRatio,
            stiffness = motion.spatialStiffness
        ),
        label = "todo_checkbox_check"
    )
    Box(
        modifier = Modifier
            .size(44.dp)
            .systemToggleable(
                value = checked,
                onValueChange = { onClick() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(shape)
                .background(fillColor)
                .border(
                    width = 1.5.dp,
                    color = borderColor,
                    shape = shape
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = checkProgress
                        val scale = 0.68f + (0.32f * checkProgress)
                        scaleX = scale
                        scaleY = scale
                    }
            ) {
                drawLine(
                    color = colors.accentPrimary,
                    start = Offset(size.width * 0.22f, size.height * 0.53f),
                    end = Offset(size.width * 0.42f, size.height * 0.73f),
                    strokeWidth = 2.7.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = colors.accentPrimary,
                    start = Offset(size.width * 0.42f, size.height * 0.73f),
                    end = Offset(size.width * 0.80f, size.height * 0.27f),
                    strokeWidth = 2.7.dp.toPx(),
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
                cap = StrokeCap.Round
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
