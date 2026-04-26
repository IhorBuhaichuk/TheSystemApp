package com.ihor.thesystem.feature.status.viewmodel

import androidx.compose.runtime.Immutable
import com.ihor.thesystem.domain.model.ActiveSetInput
import com.ihor.thesystem.feature.statistics.viewmodel.MatrixEntryUiModel
import com.ihor.thesystem.domain.model.ExerciseDetails
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class WorkoutScheduleSettingsUiState(
    val selectedDay: Int = 1,
    val totalCycleDays: Int = 4,
    val workoutNameDraft: String = "",
    val exercisesForSelectedDay: ImmutableList<ExerciseDetails> = persistentListOf(),
    val allExercises: ImmutableList<ExerciseDetails> = persistentListOf(),
    val isLoading: Boolean = false
)

@Immutable
data class ActiveDayUiModel(
    val dayNumber: Int,
    val dailyTasks: ImmutableList<com.ihor.thesystem.domain.model.Quest>,
    val workoutName: String?,
    val exercises: ImmutableList<ExerciseWorkoutUiModel>,
    val matrixEntries: ImmutableList<MatrixEntryUiModel> = persistentListOf()
)

@Immutable
data class ExerciseWorkoutUiModel(
    val exerciseId: Int,
    val name: String,
    val recommendedWeight: Double? = null,
    val recommendedReps: Int? = null,
    val recommendedSets: Int? = null,
    val recommendation: String? = null,
    val gifUrl: String? = null,
    val sets: ImmutableList<ActiveSetInput> = persistentListOf()
)

@Immutable
data class CycleDayUiModel(
    val dayNumber: Int,
    val type: DayType,
    val isActive: Boolean,
    val isSelected: Boolean = false,
    val workoutName: String? = null
)

enum class DayType { REST, WORKOUT }
