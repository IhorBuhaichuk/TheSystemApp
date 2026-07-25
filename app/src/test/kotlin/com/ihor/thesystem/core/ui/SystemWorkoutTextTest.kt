package com.ihor.thesystem.core.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class SystemWorkoutTextTest {

    @Test
    fun `known system workout names are localized for display`() {
        assertEquals("Тренування А", "Workout A".toUserFacingWorkoutName())
        assertEquals(
            "Усе тіло · тренування А–Б",
            "Full Body / Workout A-B".toUserFacingWorkoutName()
        )
        assertEquals(
            "Активне відновлення",
            "RECOVERY PROTOCOL".toUserFacingWorkoutName()
        )
    }

    @Test
    fun `user workout name stays unchanged`() {
        assertEquals(
            "Моє суботнє тренування",
            "Моє суботнє тренування".toUserFacingWorkoutName()
        )
    }
}
