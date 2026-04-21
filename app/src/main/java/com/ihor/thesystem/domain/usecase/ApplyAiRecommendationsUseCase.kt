package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.*
import android.util.Log
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
        val now = System.currentTimeMillis()
        val twelveHoursMillis = 12 * 60 * 60 * 1000L

        // 2. Збір детального контексту для кожної вправи (Batch Context)
        val exercisesContext = exerciseIds.mapNotNull { id ->
            val entry = matrix.find { it.exerciseId == id } ?: return@mapNotNull null
            
            // Захист від занадто частих запитів (не частіше ніж раз на 12 годин для однієї вправи)
            if (now - entry.lastAnalyzedTimestamp < twelveHoursMillis) {
                Log.d("ApplyAiRecs", "Skip AI analysis for exercise $id: analyzed recently")
                return@mapNotNull null
            }

            val recentLogs = analyticsRepo.getRecentLogsForExercise(id)
            val annualGoals = AnnualMatrixProvider.getMatrix()
                .find { it.exercise.equals(entry.exerciseName, true) }?.targets?.joinToString(", ") ?: "немає"
            
            val todayLog = recentLogs.firstOrNull()
            
            """
            {
                "exercise_id": $id,
                "name": "${entry.exerciseName}",
                "annual_goals": "$annualGoals",
                "recent_history": [${recentLogs.joinToString { "{\"w\": ${it.weight}, \"r\": ${it.reps}, \"f\": \"${it.userFeedback?.replace("\"", "'") ?: ""}\"}" }}],
                "current_weight": ${entry.currentWeight},
                "user_feedback": "${todayLog?.userFeedback?.replace("\"", "'") ?: ""}"
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
            
            Проаналізуй кожну вправу. 
            Відповідь поверни СУВОРО у форматі JSON об'єкта наступної структури:
            {
              "feedback_text": "текст аналізу прогресу до 3 речень",
              "next_workout_targets": [
                {
                  "exercise_id": ID,
                  "nextWeight": 50.0,
                  "nextSets": 3,
                  "nextReps": "8-10",
                  "aiFeedback": "коментар до вправи"
                }
              ]
            }
            
            КРИТИЧНО: У тексті feedback_text та aiFeedback КАТЕГОРИЧНО ЗАБОРОНЕНО використовувати будь-які лапки (ні подвійні, ні одинарні) та символи переносу рядка (\\n).
        """.trimIndent()

        // 3. Виконання єдиного пакетного запиту до ШІ
        try {
            val response = aiRepository.getChatResponse(prompt)
            
            // Якщо текст відповіді містить ключову фразу помилки парсингу
            if (response.text == "Помилка генерації AI, спробуйте ще раз") {
                Log.e("ApplyAiRecs", "AI returned parsing error. Aborting database update.")
                return
            }

            // 4. Розпарсинг та оновлення бази даних для кожної вправи
            response.recommendations.forEach { rec ->
                try {
                    matrixRepo.updateTarget(
                        exerciseId = rec.exerciseId,
                        weight = rec.weight.toDouble(),
                        sets = rec.sets,
                        reps = rec.reps,
                        aiFeedback = rec.aiFeedback ?: response.aiFeedback
                    )
                } catch (e: Exception) {
                    Log.e("ApplyAiRecs", "Failed to update target for exercise ${rec.exerciseId}: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e("ApplyAiRecs", "Critical error in ApplyAiRecommendationsUseCase: ${e.message}")
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
