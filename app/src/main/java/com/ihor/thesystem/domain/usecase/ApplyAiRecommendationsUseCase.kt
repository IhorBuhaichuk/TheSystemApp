package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.*
import com.ihor.thesystem.feature.statistics.model.AnnualMatrixProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar
import javax.inject.Inject

class ApplyAiRecommendationsUseCase @Inject constructor(
    private val matrixRepo: ProgressionMatrixRepository,
    private val playerRepo: PlayerRepository,
    private val analyticsRepo: WorkoutAnalyticsRepository,
    private val aiRepository: AiArchitectRepository
) {
    /**
     * Виконує пакетний аналіз для списку вправ та оновлює матрицю прогресії.
     * Використовуємо @JvmName для уникнення конфлікту JVM-сигнатур після erasure.
     */
    @JvmName("invokeBatch")
    suspend operator fun invoke(exerciseIds: List<Int>) {
        if (exerciseIds.isEmpty()) return

        // 1. Збір загальних даних користувача
        val playerWeight = playerRepo.getLatestWeight().firstOrNull()?.toDouble() ?: 80.0
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -6)
        val weight6MonthsAgo = playerRepo.getWeightAtOrBefore(calendar.timeInMillis) ?: playerWeight.toFloat()
        
        val matrix = matrixRepo.getAllEntries().first()

        // 2. Збір детального контексту для кожної вправи (Batch Context)
        val exercisesContext = exerciseIds.mapNotNull { id ->
            val entry = matrix.find { it.exerciseId == id } ?: return@mapNotNull null
            val recentLogs = analyticsRepo.getRecentLogsForExercise(id)
            val annualGoals = AnnualMatrixProvider.getMatrix()
                .find { it.exercise.equals(entry.exerciseName, true) }?.targets?.joinToString(", ") ?: "немає"
            
            val todayLog = recentLogs.firstOrNull()
            
            """
            {
                "exercise_id": $id,
                "name": "${entry.exerciseName}",
                "annual_goals": "$annualGoals",
                "recent_history": [${recentLogs.joinToString { "{\"w\": ${it.weight}, \"r\": ${it.reps}, \"f\": \"${it.userFeedback ?: ""}\"}" }}],
                "current_weight": ${entry.currentWeight},
                "user_feedback": "${todayLog?.userFeedback ?: ""}"
            }
            """.trimIndent()
        }.joinToString(",\n")

        val prompt = """
            ПАКЕТНИЙ АНАЛІЗ ТРЕНУВАННЯ.
            Вага гравця: $playerWeight кг (6 міс. тому: $weight6MonthsAgo кг).
            
            Дані вправ:
            [
            $exercisesContext
            ]
            
            Проаналізуй кожну вправу. Для кожної надай:
            1. Нову вагу (nextWeight)
            2. Кількість підходів (nextSets)
            3. Діапазон повторень (nextReps, наприклад "8-10")
            4. Коротка оцінка aiFeedback (до 3 речень, БЕЗ подвійних лапок всередині тексту, використовуй одинарні).
            
            Відповідай СУВОРО масивом JSON об'єктів.
        """.trimIndent()

        // 3. Виконання єдиного пакетного запиту до ШІ
        try {
            val response = aiRepository.getChatResponse(prompt)
            
            // 4. Розпарсинг та оновлення бази даних для кожної вправи
            response.recommendations.forEach { rec ->
                matrixRepo.updateTarget(
                    exerciseId = rec.exerciseId,
                    weight = rec.weight.toDouble(),
                    sets = rec.sets,
                    reps = rec.reps,
                    aiFeedback = rec.aiFeedback ?: response.aiFeedback
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Перевантажений метод для підтримки прямого оновлення (наприклад, з чату).
     */
    suspend operator fun invoke(recommendations: List<AiWorkoutRecommendation>) {
        recommendations.forEach { rec ->
            matrixRepo.updateTarget(
                exerciseId = rec.exerciseId,
                weight = rec.weight.toDouble(),
                sets = rec.sets,
                reps = rec.reps,
                aiFeedback = rec.aiFeedback
            )
        }
    }
}
