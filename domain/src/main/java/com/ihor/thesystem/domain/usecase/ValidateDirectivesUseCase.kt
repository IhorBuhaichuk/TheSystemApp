package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.util.Result
import com.ihor.thesystem.domain.model.DataError
import com.ihor.thesystem.domain.model.DomainError
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import com.ihor.thesystem.domain.model.WorkoutDirective
import com.ihor.thesystem.domain.repository.usesExternalLoad
import javax.inject.Inject

class ValidateDirectivesUseCase @Inject constructor() {

    /**
     * Застосовує жорсткі обмеження (clamp) до рекомендацій AI на основі матриці прогресії.
     */
    operator fun invoke(
        directives: List<WorkoutDirective>,
        matrix: List<ProgressionMatrixEntry>
    ): Result<List<WorkoutDirective>, DomainError> {
        return try {
            val validated = directives.mapNotNull { directive ->
                val matrixEntry = matrix.find { it.exerciseId == directive.exerciseId }
                if (matrixEntry != null && !matrixEntry.usesExternalLoad()) {
                    return@mapNotNull null
                }

                // 1. Валідація ваги: якщо <= 0 і є запис у матриці — беремо поточну вагу з матриці
                val initialWeight = if (directive.targetWeight <= 0.0 && matrixEntry != null) {
                    matrixEntry.currentWeight.toDouble()
                } else {
                    directive.targetWeight
                }

                // Клампимо вагу в межах матриці, якщо вона там є
                val finalWeight = if (matrixEntry != null) {
                    val safeMin = minOf(matrixEntry.currentWeight.toDouble(), matrixEntry.targetWeight.toDouble())
                    val safeMax = maxOf(matrixEntry.currentWeight.toDouble(), matrixEntry.targetWeight.toDouble())
                    initialWeight.coerceIn(safeMin, safeMax)
                } else {
                    initialWeight.coerceAtLeast(0.0)
                }

                directive.copy(
                    targetWeight = finalWeight,
                    // 2. Валідація підходів: 1..10
                    targetSets = directive.targetSets.coerceIn(1, 10),
                    // 3. Валідація повторень (String/діапазон)
                    targetReps = validateReps(directive.targetReps)
                )
            }
            Result.Success(validated)
        } catch (e: Exception) {
            Result.Error(DataError.Local.SQLITE_EXCEPTION)
        }
    }

    /**
     * Валідує повторення. Підтримує одиночні значення та діапазони "min-max".
     */
    private fun validateReps(reps: String): String {
        return if (reps.contains("-")) {
            val parts = reps.split("-")
            val minRaw = parts.getOrNull(0)?.toIntOrNull() ?: 1
            val maxRaw = parts.getOrNull(1)?.toIntOrNull() ?: 8
            
            val min = minRaw.coerceIn(1, 30)
            val max = maxRaw.coerceIn(1, 30)
            
            // Гарантуємо коректний порядок у діапазоні
            val actualMin = minOf(min, max)
            val actualMax = maxOf(min, max)
            
            "$actualMin-$actualMax"
        } else {
            reps.toIntOrNull()?.coerceIn(1, 30)?.toString() ?: "8"
        }
    }

}
