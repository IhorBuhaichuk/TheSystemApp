package com.ihor.thesystem.domain.model

data class QuestCompletionResult(
    val status: DomainQuestStatus,
    val wasSuccessful: Boolean
)

object QuestCompletionPolicy {

    fun resolveForDayFinalization(
        quest: Quest,
        forceComplete: Boolean = false
    ): QuestCompletionResult {
        val hasTasks = quest.tasks.isNotEmpty()
        val allTasksCompleted = hasTasks && quest.tasks.all { it.isCompleted }
        val wasSuccessful = if (!hasTasks) true else allTasksCompleted || forceComplete

        return QuestCompletionResult(
            status = if (wasSuccessful) DomainQuestStatus.COMPLETED else DomainQuestStatus.FAILED,
            wasSuccessful = wasSuccessful
        )
    }

    fun resolveAfterTaskUpdate(
        taskCount: Int,
        completedTaskCount: Int
    ): DomainQuestStatus {
        return if (taskCount > 0 && completedTaskCount == taskCount) {
            DomainQuestStatus.COMPLETED
        } else {
            DomainQuestStatus.ACTIVE
        }
    }

    fun isSuccessful(quest: Quest): Boolean = quest.status == DomainQuestStatus.COMPLETED
}
