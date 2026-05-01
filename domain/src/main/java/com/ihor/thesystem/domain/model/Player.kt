package com.ihor.thesystem.domain.model

data class Player(
    val id: Int,
    val name: String,
    val level: Int,
    val playerClass: PlayerRank,
    val height: Float,
    val age: Int = 0,
    val currentMonth: Int,
    val currentWeek: Int,
    val currentCycleDay: Int,
    val isPenaltyActive: Boolean = false,
    val consecutiveMainQuestFailures: Int = 0,
    val globalRank: Rank = Rank.E,
    val avatarUri: String? = null,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val xpTotal: Int = 0,
    val xpThisWeek: Int = 0,
    val chestAttr: Int = 0,
    val backAttr: Int = 0,
    val shouldersAttr: Int = 0,
    val quadsAttr: Int = 0,
    val legsAttr: Int = 0,
    val armsAttr: Int = 0,
    val absAttr: Int = 0,
    val legsGroupAttr: Int = 0,
    val coreAttr: Int = 0
) {
    fun rewardWorkoutCompletion(
        config: PlayerProgressionConfig = PlayerProgressionConfig()
    ): Player {
        val nextStreak = currentStreak + 1
        val nextXpTotal = xpTotal + config.workoutCompletionXp
        return copy(
            level = (nextXpTotal / config.xpPerLevel) + 1,
            currentStreak = nextStreak,
            maxStreak = maxOf(maxStreak, nextStreak),
            xpTotal = nextXpTotal,
            xpThisWeek = xpThisWeek + config.workoutCompletionXp
        )
    }

    /**
     * Оцінює виконання головних квестів, оновлює серії та нараховує досвід.
     */
    fun evaluateQuests(mainQuests: List<Quest>): Player {
        if (mainQuests.isEmpty()) return this
        val allCompleted = mainQuests.all { it.isCompleted }

        return if (!allCompleted) {
            copy(currentStreak = 0)
        } else {
            rewardWorkoutCompletion()
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
        val levelUpTriggered = playerClass != newRank
        val isNewMonthStart = currentCycleDay == 1 && currentWeek == 1

        var updatedPlayer = this
        if (levelUpTriggered) {
            updatedPlayer = updatedPlayer.copy(
                playerClass = newRank,
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

data class PlayerProgressionConfig(
    val xpPerLevel: Int = 1000,
    val workoutCompletionXp: Int = 100
)
