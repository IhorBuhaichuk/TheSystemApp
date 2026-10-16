package com.ihor.thesystem.domain.repository

import com.ihor.thesystem.domain.model.WorkoutDraft
import kotlinx.coroutines.flow.Flow

interface WorkoutDraftRepository {
    fun observeActiveDraft(): Flow<WorkoutDraft?>
    suspend fun saveDraft(draft: WorkoutDraft)
    suspend fun discardDraft(draftId: String)
}

