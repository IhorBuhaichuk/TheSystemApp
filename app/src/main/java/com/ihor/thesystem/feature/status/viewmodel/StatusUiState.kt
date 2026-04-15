package com.ihor.thesystem.feature.status.viewmodel

import com.ihor.thesystem.domain.model.Rank
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class StatusUiData(
    val playerName: String            = "Ігор",
    val playerClass: String           = "Новачок",
    val currentMonth: Int             = 1,
    val totalMonths: Int              = 12,
    val currentWeight: Float          = 80f,
    val height: Float                 = 182f,
    val cycleDay: Int                 = 1,
    val monthWorkoutsCompleted: Int   = 2,
    val monthWorkoutsTotal: Int       = 13,
    val activeDebuffs: ImmutableList<DebuffUiModel> = persistentListOf(),
    val dailyQuest: QuestUiModel?     = null,
    val mainQuest: QuestUiModel?      = null,
    val promotionQuests: ImmutableList<QuestUiModel> = persistentListOf(),
    val globalRank: Rank              = Rank.E,
    val strAttribute: Int = 0,
    val endAttribute: Int = 0,
    val disAttribute: Int = 0,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val xpTotal: Int = 0,
    val xpThisWeek: Int = 0
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
    val tasks: ImmutableList<TaskUiModel> = persistentListOf(),
    val isCompleted: Boolean = false
)

data class TaskUiModel(
    val id: Int,
    val name: String,
    val isCompleted: Boolean
)
