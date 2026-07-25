package com.ihor.thesystem.feature.statistics.ui.components.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.ihor.thesystem.core.theme.SystemControlHeight
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.components.sciPanel
import com.ihor.thesystem.core.ui.components.systemOutlinedTextFieldColors
import com.ihor.thesystem.feature.status.viewmodel.QuestUiModel
import com.ihor.thesystem.feature.status.viewmodel.TaskUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestChecklistSheet(
    quest: QuestUiModel,
    accentColor: Color,
    onTaskToggle: (TaskUiModel) -> Unit,
    onAddTask: (String) -> Unit,
    onRemoveTask: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = SystemTheme.colors
    var newTaskName by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    
    // Створюємо локальну копію списку для стабільності під час анімацій закриття
    val tasks = remember(quest.tasks) { quest.tasks }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = colors.surfaceGlassStrong,
        dragHandle       = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .sciPanel(
                        borderColor     = accentColor.copy(alpha = 0.4f),
                        backgroundColor = accentColor.copy(alpha = 0.15f),
                        cornerCut       = 2.dp
                    )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = quest.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Daily Operations Protocol",
                    style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary)
                )
            }

            HorizontalDivider(color = accentColor.copy(alpha = 0.25f), thickness = 1.dp)

            // Add Task Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SystemItemSpacing)
            ) {
                OutlinedTextField(
                    value = newTaskName,
                    onValueChange = { newTaskName = it },
                    placeholder = { Text("Нова справа...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(SystemTheme.shapes.medium),
                    colors = systemOutlinedTextFieldColors(accent = accentColor),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = colors.textPrimary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (newTaskName.isNotBlank()) {
                            onAddTask(newTaskName)
                            newTaskName = ""
                        }
                        focusManager.clearFocus()
                    }),
                    singleLine = true,
                    maxLines = 1
                )
                IconButton(
                    onClick = {
                        if (newTaskName.isNotBlank()) {
                            onAddTask(newTaskName)
                            newTaskName = ""
                        }
                        focusManager.clearFocus()
                    },
                    modifier = Modifier.size(SystemControlHeight)
                ) {
                    Icon(Icons.Filled.Add, "Add", tint = accentColor)
                }
            }

            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                    text = "[ Список порожній ]",
                        style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary)
                    )
                }
            } else {
                // Використовуємо Box з обмеженням висоти для LazyColumn, щоб уникнути конфліктів ModalBottomSheet
                Box(modifier = Modifier.heightIn(max = 400.dp)) {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(tasks, key = { it.id }) { task ->
                            TaskRow(
                                task        = task,
                                accentColor = accentColor,
                                onToggle    = { onTaskToggle(task) },
                                onRemove    = { onRemoveTask(task.id) }
                            )
                        }
                    }
                }
            }

            if (quest.isCompleted && tasks.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .sciPanel(
                            borderColor     = colors.accentSuccess,
                            backgroundColor = colors.accentSuccess.copy(alpha = 0.1f),
                            cornerCut       = 8.dp
                        )
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                    text = "✓ Всі завдання виконано",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = colors.accentSuccess,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskRow(
    task: TaskUiModel,
    accentColor: Color,
    onToggle: () -> Unit,
    onRemove: () -> Unit
) {
    val colors = SystemTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .sciPanel(
                borderColor     = if (task.isCompleted) colors.accentSuccess.copy(alpha = 0.4f) else accentColor.copy(alpha = 0.25f),
                backgroundColor = if (task.isCompleted) colors.accentSuccess.copy(alpha = 0.07f) else colors.surfaceGlassStrong,
                cornerCut       = 6.dp
            )
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Квадратик з галочкою
        IconButton(onClick = onToggle, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = if (task.isCompleted) Icons.Filled.CheckBox
                else Icons.Filled.CheckBoxOutlineBlank,
                contentDescription = null,
                tint     = if (task.isCompleted) colors.accentSuccess else accentColor.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
        }
        
        Text(
            text = task.name,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = if (task.isCompleted) colors.textSecondary else colors.textPrimary,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
            ),
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Delete",
                tint = colors.accentError.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
