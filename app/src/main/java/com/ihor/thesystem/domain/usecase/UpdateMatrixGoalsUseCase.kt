package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import javax.inject.Inject

class UpdateMatrixGoalsUseCase @Inject constructor(
    private val matrixRepo: ProgressionMatrixRepository,
    private val calculateAttributes: CalculateAttributesUseCase
) {
    suspend operator fun invoke(exerciseId: Int, startWeight: Float, targetWeight: Float) {
        // 1. Зберігаємо стартову та цільову вагу
        matrixRepo.updateMatrixGoals(exerciseId, startWeight, targetWeight)
        
        // 2. Одразу перераховуємо характеристики персонажа (радар) для оновленої вправи
        calculateAttributes(exerciseId)
    }
}
