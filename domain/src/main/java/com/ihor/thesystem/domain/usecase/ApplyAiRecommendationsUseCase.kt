package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.util.AppClock
import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.*
import com.ihor.thesystem.domain.util.AppLogger
import com.ihor.thesystem.domain.util.AnnualProgressionPlanNoteParser
import com.ihor.thesystem.domain.util.sanitizeForPrompt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class ApplyAiRecommendationsUseCase @Inject constructor(
    private val matrixRepo: ProgressionMatrixRepository,
    private val playerRepo: PlayerRepository,
    private val analyticsRepo: WorkoutAnalyticsRepository,
    private val aiRepository: AiArchitectRepository,
    private val getWeightContext: GetPlayerWeightContextUseCase,
    private val getTrainingPhaseContext: GetTrainingPhaseContextUseCase,
    private val transactionProvider: TransactionProvider,
    private val clock: AppClock,
    private val logger: AppLogger
) {

    @Serializable
    private data class ExercisePromptItem(
        val exercise_id: Int,
        val name: String,
        val annual_goals: String,
        val recent_history: List<HistoryItem>,
        val current_weight: Float,
        val user_feedback: String
    )

    @Serializable
    private data class HistoryItem(
        val w: Double,
        val r: Int,
        val f: String
    )
    /**
     * Виконує пакетний аналіз для списку вправ та оновлює матрицю прогресії.
     * Використовуємо @JvmName для уникнення конфлікту JVM-сигнатур після erasure.
     */
    @JvmName("invokeBatch")
    suspend operator fun invoke(exerciseIds: List<Int>) {
        if (exerciseIds.isEmpty()) return

        // 1. Збір загальних даних користувача через спільний UseCase
        val weightContext = getWeightContext()
        val trainingPhaseContext = getTrainingPhaseContext()
        val playerWeight = weightContext.currentWeight
        val weight6MonthsAgo = weightContext.weightSixMonthsAgo
        
        val matrix = matrixRepo.getAllEntries().first()
        val now = clock.now()
        val twelveHoursMillis = 12 * 60 * 60 * 1000L

        // 2. Збір детального контексту для кожної вправи (Batch Context)
        val exercisesContext = exerciseIds.mapNotNull { id ->
            val entry = matrix.find { it.exerciseId == id } ?: return@mapNotNull null
            
            // Захист від занадто частих запитів (не частіше ніж раз на 12 годин для однієї вправи)
            if (now - entry.lastAnalyzedTimestamp < twelveHoursMillis) {
                logger.d("Skip AI analysis for exercise $id: analyzed recently")
                return@mapNotNull null
            }

            val recentLogs = analyticsRepo.getRecentLogsForExercise(id)
            val annualGoals = entry.annualGoalSummary() ?: "немає"
            
            val todayLog = recentLogs.firstOrNull()
            
            ExercisePromptItem(
                exercise_id = id,
                name = entry.exerciseName.sanitizeForPrompt(),
                annual_goals = annualGoals.sanitizeForPrompt(),
                recent_history = recentLogs.map { 
                    HistoryItem(it.weight, it.reps, it.userFeedback?.sanitizeForPrompt() ?: "") 
                },
                current_weight = entry.currentWeight,
                user_feedback = todayLog?.userFeedback?.sanitizeForPrompt() ?: ""
            )
        }

        val exercisesJson = Json.encodeToString(exercisesContext)

        val prompt = """
            ПАКЕТНИЙ АНАЛІЗ ТРЕНУВАННЯ.
            Вага гравця: ${playerWeight?.let { "$it кг" } ?: "невідомо"} (6 міс. тому: ${weight6MonthsAgo?.let { "$it кг" } ?: "невідомо"}).
            
            ${trainingPhaseContext.toPromptBlock()}
            
            Дані вправ:
            $exercisesJson
            
            Проаналізуй кожну вправу спокійно і природно.
            Тон відповіді: як уважний тренер, без кіберпанку, пафосу, агресії, сорому і штучних метафор.
            Якщо триває фаза перших 14 днів, feedback_text та aiFeedback мають тільки хвалити і підтримувати гравця.
            Якщо річного графіка M0-M12 ще немає, не називай це проблемою і не проси користувача завантажити цілі.
            
            Відповідь поверни СУВОРО у форматі JSON об'єкта наступної структури:
            {
              "feedback_text": "2-3 короткі природні речення без заголовків і нумерації",
              "next_workout_targets": [
                {
                  "exercise_id": ID,
                  "nextWeight": 50.0,
                  "nextSets": 3,
                  "nextReps": "8-10",
                  "aiFeedback": "одне коротке природне речення до вправи"
                }
              ]
            }
            
            КРИТИЧНО: У тексті feedback_text та aiFeedback КАТЕГОРИЧНО ЗАБОРОНЕНО використовувати будь-які лапки (ні подвійні, ні одинарні) та символи переносу рядка (\\n).
        """.trimIndent()

        // 3. Виконання єдиного пакетного запиту до ШІ
        try {
            val response = aiRepository.getChatResponse(prompt)
            
            // Якщо текст відповіді містить ключову фразу помилки парсингу
            val responseText = when(val t = response.text) {
                is MessageText.DynamicString -> t.value
                is MessageText.Resource -> ""
            }
            if (responseText == "Помилка генерації AI, спробуйте ще раз" || 
                response.text is MessageText.Resource) {
                logger.e(message = "AI returned error or parsing failed. Aborting database update.")
                return
            }

            // 4. Розпарсинг та оновлення бази даних для кожної вправи
            transactionProvider.runInTransaction {
                response.recommendations.forEach { rec ->
                    matrixRepo.updateTarget(
                        exerciseId = rec.exerciseId,
                        weight = rec.weight.toDouble(),
                        sets = rec.sets,
                        reps = rec.reps,
                        aiFeedback = rec.aiFeedback ?: response.aiFeedback,
                        timestamp = now
                    )
                }
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            logger.e(e, "Critical error in ApplyAiRecommendationsUseCase")
        }
    }

    /**
     * Перевантажений метод для підтримки прямого оновлення (наприклад, з чату).
     */
    suspend operator fun invoke(recommendations: List<AiWorkoutRecommendation>) {
        if (recommendations.isEmpty()) return

        val timestamp = clock.now()
        transactionProvider.runInTransaction {
            recommendations.forEach { rec ->
                matrixRepo.updateTarget(
                    exerciseId = rec.exerciseId,
                    weight = rec.weight.toDouble(),
                    sets = rec.sets,
                    reps = rec.reps,
                    aiFeedback = rec.aiFeedback,
                    timestamp = timestamp
                )
            }
        }
    }
}

private fun ProgressionMatrixEntry.annualGoalSummary(): String? {
    val parsedPlan = AnnualProgressionPlanNoteParser.parse(targetWeightNote)
    if (parsedPlan != null) {
        return parsedPlan.monthlyTargets.joinToString(", ") { target ->
            "M${target.monthIndex}: ${target.weight}кг"
        }
    }
    return targetWeight.takeIf { it > 0f }?.let { "Ціль: ${it}кг" }
}
