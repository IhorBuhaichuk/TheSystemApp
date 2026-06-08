package com.ihor.thesystem.feature.status.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.ihor.thesystem.feature.status.ui.components.dialogs.MainQuestWorkoutDialog
import com.ihor.thesystem.feature.status.ui.components.dialogs.WorkoutReportDialog
import com.ihor.thesystem.feature.status.ui.components.dialogs.WorkoutScheduleSettingsScreen
import com.ihor.thesystem.feature.status.viewmodel.ActiveDayUiModel
import com.ihor.thesystem.feature.status.viewmodel.StatusDialogState
import com.ihor.thesystem.feature.status.viewmodel.WorkoutScheduleSettingsUiState
import com.ihor.thesystem.feature.status.viewmodel.WorkoutViewModel
import com.ihor.thesystem.health.HealthConnectPermissions

@Composable
fun WorkoutDialogHost(
    dialogState: StatusDialogState,
    activeDayWorkout: ActiveDayUiModel?,
    settingsUiState: WorkoutScheduleSettingsUiState,
    workoutViewModel: WorkoutViewModel,
    onOpenWorkoutAnalysis: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    val healthPermissionLauncher = rememberLauncherForActivityResult(
        contract = HealthConnectPermissions.requestContract()
    ) {
        workoutViewModel.onHealthConnectPermissionsChanged()
    }
    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        workoutViewModel.exportBackupJson { json ->
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(json.toByteArray(Charsets.UTF_8))
                } ?: error("Backup output stream is not available.")
            }.onFailure {
                workoutViewModel.onBackupFileOperationFailed()
            }
        }
    }
    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { reader ->
                reader.readText()
            } ?: error("Backup input stream is not available.")
        }.onSuccess { rawJson ->
            workoutViewModel.importBackupJson(rawJson)
        }.onFailure {
            workoutViewModel.onBackupFileOperationFailed()
        }
    }

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
            onSetCompletionChanged = { exerciseId, setId, completed ->
                workoutViewModel.onSetCompletionChanged(exerciseId, setId, completed)
            },
            onAddSet = { exerciseId, weight -> workoutViewModel.onAddWorkoutSet(exerciseId, weight) },
            onOpenSetup = { workoutViewModel.onOpenSetup(it, fromWorkout = true) },
            onFinishWorkout = { workoutViewModel.onFinishWorkout() },
            onDismiss = { workoutViewModel.onDismissDialog() }
        )
        is StatusDialogState.SetupMatrix -> {
            com.ihor.thesystem.feature.statistics.ui.dialogs.SetupMatrixDialog(
                exerciseName = dialogState.entry.exerciseName,
                initialStart = dialogState.startWeight,
                initialTarget = dialogState.targetWeight,
                usesExternalLoad = dialogState.entry.usesExternalLoad,
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
                trackingMode = dialogState.trackingMode,
                onUpdate = { id, weight, reps ->
                    workoutViewModel.updateLogSetInput(id, weight, reps)
                },
                onAdd = { workoutViewModel.addLogSet() },
                onRemove = { workoutViewModel.removeLogSet() },
                onSave = { feedback ->
                    workoutViewModel.onLogSetsConfirmed(
                        exerciseId = dialogState.entry.exerciseId,
                        sets = dialogState.sets,
                        feedback = feedback,
                        trackingMode = dialogState.trackingMode
                    )
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
            WorkoutScheduleSettingsScreen(
                uiState = settingsUiState,
                onDismiss = { workoutViewModel.onDismissDialog() },
                onSelectDay = { workoutViewModel.onSettingsSelectDay(it) },
                onWorkoutNameChange = { workoutViewModel.onWorkoutNameChange(it) },
                onSaveWorkoutName = { workoutViewModel.onSaveWorkoutName() },
                onAddExercise = { workoutViewModel.onAddExerciseToDay(it.toInt()) },
                onRemoveExercise = { workoutViewModel.onRemoveExerciseFromDay(it) },
                onDeleteAllExercises = { },
                onCreateNewExercise = { workoutViewModel.onCreateExercise(it) },
                onDeleteExercise = { workoutViewModel.onDeleteExercise(it) },
                onTrackingModeChanged = { exerciseId, trackingMode ->
                    workoutViewModel.onExerciseTrackingModeChanged(exerciseId, trackingMode)
                },
                onEquipmentLocationChanged = { workoutViewModel.onEquipmentLocationChanged(it) },
                onEquipmentAvailabilityChanged = { type, available ->
                    workoutViewModel.onEquipmentAvailabilityChanged(type, available)
                },
                onDumbbellMaxKgChanged = { workoutViewModel.onDumbbellMaxKgChanged(it) },
                onConnectHealthConnect = {
                    healthPermissionLauncher.launch(
                        HealthConnectPermissions.permissionsFor(
                            workoutViewModel.healthConnectPermissionRequest()
                        )
                    )
                },
                onExportBackup = {
                    exportBackupLauncher.launch("the-system-backup.json")
                },
                onImportBackup = {
                    importBackupLauncher.launch(arrayOf("application/json", "text/*"))
                }
            )
        }
        is StatusDialogState.WorkoutReport -> WorkoutReportDialog(
            report = dialogState.report,
            onDismiss = { workoutViewModel.onDismissDialog() },
            onOpenAnalysis = {
                workoutViewModel.onDismissDialog()
                onOpenWorkoutAnalysis(dialogState.report.sessionId)
            }
        )
        else -> Unit
    }
}
