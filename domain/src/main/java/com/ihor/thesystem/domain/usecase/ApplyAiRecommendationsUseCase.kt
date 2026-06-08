package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.util.AppClock
import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.*
import com.ihor.thesystem.domain.util.AppLogger
import com.ihor.thesystem.domain.util.AnnualProgressionPlanNoteParser
import com.ihor.thesystem.domain.util.Result
import com.ihor.thesystem.domain.util.sanitizeForPrompt
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import javax.inject.Inject

class ApplyAiRecommendationsUseCase @Inject constructor(
    private val matrixRepo: ProgressionMatrixRepository,
    private val playerRepo: PlayerRepository,
    private val analyticsRepo: WorkoutAnalyticsRepository,
    private val aiRepository: AiArchitectRepository,
    private val getWeightContext: GetPlayerWeightContextUseCase,
    private val getTrainingPhaseContext: GetTrainingPhaseContextUseCase,
    private val validateDirectives: ValidateDirectivesUseCase,
    private val decideTodayWorkout: DecideTodayWorkoutUseCase,
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
    suspend operator fun invoke(exerciseIds: List<Int>): AiRecommendationApplicationResult {
        if (exerciseIds.isEmpty()) return AiRecommendationApplicationResult.Empty

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
            if (!entry.usesExternalLoad()) {
                logger.d("Skip AI target update for exercise $id: exercise has no external load")
                return@mapNotNull null
            }
            
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
        if (exercisesContext.isEmpty()) return AiRecommendationApplicationResult.Empty

        val exercisesJson = Json.encodeToString(exercisesContext)

        val prompt = """
            ПАКЕТНИЙ АНАЛІЗ ТРЕНУВАННЯ.
            Вага гравця: ${playerWeight?.let { "$it кг" } ?: "невідомо"} (6 міс. тому: ${weight6MonthsAgo?.let { "$it кг" } ?: "невідомо"}).
            
            ${trainingPhaseContext.toPromptBlock()}
            
            Дані вправ:
            $exercisesJson

            Роль AI:
            - AI НЕ є джерелом істини і НЕ приймає фінальне рішення по плану.
            - Фінальне рішення завжди приймає deterministic System validator.
            - Твоя задача: пояснити дані і запропонувати обережну recommendation у JSON.
            - Не намагайся обійти readiness, recovery debt, deload/no-excuse/recovery decision або progression matrix.
            - Якщо рекомендація перевищить allowed step чи target cap, System її обмежить або відхилить.

            Проаналізуй кожну вправу спокійно і природно.
            Усі вправи в JSON є вправами із зовнішньою вагою. Не створюй kg-цілі для вправ, яких немає в JSON.
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
                return AiRecommendationApplicationResult.Empty
            }

            // 4. Розпарсинг та оновлення бази даних для кожної вправи
            val rawDirectives = response.recommendations.map { it.toDirective() }
            val validationContext = buildSystemDecisionContext(now)
            // Compatibility guard: validateDirectives(rawDirectives, matrix)
            val validationResult = when (val validation = validateDirectives(rawDirectives, matrix, validationContext)) {
                is Result.Success -> {
                    logDirectiveAdjustments(rawDirectives, validation.data)
                    validation.data
                }
                is Result.Error -> {
                    logger.e(message = "AI recommendations rejected by directive validation: ${validation.error.message}")
                    return AiRecommendationApplicationResult.Empty
                }
            }
            val validatedDirectives = validationResult.validatedDirectives
            val feedbackByExercise = response.recommendations.associateBy { it.exerciseId }

            transactionProvider.runInTransaction {
                validatedDirectives.forEach { directive ->
                    val source = feedbackByExercise[directive.exerciseId]
                    matrixRepo.updateTarget(
                        exerciseId = directive.exerciseId,
                        weight = directive.targetWeight,
                        sets = directive.targetSets,
                        reps = directive.targetReps,
                        aiFeedback = source?.aiFeedback ?: response.aiFeedback,
                        timestamp = now
                    )
                }
            }
            return AiRecommendationApplicationResult.from(validationResult)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            logger.e(e, "Critical error in ApplyAiRecommendationsUseCase")
            return AiRecommendationApplicationResult.Empty
        }
    }

    /**
     * Перевантажений метод для підтримки прямого оновлення (наприклад, з чату).
     */
    suspend operator fun invoke(recommendations: List<AiWorkoutRecommendation>): AiRecommendationApplicationResult {
        if (recommendations.isEmpty()) return AiRecommendationApplicationResult.Empty

        val timestamp = clock.now()
        val matrix = matrixRepo.getAllEntries().first()
        val rawDirectives = recommendations.map { it.toDirective() }
        val validationContext = buildSystemDecisionContext(timestamp)
        // Compatibility guard: validateDirectives(rawDirectives, matrix)
        val validationResult = when (val validation = validateDirectives(rawDirectives, matrix, validationContext)) {
            is Result.Success -> {
                logDirectiveAdjustments(rawDirectives, validation.data)
                validation.data
            }
            is Result.Error -> {
                logger.e(message = "Direct AI recommendations rejected by directive validation: ${validation.error.message}")
                return AiRecommendationApplicationResult.Empty
            }
        }
        val validatedDirectives = validationResult.validatedDirectives
        val feedbackByExercise = recommendations.associateBy { it.exerciseId }

        transactionProvider.runInTransaction {
            validatedDirectives.forEach { directive ->
                matrixRepo.updateTarget(
                    exerciseId = directive.exerciseId,
                    weight = directive.targetWeight,
                    sets = directive.targetSets,
                    reps = directive.targetReps,
                    aiFeedback = feedbackByExercise[directive.exerciseId]?.aiFeedback,
                    timestamp = timestamp
                )
            }
        }
        return AiRecommendationApplicationResult.from(validationResult)
    }

    private fun AiWorkoutRecommendation.toDirective(): WorkoutDirective =
        WorkoutDirective(
            exerciseId = exerciseId,
            targetWeight = weight.toDouble(),
            targetSets = sets,
            targetReps = reps
        )

    private suspend fun buildSystemDecisionContext(referenceTimestamp: Long): SystemDecisionValidationContext {
        val referenceDate = Instant.ofEpochMilli(referenceTimestamp)
            .atZone(clock.zoneId())
            .toLocalDate()
        val todayDecision = runCatching { decideTodayWorkout(referenceDate) }
            .onFailure { error -> logger.w("System decision unavailable for AI validation: ${error.message}") }
            .getOrNull()
        val lastWorkoutFailed = runCatching {
            analyticsRepo.getAllLogs().firstOrNull().orEmpty().latestWorkoutFailed()
        }.onFailure { error ->
            logger.w("Last workout failure context unavailable for AI validation: ${error.message}")
        }.getOrDefault(false)

        return SystemDecisionValidationContext(
            todayDecision = todayDecision,
            lastWorkoutFailed = lastWorkoutFailed
        )
    }

    private fun logDirectiveAdjustments(
        rawDirectives: List<WorkoutDirective>,
        validationResult: DirectiveValidationResult
    ) {
        val auditsByExercise = validationResult.audits.associateBy { it.exerciseId }
        rawDirectives.forEach { raw ->
            val audit = auditsByExercise[raw.exerciseId]
            when {
                audit == null ->
                    logger.w("AI recommendation rejected for exercise ${raw.exerciseId}: missing validation audit")
                audit.status == DirectiveValidationStatus.REJECTED ->
                    logger.w("AI recommendation rejected for exercise ${raw.exerciseId}: ${audit.reason}")
                audit.status == DirectiveValidationStatus.CLAMPED ->
                    logger.w("AI recommendation clamped for exercise ${raw.exerciseId}: ${audit.original} -> ${audit.validated}. ${audit.reason}")
            }
        }
    }

    private fun List<WorkoutLog>.latestWorkoutFailed(): Boolean {
        val latest = maxByOrNull { it.session.timestamp } ?: return false
        return latest.sets.any { set ->
            !set.isCompleted || set.userFeedback.isFailureFeedback()
        }
    }

    private fun String?.isFailureFeedback(): Boolean {
        val normalized = this?.lowercase().orEmpty()
        return FAILURE_FEEDBACK_KEYWORDS.any { keyword -> keyword in normalized }
    }

    private companion object {
        val FAILURE_FEEDBACK_KEYWORDS = listOf(
            "fail",
            "failed",
            "failure",
            "pain",
            "miss",
            "провал",
            "біль",
            "не виконав"
        )
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
