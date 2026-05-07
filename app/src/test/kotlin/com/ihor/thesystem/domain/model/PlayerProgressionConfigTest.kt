package com.ihor.thesystem.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class PlayerProgressionConfigTest {

    @Test
    fun `workout reward uses configured XP and resolves level from total XP`() {
        val player = player(
            level = 1,
            currentStreak = 2,
            maxStreak = 4,
            xpTotal = 90,
            xpThisWeek = 10
        )
        val config = PlayerProgressionConfig(
            xpPerLevel = 100,
            workoutCompletionXp = 60
        )

        val result = player.rewardWorkoutCompletion(config)

        assertEquals(2, result.level)
        assertEquals(3, result.currentStreak)
        assertEquals(4, result.maxStreak)
        assertEquals(150, result.xpTotal)
        assertEquals(70, result.xpThisWeek)
    }

    @Test
    fun `class rank promotion XP is configured instead of embedded in player logic`() {
        val player = player(
            currentMonth = 2,
            currentWeek = 2,
            currentCycleDay = 1,
            playerClass = PlayerRank.NOVICE,
            xpTotal = 10,
            xpThisWeek = 5
        )
        val config = PlayerProgressionConfig(classRankPromotionXp = 50)

        val (result, didLevelUp) = player.checkLevelUp(config)

        assertTrue(didLevelUp)
        assertEquals(PlayerRank.APPRENTICE, result.playerClass)
        assertEquals(60, result.xpTotal)
        assertEquals(55, result.xpThisWeek)
    }

    @Test
    fun `progression config rejects invalid XP rules`() {
        assertInvalidConfig { PlayerProgressionConfig(xpPerLevel = 0) }
        assertInvalidConfig { PlayerProgressionConfig(workoutCompletionXp = -1) }
        assertInvalidConfig { PlayerProgressionConfig(classRankPromotionXp = -1) }
    }

    private fun assertInvalidConfig(block: () -> Unit) {
        try {
            block()
            fail("Expected invalid progression config")
        } catch (_: IllegalArgumentException) {
            // Expected.
        }
    }

    private fun player(
        level: Int = 1,
        currentMonth: Int = 1,
        currentWeek: Int = 1,
        currentCycleDay: Int = 1,
        playerClass: PlayerRank = PlayerRank.NOVICE,
        currentStreak: Int = 0,
        maxStreak: Int = 0,
        xpTotal: Int = 0,
        xpThisWeek: Int = 0
    ) = Player(
        id = 1,
        name = "Test",
        level = level,
        playerClass = playerClass,
        height = 180f,
        currentMonth = currentMonth,
        currentWeek = currentWeek,
        currentCycleDay = currentCycleDay,
        currentStreak = currentStreak,
        maxStreak = maxStreak,
        xpTotal = xpTotal,
        xpThisWeek = xpThisWeek
    )
}
