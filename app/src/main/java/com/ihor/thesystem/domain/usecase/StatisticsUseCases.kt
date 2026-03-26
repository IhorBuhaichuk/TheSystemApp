package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProgressionMatrixUseCase @Inject constructor(
    private val repo: ProgressionMatrixRepository
) {
    operator fun invoke(): Flow<List<ProgressionMatrixEntry>> = repo.getAllEntries()
}

class UpdateExerciseWeightUseCase @Inject constructor(
    private val repo: ProgressionMatrixRepository
) {
    suspend operator fun invoke(exerciseId: Int, newWeight: Float) {
        if (newWeight > 0f) repo.updateCurrentWeight(exerciseId, newWeight)
    }
}