package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.core.util.Result
import com.ihor.thesystem.domain.model.DataError
import com.ihor.thesystem.domain.model.DomainError
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import com.ihor.thesystem.domain.model.WorkoutDirective
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
            val validated = directives.map { directive ->
                // Шукаємо ліміти для конкретної вправи у матриці прогресії
                val matrixEntry = matrix.find { it.exerciseId == directive.exerciseId }

                if (matrixEntry != null) {
                    val safeMin = minOf(
                        matrixEntry.currentWeight.toDouble(),
                        matrixEntry.targetWeight.toDouble()
                    )
                    val safeMax = maxOf(
                        matrixEntry.currentWeight.toDouble(),
                        matrixEntry.targetWeight.toDouble()
                    )
                    // Якщо вправа є в матриці, затискаємо вагу в її межах
                    directive.copy(
                        targetWeight = directive.targetWeight.coerceIn(safeMin, safeMax),
                        // Оскільки targetReps тепер String (діапазони), ми не можемо напряму використати coerceIn.
                        // Для базової валідації намагаємося розпарсити або залишаємо як є.
                        targetReps = directive.targetReps,
                        targetSets = directive.targetSets.coerceAtLeast(1)
                    )
                } else {
                    // Якщо вправи чомусь немає в матриці, ставимо базові ліміти
                    directive.copy(
                        targetSets = directive.targetSets.coerceAtLeast(1)
                    )
                }
            }
            Result.Success(validated)
        } catch (e: Exception) {
            Result.Error(DataError.Local.SQLITE_EXCEPTION)
        }
    }
}
