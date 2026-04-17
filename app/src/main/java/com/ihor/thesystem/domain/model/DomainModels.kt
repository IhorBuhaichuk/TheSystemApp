package com.ihor.thesystem.domain.model

object CycleConfig {
    val MICROCYCLE_DAYS = listOf(1, 2, 3, 4)
    val MICROCYCLE_DAY_RANGE = 1..4
}

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
) {
    /**
     * Оцінює виконання головних квестів, оновлює серії та нараховує досвід.
     * Активує штрафний режим при повторних провалах.
     */
    fun evaluateQuests(mainQuests: List<Quest>): Player {
        if (mainQuests.isEmpty()) return this
        val allCompleted = mainQuests.all { it.isCompleted }

        return if (!allCompleted) {
            val newFailures = consecutiveMainQuestFailures + 1
            copy(
                consecutiveMainQuestFailures = newFailures,
                currentStreak = 0,
                isPenaltyActive = isPenaltyActive || newFailures >= 2
            )
        } else {
            val newStreak = currentStreak + 1
            copy(
                isPenaltyActive = false,
                consecutiveMainQuestFailures = 0,
                currentStreak = newStreak,
                maxStreak = maxOf(maxStreak, newStreak),
                xpTotal = xpTotal + 100,
                xpThisWeek = xpThisWeek + 100
            )
        }
    }

    /**
     * Просуває час у системі: дні мікроциклу, тижні та місяці.
     */
    fun advanceTime(config: SystemConfig): Player {
        var newCycleDay = currentCycleDay + 1
        var newWeek = currentWeek
        var newMonth = currentMonth

        if (newCycleDay > config.cycleDaysPerMicrocycle) {
            newCycleDay = 1
            newWeek += 1
            if (newWeek > config.microCyclesPerMonth) {
                newWeek = 1
                newMonth += 1
            }
        }

        return copy(
            currentCycleDay = newCycleDay,
            currentWeek = newWeek,
            currentMonth = newMonth
        )
    }

    /**
     * Перевіряє можливість підвищення рангу та скидає тижневий досвід при новому місяці.
     * @return Пара: Оновлений гравець та прапорець LevelUp.
     */
    fun checkLevelUp(): Pair<Player, Boolean> {
        val newRank = PlayerRank.resolveByMonth(currentMonth)
        val levelUpTriggered = playerClass != newRank.title
        val isNewMonthStart = currentCycleDay == 1 && currentWeek == 1

        var updatedPlayer = this
        if (levelUpTriggered) {
            updatedPlayer = updatedPlayer.copy(
                playerClass = newRank.title,
                xpTotal = xpTotal + 200,
                xpThisWeek = xpThisWeek + 200
            )
        }

        // Скидання тижневого прогресу при переході на новий місяць
        if (isNewMonthStart) {
            updatedPlayer = updatedPlayer.copy(xpThisWeek = 0)
        }

        return updatedPlayer to levelUpTriggered
    }
}

data class Quest(
    val id: Int,
    val title: String,
    val type: DomainQuestType,
    val date: Long,
    val status: DomainQuestStatus,
    val tasks: List<QuestTask>,
    val scheduleId: Int? = null,
    val targetExerciseId: Int? = null
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
    val defaultPenalty: Int = 20,
    val targetSets: Int = 3,
    val targetReps: Int = 12,
    val matrixWeeks: Int = 48,
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
    val isActive: Boolean,
    val cycleDay: Int? = null
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
