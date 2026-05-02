package com.ihor.thesystem.domain.model

data class StatusData(
    val playerName: String = "",
    val playerClass: PlayerRank = PlayerRank.NOVICE,
    val level: Int = 1,
    val xpTotal: Int = 0,
    val xpMax: Int = 1000,
    val currentMonth: Int = 1,
    val totalMonths: Int = 12,
    val currentWeight: Float? = null,
    val height: Float? = null,
    val cycleDay: Int = 1,
    val monthWorkoutsCompleted: Int = 0,
    val monthWorkoutsTotal: Int = 0,
    val todos: List<TodoItem> = emptyList(),
    val dailyQuest: Quest? = null,
    val mainQuest: Quest? = null,
    val promotionQuests: List<Quest> = emptyList(),
    val globalRank: Rank = Rank.E,
    val characterAttributes: Map<MuscleGroup, Float> = emptyMap(),
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val xpThisWeek: Int = 0,
    val avatarUri: String? = null
)
