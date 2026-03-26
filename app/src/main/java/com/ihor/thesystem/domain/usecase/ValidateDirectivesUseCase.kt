package com.ihor.thesystem.domain.usecase

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
    ): Result<List<WorkoutDirective>> {
        return runCatching {
            directives.map { directive ->
                // Шукаємо ліміти для конкретної вправи у матриці прогресії
                val matrixEntry = matrix.find { it.exerciseId.toString() == directive.exerciseId }

                if (matrixEntry != null) {
                    // Якщо вправа є в матриці, затискаємо вагу в її межах
                    directive.copy(
                        targetWeight = directive.targetWeight.coerceIn(
                            minimumValue = matrixEntry.startWeight.toDouble(),
                            maximumValue = matrixEntry.targetWeight.toDouble()
                        ),
                        targetReps = directive.targetReps.coerceIn(1, 30),
                        targetSets = directive.targetSets.coerceAtLeast(1)
                    )
                } else {
                    // Якщо вправи чомусь немає в матриці, ставимо базові ліміти
                    directive.copy(
                        targetReps = directive.targetReps.coerceIn(1, 30),
                        targetSets = directive.targetSets.coerceAtLeast(1)
                    )
                }
            }
        }
    }
}