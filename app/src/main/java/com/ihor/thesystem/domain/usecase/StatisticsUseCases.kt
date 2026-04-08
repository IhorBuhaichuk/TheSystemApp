package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import com.ihor.thesystem.feature.statistics.viewmodel.WorkoutSetInput
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

class SaveExerciseSetsUseCase @Inject constructor(
    private val repo: ProgressionMatrixRepository
) {
    suspend operator fun invoke(exerciseId: Int, sets: List<WorkoutSetInput>, timestamp: Long, userFeedback: String?) {
        repo.saveExerciseSetsWithDate(exerciseId, sets, timestamp, userFeedback)
    }
}
