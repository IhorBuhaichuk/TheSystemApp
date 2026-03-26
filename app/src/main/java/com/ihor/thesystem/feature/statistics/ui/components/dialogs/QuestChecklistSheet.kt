package com.ihor.thesystem.feature.statistics.ui.components.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.core.ui.components.sciPanel
import com.ihor.thesystem.feature.status.viewmodel.QuestUiModel
import com.ihor.thesystem.feature.status.viewmodel.TaskUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestChecklistSheet(
    quest: QuestUiModel,
    accentColor: Color,
    onTaskToggle: (TaskUiModel) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor   = PanelSurface,
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
                    text          = quest.title,
                    color         = accentColor,
                    fontFamily    = FontFamily.Monospace,
                    fontWeight    = FontWeight.Bold,
                    fontSize      = 15.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text       = quest.subtitle,
                    color      = TextSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 11.sp
                )
            }

            Divider(color = accentColor.copy(alpha = 0.25f), thickness = 1.dp)

            if (quest.tasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = "[ ЗАВДАННЯ ВІДСУТНІ ]",
                        color      = TextSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize   = 12.sp
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(quest.tasks, key = { it.id }) { task ->
                        TaskRow(
                            task        = task,
                            accentColor = accentColor,
                            onToggle    = { onTaskToggle(task) }
                        )
                    }
                }
            }

            if (quest.isCompleted) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .sciPanel(
                            borderColor     = NeonGreen,
                            backgroundColor = NeonGreen.copy(alpha = 0.1f),
                            cornerCut       = 8.dp
                        )
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = "✓ КВЕСТ ВИКОНАНО",
                        color      = NeonGreen,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 13.sp
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
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .sciPanel(
                borderColor     = if (task.isCompleted) NeonGreen.copy(0.4f) else accentColor.copy(0.25f),
                backgroundColor = if (task.isCompleted) NeonGreen.copy(0.07f) else PanelSurface,
                cornerCut       = 6.dp
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(onClick = onToggle, modifier = Modifier.size(28.dp)) {
            Icon(
                imageVector = if (task.isCompleted) Icons.Filled.CheckCircle
                else Icons.Filled.RadioButtonUnchecked,
                contentDescription = null,
                tint     = if (task.isCompleted) NeonGreen else accentColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text           = task.name,
            color          = if (task.isCompleted) TextSecondary else TextPrimary,
            fontFamily     = FontFamily.Monospace,
            fontSize       = 13.sp,
            textDecoration = if (task.isCompleted) TextDecoration.LineThrough
            else TextDecoration.None,
            modifier       = Modifier.weight(1f)
        )
    }
}