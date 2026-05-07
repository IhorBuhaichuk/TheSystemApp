package com.ihor.thesystem.feature.status.viewmodel

import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.domain.model.Rank
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDate

import com.ihor.thesystem.domain.model.PlayerRank

data class StatusUiData(
    val playerName: String            = "Ігор",
    val playerClass: PlayerRank        = PlayerRank.NOVICE,
    val level: Int                    = 1,
    val xpTotal: Int                  = 0,
    val xpMax: Int                    = 1000,
    val currentMonth: Int             = 1,
    val totalMonths: Int              = 12,
    val currentWeight: Float?         = null,
    val height: Float?                = null,
    val cycleDay: Int                 = 1,
    val monthWorkoutsCompleted: Int   = 0,
    val monthWorkoutsTotal: Int       = 0,
    val todos: ImmutableList<TodoUiModel> = persistentListOf(),
    val dailyQuest: QuestUiModel?     = null,
    val mainQuest: QuestUiModel?      = null,
    val promotionQuests: ImmutableList<QuestUiModel> = persistentListOf(),
    val globalRank: Rank              = Rank.E,
    val characterAttributes: Map<com.ihor.thesystem.domain.model.MuscleGroup, Float> = emptyMap(),
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val xpThisWeek: Int = 0,
    val avatarUri: String? = null,
    val weekPreview: ImmutableList<StatusWeekDayUiModel> = persistentListOf()
)

enum class StatusWeekDayVisualType {
    WORK,
    TRAINING,
    MIXED,
    REST
}

enum class StatusWeekDayStatus {
    COMPLETED,
    PARTIAL,
    MISSED,
    PLANNED,
    NO_DATA
}

data class StatusWeekDayUiModel(
    val date: LocalDate,
    val weekDayLabel: String,
    val dayNumber: String,
    val visualType: StatusWeekDayVisualType,
    val status: StatusWeekDayStatus,
    val isToday: Boolean
)

data class QuestUiModel(
    val id: Int,
    val title: String,
    val subtitle: UiText,
    val tasks: ImmutableList<TaskUiModel> = persistentListOf(),
    val isCompleted: Boolean = false
)

data class TaskUiModel(
    val id: Int,
    val name: String,
    val nameUk: String? = null,
    val isCompleted: Boolean
)

data class TodoUiModel(
    val id: Int,
    val title: String,
    val isCompleted: Boolean,
    val parentTodoId: Int? = null,
    val microtasks: ImmutableList<TodoUiModel> = persistentListOf()
)
