package com.ihor.thesystem.feature.profile.ui

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ProfilePerformanceGuardTest {

    @Test
    fun `profile reuses lightweight status data for personal metrics`() {
        val source = sourceFile().readText()

        assertFalse(
            "Profile must not start the full statistics pipeline for personal metrics.",
            "StatisticsViewModel" in source || "statisticsViewModel" in source
        )
        assertTrue(
            "Profile age must come from the already observed status data.",
            "val age = statusData.age" in source
        )
        val profileParameters = source
            .substringAfter("fun ProfileScreen(")
            .substringBefore(") {")
        assertFalse(
            "Profile must not create WorkoutViewModel until workout settings are requested.",
            "WorkoutViewModel" in profileParameters
        )
        assertTrue(
            "Workout settings must be hosted only after an explicit request.",
            "if (workoutSettingsRequestId > 0)" in source &&
                "ProfileWorkoutSettingsHost(" in source
        )
    }

    private fun sourceFile(): File =
        requireNotNull(File(requireNotNull(System.getProperty("user.dir"))).absoluteFile.parentFile)
            .resolve("app/src/main/java/com/ihor/thesystem/feature/profile/ui/ProfileScreen.kt")
}
