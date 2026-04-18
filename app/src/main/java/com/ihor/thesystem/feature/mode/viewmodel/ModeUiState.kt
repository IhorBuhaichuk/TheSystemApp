package com.ihor.thesystem.feature.mode.viewmodel

import com.ihor.thesystem.domain.model.Quest
import com.ihor.thesystem.feature.statistics.viewmodel.MatrixEntryUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ModeUiData(
    val currentCycleDay: Int             = 1,
    val selectedDay: Int                 = 1,
    val days: ImmutableList<CycleDayUiModel> = persistentListOf(),
    val activeDayData: ActiveDayUiModel? = null,
    val isPenaltyActive: Boolean         = false
)

data class CycleDayUiModel(
    val dayNumber: Int,
    val type: DayType,
    val isActive: Boolean,
    val isSelected: Boolean = false,
    val workoutName: String? = null
)

enum class DayType { REST, WORKOUT }

enum class DebuffType { WEAKNESS, CNS, NONE }

data class ActiveDayUiModel(
    val dayNumber: Int,
    val debuffName: String?,
    val dailyTasks: ImmutableList<Quest>,
    val workoutName: String?,
    val exercises: ImmutableList<ExerciseWorkoutUiModel>,
    val matrixEntries: ImmutableList<MatrixEntryUiModel> = persistentListOf()
)

data class ExerciseWorkoutUiModel(
    val exerciseId: Int,
    val name: String,
    val recommendation: String? = null,
    val sets: ImmutableList<WorkoutSetInput> = persistentListOf()
)

data class WorkoutSetInput(
    val id: Long = System.nanoTime(),
    val weight: String = "",
    val reps: String = ""
)

data class ActiveTaskUiModel(
    val name: String,
    val isSystemTask: Boolean = true
)
