package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.ReferenceMatrix
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import javax.inject.Inject

class GetExerciseReferenceUseCase @Inject constructor(
    private val progressionMatrixRepository: ProgressionMatrixRepository
) {
    suspend operator fun invoke(exerciseId: Int): ReferenceMatrix? =
        progressionMatrixRepository.getReferenceForExercise(exerciseId)

    suspend operator fun invoke(exerciseName: String): ReferenceMatrix? =
        progressionMatrixRepository.getReferenceForExercise(exerciseName)
}
