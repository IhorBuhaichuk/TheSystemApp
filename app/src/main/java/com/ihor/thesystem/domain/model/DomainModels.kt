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
    val consecutiveMainQuestFailures: Int,
    val isPenaltyActive: Boolean,
    val globalRank: Rank = Rank.E,
    val strAttribute: Int = 0,
    val endAttribute: Int = 0,
    val disAttribute: Int = 0,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val xpTotal: Int = 0,
    val xpThisWeek: Int = 0
)

data class Quest(
    val id: Int,
    val title: String,
    val type: DomainQuestType,
    val date: Long,
    val status: DomainQuestStatus,
    val tasks: List<QuestTask>,
    val scheduleId: Int? = null
) {
    val isCompleted: Boolean get() = status == DomainQuestStatus.COMPLETED
}

data class QuestTask(
    val id: Int,
    val questId: Int,
    val name: String,
    val isCompleted: Boolean,
    val exerciseId: Int? = null,
    val recommendedWeight: Double? = null,
    val recommendedSets: Int? = null,
    val recommendedReps: Int? = null
)

data class SystemConfig(
    val id: Int = 1,
    val defaultPenalty: Int,
    val targetSets: Int,
    val targetReps: Int,
    val matrixWeeks: Int,
    val cycleAnchorDateTimestamp: Long = 0L, // Epoch Day
    val cycleAnchorDay: Int = 1,              // Який це був день циклу (1..4)
    val cycleDaysPerMicrocycle: Int = 4,
    val microCyclesPerMonth: Int = 4
)

data class DebuffConfig(
    val id: Int,
    val condition: String,
    val text: String,
    val penaltyPercent: Int,
    val isActive: Boolean
) {
    val penaltyPercentage: Double get() = penaltyPercent.toDouble()
}

data class ScheduleDay(
    val id: Int,
    val cycleDay: Int,
    val workoutTemplateId: Int?,
    val workoutTemplateName: String?,
    val dailyTaskNames: List<String>,
    val exercises: List<ExerciseDetails>
)

data class ExerciseDetails(
    val id: Int,
    val name: String
)

data class ExerciseRecommendation(
    val exerciseId: Int,
    val exerciseName: String,
    val weight: Double,
    val sets: Int,
    val reps: Int
)
