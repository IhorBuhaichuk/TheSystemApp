package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.data.local.room.dao.WorkoutDraftDao
import com.ihor.thesystem.data.local.room.entity.WorkoutDraftEntity
import com.ihor.thesystem.data.local.room.entity.WorkoutDraftSetEntity
import com.ihor.thesystem.data.local.room.relations.WorkoutDraftWithSets
import com.ihor.thesystem.domain.model.WorkoutDraft
import com.ihor.thesystem.domain.model.WorkoutDraftExercise
import com.ihor.thesystem.domain.model.WorkoutDraftSet
import com.ihor.thesystem.domain.repository.WorkoutDraftRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class WorkoutDraftRepositoryImpl @Inject constructor(
    private val dao: WorkoutDraftDao
) : WorkoutDraftRepository {

    override fun observeActiveDraft(): Flow<WorkoutDraft?> =
        dao.observeActiveDraft().map { it?.toDomain() }

    override suspend fun saveDraft(draft: WorkoutDraft) {
        dao.saveDraft(
            draft = draft.toEntity(),
            sets = draft.exercises.flatMapIndexed { exerciseOrder, exercise ->
                exercise.sets.mapIndexed { setOrder, set ->
                    WorkoutDraftSetEntity(
                        draftId = draft.id,
                        exerciseId = exercise.exerciseId,
                        exerciseOrder = exerciseOrder,
                        setOrder = setOrder,
                        inputId = set.inputId,
                        weightInput = set.weightInput,
                        repsInput = set.repsInput,
                        isCompleted = set.isCompleted
                    )
                }
            }
        )
    }

    override suspend fun discardDraft(draftId: String) {
        dao.discardDraft(draftId)
    }
}

private fun WorkoutDraft.toEntity() = WorkoutDraftEntity(
    draftId = id,
    dateEpochDay = dateEpochDay,
    scheduleId = scheduleId,
    cycleDay = cycleDay,
    workoutTemplateId = workoutTemplateId,
    workoutName = workoutName,
    questId = questId,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis
)

private fun WorkoutDraftWithSets.toDomain(): WorkoutDraft {
    val orderedExercises = sets
        .sortedWith(compareBy(WorkoutDraftSetEntity::exerciseOrder, WorkoutDraftSetEntity::setOrder))
        .groupBy { it.exerciseOrder }
        .values
        .map { exerciseSets ->
            WorkoutDraftExercise(
                exerciseId = exerciseSets.first().exerciseId,
                sets = exerciseSets.map { set ->
                    WorkoutDraftSet(
                        inputId = set.inputId,
                        weightInput = set.weightInput,
                        repsInput = set.repsInput,
                        isCompleted = set.isCompleted
                    )
                }
            )
        }

    return WorkoutDraft(
        id = draft.draftId,
        dateEpochDay = draft.dateEpochDay,
        scheduleId = draft.scheduleId,
        cycleDay = draft.cycleDay,
        workoutTemplateId = draft.workoutTemplateId,
        workoutName = draft.workoutName,
        questId = draft.questId,
        exercises = orderedExercises,
        createdAtMillis = draft.createdAtMillis,
        updatedAtMillis = draft.updatedAtMillis
    )
}

