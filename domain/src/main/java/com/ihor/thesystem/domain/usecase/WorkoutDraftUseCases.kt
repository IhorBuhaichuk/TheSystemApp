package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.WorkoutDraft
import com.ihor.thesystem.domain.repository.WorkoutDraftRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveWorkoutDraftUseCase @Inject constructor(
    private val repository: WorkoutDraftRepository
) {
    operator fun invoke(): Flow<WorkoutDraft?> = repository.observeActiveDraft()
}

class SaveWorkoutDraftUseCase @Inject constructor(
    private val repository: WorkoutDraftRepository
) {
    suspend operator fun invoke(draft: WorkoutDraft) = repository.saveDraft(draft)
}

class DiscardWorkoutDraftUseCase @Inject constructor(
    private val repository: WorkoutDraftRepository
) {
    suspend operator fun invoke(draftId: String) = repository.discardDraft(draftId)
}

