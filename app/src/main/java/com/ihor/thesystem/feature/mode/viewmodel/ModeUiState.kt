package com.ihor.thesystem.feature.mode.viewmodel

import com.ihor.thesystem.domain.model.Quest

data class ModeUiData(
    val currentCycleDay: Int             = 1,
    val days: List<CycleDayUiModel>      = emptyList(),
    val activeDayData: ActiveDayUiModel? = null,
    val isPenaltyActive: Boolean         = false
)

data class CycleDayUiModel(
    val dayNumber: Int,
    val label: String,
    val type: DayType,
    val isActive: Boolean,
    val workoutName: String? = null
)

enum class DayType { REST, WORKOUT }

data class ActiveDayUiModel(
    val dayNumber: Int,
    val debuffName: String?,
    val dailyTasks: List<Quest>,
    val workoutName: String?,
    val exercises: List<ExerciseWorkoutUiModel>
)

data class ExerciseWorkoutUiModel(
    val name: String,
    val recommendation: String? = null
)

data class ActiveTaskUiModel(
    val name: String,
    val isSystemTask: Boolean = true
)
