package com.ihor.thesystem.domain.model

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

    val systemTemplateType: SystemWorkoutTemplateType?
        get() = SystemWorkoutTemplateType.values().firstOrNull { template ->
            title.equals(template.questTitle, ignoreCase = true)
        }

    val isSystemWorkout: Boolean get() = systemTemplateType != null
}

data class QuestTask(
    val id: Int,
    val questId: Int,
    val name: String,
    val nameUk: String? = null,
    val isCompleted: Boolean,
    val exerciseId: Int? = null,
    val recommendedWeight: Double? = null,
    val recommendedSets: Int? = null,
    val recommendedReps: Int? = null
)
