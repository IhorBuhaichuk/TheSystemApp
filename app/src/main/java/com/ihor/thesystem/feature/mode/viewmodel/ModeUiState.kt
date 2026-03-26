package com.ihor.thesystem.feature.mode.viewmodel

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
    val dailyTasks: List<ActiveTaskUiModel>,
    val workoutName: String?,
    val exercises: List<String>
)

data class ActiveTaskUiModel(
    val name: String,
    val isSystemTask: Boolean = true   // для відображення іконки
)