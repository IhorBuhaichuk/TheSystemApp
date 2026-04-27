package com.ihor.thesystem.feature.status.viewmodel

import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.domain.model.Rank
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

import com.ihor.thesystem.domain.model.PlayerRank

data class StatusUiData(
    val playerName: String            = "Ігор",
    val playerClass: PlayerRank        = PlayerRank.NOVICE,
    val level: Int                    = 1,
    val xpTotal: Int                  = 0,
    val xpMax: Int                    = 1000,
    val currentMonth: Int             = 1,
    val totalMonths: Int              = 12,
    val currentWeight: Float          = 80f,
    val height: Float                 = 182f,
    val cycleDay: Int                 = 1,
    val monthWorkoutsCompleted: Int   = 2,
    val monthWorkoutsTotal: Int       = 13,
    val dailyQuest: QuestUiModel?     = null,
    val mainQuest: QuestUiModel?      = null,
    val promotionQuests: ImmutableList<QuestUiModel> = persistentListOf(),
    val globalRank: Rank              = Rank.E,
    val characterAttributes: Map<com.ihor.thesystem.domain.model.MuscleGroup, Float> = emptyMap(),
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val xpThisWeek: Int = 0,
    val avatarUri: String? = null
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
