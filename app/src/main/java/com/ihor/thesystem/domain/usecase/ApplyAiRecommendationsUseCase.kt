package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.AiWorkoutRecommendation
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import javax.inject.Inject

class ApplyAiRecommendationsUseCase @Inject constructor(
    private val matrixRepo: ProgressionMatrixRepository
) {
    suspend operator fun invoke(recommendations: List<AiWorkoutRecommendation>) {
        recommendations.forEach { rec ->
            matrixRepo.updateTarget(
                exerciseId = rec.exerciseId,
                weight = rec.weight.toDouble(),
                sets = rec.sets,
                reps = rec.reps
            )
        }
    }
}
