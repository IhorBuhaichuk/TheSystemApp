package com.ihor.thesystem.feature.status.viewmodel

data class StatusUiData(
    val playerName: String            = "ІГОР",
    val playerClass: String           = "ПРОБУДЖЕНИЙ",
    val currentMonth: Int             = 1,
    val totalMonths: Int              = 12,
    val currentWeight: Float          = 80f,
    val height: Float                 = 182f,
    val cycleDay: Int                 = 1,
    val monthWorkoutsCompleted: Int   = 2,
    val monthWorkoutsTotal: Int       = 13,
    val activeDebuffs: List<DebuffUiModel> = emptyList(),
    val dailyQuest: QuestUiModel?     = null,
    val mainQuest: QuestUiModel?      = null
)

data class DebuffUiModel(
    val id: Int,
    val condition: String,
    val text: String,
    val penaltyPercent: Int,
    val isActive: Boolean = false
)

data class QuestUiModel(
    val id: Int,
    val title: String,
    val subtitle: String,
    val tasks: List<TaskUiModel> = emptyList(),
    val isCompleted: Boolean = false
)

data class TaskUiModel(
    val id: Int,
    val name: String,
    val isCompleted: Boolean
)