package com.ihor.thesystem.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutDraftCompatibilityTest {

    @Test
    fun `same date and plan restores draft`() {
        assertEquals(
            WorkoutDraftCompatibility.COMPATIBLE,
            draft().compatibilityWith(plan())
        )
    }

    @Test
    fun `midnight preserves draft but does not apply it to today`() {
        assertEquals(
            WorkoutDraftCompatibility.PREVIOUS_DAY,
            draft().compatibilityWith(plan().copy(dateEpochDay = 20_001L))
        )
    }

    @Test
    fun `changed exercise order is treated as a changed plan`() {
        assertEquals(
            WorkoutDraftCompatibility.PLAN_CHANGED,
            draft().compatibilityWith(plan().copy(exerciseIds = listOf(22, 11)))
        )
    }

    @Test
    fun `changed schedule is not silently merged`() {
        assertEquals(
            WorkoutDraftCompatibility.PLAN_CHANGED,
            draft().compatibilityWith(plan().copy(scheduleId = 99))
        )
    }

    private fun draft() = WorkoutDraft(
        id = "draft-1",
        dateEpochDay = 20_000L,
        scheduleId = 3,
        cycleDay = 2,
        workoutTemplateId = 7,
        workoutName = "Workout A",
        questId = 42L,
        exercises = listOf(
            WorkoutDraftExercise(11, listOf(WorkoutDraftSet(1L, "20", "10", true))),
            WorkoutDraftExercise(22, listOf(WorkoutDraftSet(2L, "", "12", false)))
        ),
        createdAtMillis = 100L,
        updatedAtMillis = 200L
    )

    private fun plan() = WorkoutDraftPlan(
        dateEpochDay = 20_000L,
        scheduleId = 3,
        cycleDay = 2,
        workoutTemplateId = 7,
        exerciseIds = listOf(11, 22)
    )
}
