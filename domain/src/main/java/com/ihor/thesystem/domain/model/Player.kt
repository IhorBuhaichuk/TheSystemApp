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
            level = config.levelForXp(nextXpTotal),
            currentStreak = nextStreak,
            maxStreak = maxOf(maxStreak, nextStreak),
            xpTotal = nextXpTotal,
            xpThisWeek = xpThisWeek + config.workoutCompletionXp
        )
    }

    /**
     * Оцінює виконання головних квестів, оновлює серії та нараховує досвід.
     */
    fun evaluateQuests(
        mainQuests: List<Quest>,
        config: PlayerProgressionConfig = PlayerProgressionConfig()
    ): Player {
        if (mainQuests.isEmpty()) return this
        val allCompleted = mainQuests.all { QuestCompletionPolicy.isSuccessful(it) }

        return if (!allCompleted) {
            copy(currentStreak = 0)
        } else {
            rewardWorkoutCompletion(config)
        }
    }

    /**
     * Просуває час у системі: дні мікроциклу, тижні та місяці.
     */
    fun advanceTime(config: SystemConfig, days: Long = 1L): Player {
        val daysToAdvance = days.coerceAtLeast(0L)
        val cycleDays = config.cycleDaysPerMicrocycle.coerceAtLeast(1)
        val microCyclesPerMonth = config.microCyclesPerMonth.coerceAtLeast(1)
        val daysPerMonth = cycleDays.toLong() * microCyclesPerMonth.toLong()
        val currentDayIndex =
            (currentMonth.coerceAtLeast(1) - 1).toLong() * daysPerMonth +
                (currentWeek.coerceAtLeast(1) - 1).toLong() * cycleDays.toLong() +
                (currentCycleDay.coerceIn(1, cycleDays) - 1).toLong()
        val nextDayIndex = currentDayIndex + daysToAdvance
        val nextMonth = (nextDayIndex / daysPerMonth).toInt() + 1
        val indexWithinMonth = nextDayIndex % daysPerMonth
        val nextWeek = (indexWithinMonth / cycleDays).toInt() + 1
        val nextCycleDay = (indexWithinMonth % cycleDays).toInt() + 1

        return copy(
            currentCycleDay = nextCycleDay,
            currentWeek = nextWeek,
            currentMonth = nextMonth
        )
    }

    /**
     * Перевіряє можливість підвищення рангу та скидає тижневий досвід при новому місяці.
     * @return Пара: Оновлений гравець та прапорець LevelUp.
     */
    fun checkLevelUp(
        config: PlayerProgressionConfig = PlayerProgressionConfig()
    ): Pair<Player, Boolean> {
        val newRank = PlayerRank.resolveByMonth(currentMonth)
        val levelUpTriggered = playerClass != newRank
        val isNewMonthStart = currentCycleDay == 1 && currentWeek == 1

        var updatedPlayer = this
        if (levelUpTriggered) {
            updatedPlayer = updatedPlayer.copy(
                playerClass = newRank,
                xpTotal = xpTotal + config.classRankPromotionXp,
                xpThisWeek = xpThisWeek + config.classRankPromotionXp
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
    val workoutCompletionXp: Int = 100,
    val classRankPromotionXp: Int = 200
) {
    init {
        require(xpPerLevel > 0) { "XP per level must be positive." }
        require(workoutCompletionXp >= 0) { "Workout completion XP must not be negative." }
        require(classRankPromotionXp >= 0) { "Class rank promotion XP must not be negative." }
    }

    fun levelForXp(xpTotal: Int): Int = (xpTotal.coerceAtLeast(0) / xpPerLevel) + 1
}
