package com.ihor.thesystem.domain.model

data class Player(
    val id: Int,
    val name: String,
    val level: Int,
    val playerClass: String,
    val height: Float,
    val currentMonth: Int,
    val currentWeek: Int,
    val currentCycleDay: Int,
    val consecutiveFailedMainQuests: Int,
    val isPenaltyActive: Boolean
)

data class Quest(
    val id: Int,
    val title: String,
    val type: DomainQuestType,
    val date: Long,
    val status: DomainQuestStatus,
    val tasks: List<QuestTask>
)

data class QuestTask(
    val id: Int,
    val questId: Int,
    val name: String,
    val isCompleted: Boolean
)

data class SystemConfig(
    val id: Int = 1,
    val defaultPenalty: Int,
    val targetSets: Int,
    val targetReps: Int,
    val matrixWeeks: Int
)

data class DebuffConfig(
    val id: Int,
    val condition: String,
    val text: String,
    val penaltyPercent: Int,
    val isActive: Boolean
)

data class ScheduleDay(
    val id: Int,
    val cycleDay: Int,
    val workoutTemplateId: Int?,
    val workoutTemplateName: String?,
    val dailyTaskNames: List<String>,
    val exerciseNames: List<String>
)