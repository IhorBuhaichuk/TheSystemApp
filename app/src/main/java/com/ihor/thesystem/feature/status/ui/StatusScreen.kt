package com.ihor.thesystem.feature.status.ui

import androidx.compose.foundation.background
import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.ihor.thesystem.core.navigation.Routes
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemScreenPadding
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.core.ui.RefreshOnResume
import com.ihor.thesystem.core.ui.components.DarkGlassCard
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemStateKind
import com.ihor.thesystem.core.ui.components.SystemStatePanel
import com.ihor.thesystem.feature.status.ui.components.dialogs.AddTaskDialog
import com.ihor.thesystem.feature.status.ui.components.dialogs.LevelUpDialog
import com.ihor.thesystem.feature.status.viewmodel.StatusDialogState
import com.ihor.thesystem.feature.status.viewmodel.StatusOneOffEvent
import com.ihor.thesystem.feature.status.viewmodel.StatusUiData
import com.ihor.thesystem.feature.status.viewmodel.StatusViewModel
import com.ihor.thesystem.presentation.common.components.RpgStatusBackdrop

@Composable
fun StatusScreen(
    navController: NavHostController,
    statusViewModel: StatusViewModel = hiltViewModel()
) {
    val uiState by statusViewModel.uiState.collectAsStateWithLifecycle()
    val statusDialogState by statusViewModel.dialogState.collectAsStateWithLifecycle()
    val colors = SystemTheme.colors

    ReportDrawnWhen {
        uiState is UiState.Content<*> || uiState is UiState.Error
    }

    var levelUpEvent by remember { mutableStateOf<StatusOneOffEvent.ShowLevelUp?>(null) }

    RefreshOnResume(statusViewModel::refreshForCurrentDay)

    LaunchedEffect(Unit) {
        statusViewModel.events.collect { event ->
            if (event is StatusOneOffEvent.ShowLevelUp) {
                levelUpEvent = event
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        RpgStatusBackdrop()

        when (val state = uiState) {
            is UiState.Loading -> StatusLoading()
            is UiState.Content<*> -> {
                val data = state.data as StatusUiData
                RpgStatusDashboard(
                    data = data,
                    onAvatarSelected = { statusViewModel.updateAvatarUri(it) },
                    onEditNameTap = { statusViewModel.onEditNameTap() },
                    onStartWorkout = { navController.navigate(Routes.Cycle) },
                    onOpenCalendar = {
                        statusViewModel.onOpenCalendarTap()
                        navController.navigate(Routes.Calendar)
                    },
                    onOpenWorkoutSettings = { navController.navigate(Routes.Cycle) },
                    onTaskToggled = { todo -> statusViewModel.onTodoToggled(todo) },
                    onAddTask = { questId -> statusViewModel.onAddTaskTap(questId) },
                    onAddMicrotask = { todo -> statusViewModel.onAddMicrotaskTap(todo) },
                    onTodosReordered = { orderedIds -> statusViewModel.onTodosReordered(orderedIds) },
                    onRemoveTask = { todoId -> statusViewModel.onRemoveTodo(todoId) }
                )
            }
            is UiState.Error -> DatabaseErrorScreen(
                message = state.message,
                onRetry = statusViewModel::refreshForCurrentDay
            )
        }

        levelUpEvent?.let { event ->
            LevelUpDialog(
                newClass = event.newClass,
                newMonth = event.newMonth,
                onDismiss = { levelUpEvent = null }
            )
        }

        StatusDialogs(
            dialogState = statusDialogState,
            uiState = uiState,
            statusViewModel = statusViewModel
        )
    }
}

@Composable
private fun StatusLoading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(SystemScreenPadding),
        contentAlignment = Alignment.Center
    ) {
        SystemStatePanel(
            kind = SystemStateKind.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp)
        )
    }
}

@Composable
private fun StatusDialogs(
    dialogState: StatusDialogState,
    uiState: UiState<StatusUiData>,
    statusViewModel: StatusViewModel
) {
    when (dialogState) {
        is StatusDialogState.EditName -> {
            val currentData = (uiState as? UiState.Content<StatusUiData>)?.data
            if (currentData != null) {
                com.ihor.thesystem.feature.statistics.ui.components.dialogs.EditNameDialog(
                    currentName = currentData.playerName,
                    onConfirm = { statusViewModel.onNameConfirmed(it) },
                    onDismiss = { statusViewModel.onDismissDialog() }
                )
            }
        }
        is StatusDialogState.AddTask -> AddTaskDialog(
            onConfirm = { statusViewModel.onAddTaskConfirmed(dialogState.questId, it) },
            onDismiss = { statusViewModel.onDismissDialog() }
        )
        is StatusDialogState.AddMicrotask -> AddTaskDialog(
            onConfirm = { statusViewModel.onAddMicrotaskConfirmed(dialogState.parentTodoId, it) },
            onDismiss = { statusViewModel.onDismissDialog() },
            titleText = "Нова мікрозадача",
            subtitleText = "До: ${dialogState.parentTitle}",
            placeholderText = "Що саме треба зробити?"
        )
        else -> Unit
    }
}

@Composable
private fun DatabaseErrorScreen(
    message: UiText,
    onRetry: () -> Unit
) {
    val context = LocalContext.current
    val colors = SystemTheme.colors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(SystemScreenPadding),
        contentAlignment = Alignment.Center
    ) {
        SystemStatePanel(
            kind = SystemStateKind.Error,
            title = "Не вдалося завантажити дані",
            message = message.asString(context),
            actionLabel = "Повторити",
            onAction = onRetry,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp)
        )
    }
}
