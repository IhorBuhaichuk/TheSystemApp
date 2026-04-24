package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import javax.inject.Inject

class UpdateMatrixGoalsUseCase @Inject constructor(
    private val matrixRepo: ProgressionMatrixRepository
) {
    suspend operator fun invoke(exerciseId: Int, startWeight: Float, targetWeight: Float) {
        matrixRepo.updateMatrixGoals(exerciseId, startWeight, targetWeight)
    }
}
