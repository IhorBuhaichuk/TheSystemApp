package com.ihor.thesystem.feature.status.viewmodel

import com.ihor.thesystem.domain.model.ActiveSetInput
import com.ihor.thesystem.feature.statistics.viewmodel.MatrixEntryUiModel

sealed class StatusDialogState {
    data object None                                                       : StatusDialogState()
    data object EditName                                                   : StatusDialogState()
    data object LogWeight                                                  : StatusDialogState()
    data object EditHeight                                                 : StatusDialogState()
    data object EditSystemConfig                                           : StatusDialogState()
    data class QuestChecklist(val questId: Int, val isDaily: Boolean) : StatusDialogState()
    data class AddTask(val questId: Int)                             : StatusDialogState()
    data object MainQuestWorkout                                          : StatusDialogState()
    data object WorkoutScheduleSettings                                   : StatusDialogState()
    data class SetupMatrix(val entry: MatrixEntryUiModel, val startWeight: String, val targetWeight: String, val showWorkoutAfter: Boolean = false) : StatusDialogState()
    data class LogWorkoutSets(val entry: MatrixEntryUiModel, val sets: List<ActiveSetInput>, val existingLogs: List<com.ihor.thesystem.domain.model.ExerciseSet> = emptyList(), val showWorkoutAfter: Boolean = false) : StatusDialogState()
    data class WorkoutReport(val report: com.ihor.thesystem.domain.model.AiArchitectReport) : StatusDialogState()
}
