package com.ihor.thesystem.feature.status.ui

import androidx.compose.runtime.Composable
import com.ihor.thesystem.feature.status.ui.components.dialogs.MainQuestWorkoutDialog
import com.ihor.thesystem.feature.status.ui.components.dialogs.WorkoutReportDialog
import com.ihor.thesystem.feature.status.viewmodel.ActiveDayUiModel
import com.ihor.thesystem.feature.status.viewmodel.StatusDialogState
import com.ihor.thesystem.feature.status.viewmodel.WorkoutScheduleSettingsUiState
import com.ihor.thesystem.feature.status.viewmodel.WorkoutViewModel

@Composable
fun WorkoutDialogHost(
    dialogState: StatusDialogState,
    activeDayWorkout: ActiveDayUiModel?,
    settingsUiState: WorkoutScheduleSettingsUiState,
    workoutViewModel: WorkoutViewModel,
    onOpenWorkoutAnalysis: () -> Unit = {}
) {
    when (dialogState) {
        is StatusDialogState.MainQuestWorkout -> MainQuestWorkoutDialog(
            data = activeDayWorkout,
            onSetWeightChanged = { exerciseId, setId, weight ->
                workoutViewModel.onSetWeightChanged(exerciseId, setId, weight)
            },
            onSetRepsChanged = { exerciseId, setId, reps ->
                workoutViewModel.onSetRepsChanged(exerciseId, setId, reps)
            },
            onSetFocusLost = { exerciseId, setId -> workoutViewModel.onSetFocusLost(exerciseId, setId) },
            onSetCompleted = { exerciseId, setId -> workoutViewModel.onSetCompleted(exerciseId, setId) },
            onOpenSetup = { workoutViewModel.onOpenSetup(it, fromWorkout = true) },
            onFinishWorkout = { workoutViewModel.onFinishWorkout() },
            onDismiss = { workoutViewModel.onDismissDialog() }
        )
        is StatusDialogState.SetupMatrix -> {
            com.ihor.thesystem.feature.statistics.ui.dialogs.SetupMatrixDialog(
                exerciseName = dialogState.entry.exerciseName,
                initialStart = dialogState.startWeight,
                initialTarget = dialogState.targetWeight,
                onConfirm = { start, target ->
                    workoutViewModel.onConfirmSetup(dialogState.entry.exerciseId, start, target)
                },
                onDismiss = {
                    if (dialogState.showWorkoutAfter) {
                        workoutViewModel.onOpenMainWorkout()
                    } else {
                        workoutViewModel.onDismissDialog()
                    }
                }
            )
        }
        is StatusDialogState.LogWorkoutSets -> {
            com.ihor.thesystem.feature.statistics.ui.dialogs.LogWorkoutSetsDialog(
                exerciseName = dialogState.entry.exerciseName,
                sets = dialogState.sets,
                onUpdate = { id, weight, reps ->
                    workoutViewModel.updateLogSetInput(id, weight, reps)
                },
                onAdd = { workoutViewModel.addLogSet() },
                onRemove = { workoutViewModel.removeLogSet() },
                onSave = { feedback ->
                    workoutViewModel.onLogSetsConfirmed(dialogState.entry.exerciseId, dialogState.sets, feedback)
                },
                onDismiss = {
                    if (dialogState.showWorkoutAfter) {
                        workoutViewModel.onOpenMainWorkout()
                    } else {
                        workoutViewModel.onDismissDialog()
                    }
                },
                existingLogs = dialogState.existingLogs
            )
        }
        is StatusDialogState.WorkoutScheduleSettings -> {
            com.ihor.thesystem.feature.status.ui.components.dialogs.WorkoutScheduleSettingsDialog(
                uiState = settingsUiState,
                onDismiss = { workoutViewModel.onDismissDialog() },
                onSelectDay = { workoutViewModel.onSettingsSelectDay(it) },
                onWorkoutNameChange = { workoutViewModel.onWorkoutNameChange(it) },
                onSaveWorkoutName = { workoutViewModel.onSaveWorkoutName() },
                onAddExercise = { workoutViewModel.onAddExerciseToDay(it.toInt()) },
                onRemoveExercise = { workoutViewModel.onRemoveExerciseFromDay(it) },
                onDeleteAllExercises = { },
                onCreateNewExercise = { workoutViewModel.onCreateExercise(it) },
                onDeleteExercise = { workoutViewModel.onDeleteExercise(it) }
            )
        }
        is StatusDialogState.WorkoutReport -> WorkoutReportDialog(
            report = dialogState.report,
            onDismiss = { workoutViewModel.onDismissDialog() },
            onOpenAnalysis = {
                workoutViewModel.onDismissDialog()
                onOpenWorkoutAnalysis()
            }
        )
        else -> Unit
    }
}
