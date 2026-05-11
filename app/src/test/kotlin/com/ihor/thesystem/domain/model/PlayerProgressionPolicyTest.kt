package com.ihor.thesystem.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlayerProgressionPolicyTest {

    @Test
    fun `first failed main quest resets streak and increments failure counter`() {
        val result = PlayerProgressionPolicy.applyMainQuestFailure(
            player(currentStreak = 4, consecutiveFailures = 0)
        ).player

        assertEquals(0, result.currentStreak)
        assertEquals(1, result.consecutiveMainQuestFailures)
        assertFalse(result.isPenaltyActive)
    }

    @Test
    fun `second failed main quest activates penalty`() {
        val result = PlayerProgressionPolicy.applyMainQuestFailure(
            player(consecutiveFailures = 1)
        )

        assertEquals(2, result.player.consecutiveMainQuestFailures)
        assertTrue(result.player.isPenaltyActive)
        assertTrue(result.penaltyActivated)
    }

    @Test
    fun `successful main quest resets failure counter`() {
        val result = PlayerProgressionPolicy.applyMainQuestSuccess(
            player(consecutiveFailures = 1),
            reward = true
        ).player

        assertEquals(0, result.consecutiveMainQuestFailures)
        assertFalse(result.isPenaltyActive)
        assertEquals(100, result.xpTotal)
    }

    @Test
    fun `successful main quest after penalty deactivates penalty`() {
        val result = PlayerProgressionPolicy.applyMainQuestSuccess(
            player(consecutiveFailures = 2, isPenaltyActive = true),
            reward = true
        ).player

        assertEquals(0, result.consecutiveMainQuestFailures)
        assertFalse(result.isPenaltyActive)
    }

    private fun player(
        currentStreak: Int = 0,
        consecutiveFailures: Int = 0,
        isPenaltyActive: Boolean = false
    ): Player =
        Player(
            id = 1,
            name = "Player",
            level = 1,
            playerClass = PlayerRank.NOVICE,
            height = 180f,
            currentMonth = 1,
            currentWeek = 1,
            currentCycleDay = 1,
            currentStreak = currentStreak,
            maxStreak = currentStreak,
            consecutiveMainQuestFailures = consecutiveFailures,
            isPenaltyActive = isPenaltyActive
        )
}
