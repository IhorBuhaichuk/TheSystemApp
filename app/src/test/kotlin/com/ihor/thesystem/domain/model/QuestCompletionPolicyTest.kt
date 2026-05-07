package com.ihor.thesystem.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestCompletionPolicyTest {

    @Test
    fun `quest without tasks is successful during day finalization`() {
        val result = QuestCompletionPolicy.resolveForDayFinalization(
            quest = quest(tasks = emptyList())
        )

        assertEquals(DomainQuestStatus.COMPLETED, result.status)
        assertTrue(result.wasSuccessful)
    }

    @Test
    fun `incomplete quest fails unless forced`() {
        val result = QuestCompletionPolicy.resolveForDayFinalization(
            quest = quest(tasks = listOf(task(isCompleted = true), task(isCompleted = false)))
        )

        assertEquals(DomainQuestStatus.FAILED, result.status)
        assertFalse(result.wasSuccessful)
    }

    @Test
    fun `force complete marks incomplete quest as successful`() {
        val result = QuestCompletionPolicy.resolveForDayFinalization(
            quest = quest(tasks = listOf(task(isCompleted = false))),
            forceComplete = true
        )

        assertEquals(DomainQuestStatus.COMPLETED, result.status)
        assertTrue(result.wasSuccessful)
    }

    @Test
    fun `task update completion requires at least one completed task list`() {
        assertEquals(
            DomainQuestStatus.ACTIVE,
            QuestCompletionPolicy.resolveAfterTaskUpdate(taskCount = 0, completedTaskCount = 0)
        )
        assertEquals(
            DomainQuestStatus.ACTIVE,
            QuestCompletionPolicy.resolveAfterTaskUpdate(taskCount = 2, completedTaskCount = 1)
        )
        assertEquals(
            DomainQuestStatus.COMPLETED,
            QuestCompletionPolicy.resolveAfterTaskUpdate(taskCount = 2, completedTaskCount = 2)
        )
    }

    private fun quest(tasks: List<QuestTask>) = Quest(
        id = 1,
        title = "Main quest",
        type = DomainQuestType.MAIN,
        date = 0L,
        status = DomainQuestStatus.ACTIVE,
        tasks = tasks
    )

    private fun task(isCompleted: Boolean) = QuestTask(
        id = 1,
        questId = 1,
        name = "Task",
        isCompleted = isCompleted
    )
}
