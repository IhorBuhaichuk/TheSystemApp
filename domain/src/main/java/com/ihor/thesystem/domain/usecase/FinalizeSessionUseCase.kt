package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.util.AppClock
import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.*
import com.ihor.thesystem.domain.util.AnnualProgressionPlanNoteParser
import com.ihor.thesystem.domain.util.sanitizeForPrompt
import com.ihor.thesystem.domain.util.Result
import com.ihor.thesystem.domain.util.AppLogger
import com.ihor.thesystem.domain.util.getOrDefault
import com.ihor.thesystem.domain.model.ExerciseTrackingModeResolver
import com.ihor.thesystem.domain.model.formatForTrackingMode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours
import kotlin.time.DurationUnit

class FinalizeSessionUseCase @Inject constructor(
    private val analyticsRepository: WorkoutAnalyticsRepository,
    private val sendArchitectAnalysis: SendArchitectAnalysisUseCase,
    private val progressionMatrixRepository: ProgressionMatrixRepository,
    private val recalculateGlobalRank: RecalculateGlobalRankUseCase,
    private val calculateRecovery: CalculateRecoveryWindowUseCase,
    private val validateDirectives: ValidateDirectivesUseCase,
    private val transactionProvider: TransactionProvider,
    private val getWeightContext: GetPlayerWeightContextUseCase,
    private val getTrainingPhaseContext: GetTrainingPhaseContextUseCase,
    private val questRepository: QuestRepository,
    private val completeQuest: CompleteQuestUseCase,
    private val calculateProgressRank: CalculateProgressRankUseCase,
    private val calculateWorkoutJudgment: CalculateWorkoutJudgmentUseCase,
    private val decideTodayWorkout: DecideTodayWorkoutUseCase,
    private val clock: AppClock,
    private val logger: AppLogger
) {
    suspend operator fun invoke(
        session: WorkoutSession,
        sets: List<ExerciseSet>,
        plannedRecommendations: List<SetRecommendation> = emptyList()
    ): Result<AiArchitectReport, DomainError> {
        return try {
        
        // 1. Гарантоване локальне збереження в БД
        val localData = try {
            transactionProvider.runInTransaction {
                val activeMainQuest = session.questId
                    .takeIf { it > 0 && it <= Int.MAX_VALUE }
                    ?.let { questRepository.getQuestById(it.toInt()) }
                    ?.takeIf { it.type == DomainQuestType.MAIN }
                val systemTemplateType = activeMainQuest?.systemTemplateType
                val sessionId = analyticsRepository.saveFullSessionLog(session, sets)
                
                val weightContext = getWeightContext()
                val playerWeight = weightContext.currentWeight
                val weight6MonthsAgo = weightContext.weightSixMonthsAgo

                val matrix = progressionMatrixRepository.getAllEntries().first()
                if (systemTemplateType == null) {
                    updateExerciseRanks(sets, matrix)
                }

                val calculatedTonnage = sets.filter { it.isCompleted && it.weight > TECHNICAL_LOAD_WEIGHT }
                    .sumOf { it.weight * it.reps }
                val finalTonnage = session.totalTonnage.takeIf { it > 0.0 } ?: calculatedTonnage
                val recoveryHours = calculateRecovery(finalTonnage).toDouble(DurationUnit.HOURS)

                if (systemTemplateType == null) {
                    recalculateGlobalRank()
                }
                completeWorkoutQuestIfPossible(session.questId, sets)
                
                LocalSessionData(
                    sessionId = sessionId,
                    playerWeight = playerWeight,
                    weight6MonthsAgo = weight6MonthsAgo,
                    matrix = matrix,
                    recoveryHours = recoveryHours,
                    systemTemplateType = systemTemplateType
                )
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            logger.e(e, "Local session saving failed")
            return Result.Error(DataError.Local.SQLITE_EXCEPTION)
        }

        // 2. Асинхронний запит до AiArchitectRepository та оновлення матриці
        val todayDecision = runCatching {
            decideTodayWorkout(session.localDate(clock))
        }.onFailure { error ->
            logger.w("Today decision unavailable for workout judgment: ${error.message}")
        }.getOrNull()
        val judgment = calculateWorkoutJudgment(
            plannedRecommendations = plannedRecommendations.ifEmpty {
                sets.toPlannedRecommendations(localData.matrix)
            },
            actualSets = sets,
            todayDecision = todayDecision
        )
        val completedExerciseIds = sets
            .filter { it.isCompleted }
            .map { it.exerciseId }
            .distinct()

        if (localData.systemTemplateType != null) {
            return Result.Success(
                AiArchitectReport(
                    architectFeedback = MessageText.DynamicString("${localData.systemTemplateType.questTitle} logged."),
                    currentStageStatus = "[ SYSTEM_PROTOCOL ]",
                    completedExercises = completedExerciseIds,
                    pendingExercises = emptyList(),
                    nextWorkoutDirectives = emptyList(),
                    recoveryWindowHours = localData.recoveryHours,
                    isFallback = true,
                    sessionId = localData.sessionId,
                    judgment = judgment
                )
            )
        }

        val exerciseContexts = generateAiPrompt(
            sessionTimestamp = session.timestamp,
            sets = sets,
            matrix = localData.matrix,
            playerWeight = localData.playerWeight,
            weight6MonthsAgo = localData.weight6MonthsAgo
        )
        val weightedExerciseIds = sets
            .filter { it.isCompleted && it.hasRealExternalLoad() }
            .map { it.exerciseId }
            .toSet()
        
        var aiFeedbackByExercise: Map<Int, String?> = emptyMap()
        var generalAiFeedback: String? = null
        val report = try {
            val chatMsg = sendArchitectAnalysis(exerciseContexts)
            val weightedRecommendations = chatMsg.recommendations
                .filter { it.exerciseId in weightedExerciseIds }
            if (weightedExerciseIds.isNotEmpty() && weightedRecommendations.isEmpty()) {
                throw IllegalStateException("AI architect returned no actionable weighted workout directives.")
            }
            aiFeedbackByExercise = weightedRecommendations.associate { it.exerciseId to it.aiFeedback }
            generalAiFeedback = chatMsg.aiFeedback
            
            // Оновлення цілей у матриці на основі AI-аналізу
            AiArchitectReport(
                architectFeedback = chatMsg.text,
                currentStageStatus = "[ LOGGED ]",
                completedExercises = completedExerciseIds,
                pendingExercises = emptyList(),
                nextWorkoutDirectives = weightedRecommendations.map {
                    WorkoutDirective(it.exerciseId, it.weight.toDouble(), it.sets, it.reps)
                },
                recoveryWindowHours = localData.recoveryHours,
                isFallback = false,
                sessionId = localData.sessionId,
                judgment = judgment
            )
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            logger.e(e, "Architect analysis failed")
            generateFallbackReport(
                sets = sets,
                matrix = localData.matrix,
                recoveryHours = localData.recoveryHours,
                sessionId = localData.sessionId,
                judgment = judgment
            )
        }

        // 3. Валідація та збереження фінальних директив
        val validationContext = SystemDecisionValidationContext(
            todayDecision = todayDecision,
            lastWorkoutFailed = judgment.performanceStatus == WorkoutPerformanceStatus.FAILED ||
                judgment.progressionDecision == WorkoutProgressionDecision.REDUCE ||
                judgment.progressionDecision == WorkoutProgressionDecision.DELOAD_RECOMMENDED
        )
        val validatedDirectives = when (
            val validation = validateDirectives(report.nextWorkoutDirectives, localData.matrix, validationContext)
        ) {
            is Result.Success -> {
                logDirectiveAdjustments(report.nextWorkoutDirectives, validation.data)
                validation.data.validatedDirectives
            }
            is Result.Error -> {
                logger.e(message = "Workout directives rejected by validation: ${validation.error.message}")
                emptyList()
            }
        }

        transactionProvider.runInTransaction {
            if (!report.isFallback) {
                validatedDirectives.forEach { directive ->
                    progressionMatrixRepository.updateTarget(
                        exerciseId = directive.exerciseId,
                        weight = directive.targetWeight,
                        sets = directive.targetSets,
                        reps = directive.targetReps,
                        aiFeedback = aiFeedbackByExercise[directive.exerciseId] ?: generalAiFeedback,
                        timestamp = clock.now()
                    )
                }
            }
            analyticsRepository.saveDirectives(validatedDirectives)
        }

        Result.Success(
            report.copy(
                nextWorkoutDirectives = validatedDirectives,
                recoveryWindowHours = localData.recoveryHours,
                judgment = judgment
            )
        )
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        logger.e(e, "Unexpected error in FinalizeSessionUseCase")
        Result.Error(DataError.Local.UNKNOWN)
    }
    }

    private data class LocalSessionData(
        val sessionId: Long,
        val playerWeight: Double?,
        val weight6MonthsAgo: Float?,
        val matrix: List<ProgressionMatrixEntry>,
        val recoveryHours: Double,
        val systemTemplateType: SystemWorkoutTemplateType?
    )

    private suspend fun generateAiPrompt(
        sessionTimestamp: Long,
        sets: List<ExerciseSet>,
        matrix: List<ProgressionMatrixEntry>,
        playerWeight: Double?,
        weight6MonthsAgo: Float?
    ): String {
        val matrixMap = matrix.associateBy { it.exerciseId }
        val trainingPhaseContext = getTrainingPhaseContext(referenceTimestamp = sessionTimestamp)
        
        val exerciseContexts = sets.filter { it.isCompleted }.groupBy { it.exerciseId }.map { (exId, exerciseSets) ->
            val matrixEntry = matrixMap[exId]
            val recentLogs = analyticsRepository.getRecentLogsForExercise(exId)
            val annualGoals = matrixEntry?.annualGoalSummary() ?: "немає"
            
            val exerciseName = matrixEntry?.exerciseName ?: "ID $exId"
            val trackingMode = resolveTrackingModeForLoggedSets(exerciseName, exerciseSets)
            val metricPolicy = if (exerciseSets.any { it.hasRealExternalLoad() }) {
                "зовнішня вага у кг; можна додавати вправу до next_workout_targets"
            } else {
                "без зовнішньої ваги; НЕ додавай цю вправу до next_workout_targets і НЕ пропонуй кг"
            }
            val sanitizedExerciseName = exerciseName.sanitizeForPrompt()
            val sanitizedFeedback = (exerciseSets.firstOrNull()?.userFeedback ?: "відсутній").sanitizeForPrompt()
            val recentLogsText = recentLogs.joinToString { it.formatForTrackingMode(trackingMode) }
            val completedSetsText = exerciseSets.joinToString { it.formatForTrackingMode(trackingMode) }

            """
            Вправа: $sanitizedExerciseName
            - Поточна вага тіла: ${playerWeight?.let { "$it кг" } ?: "невідомо"} (6 міс. тому: ${weight6MonthsAgo?.let { "$it кг" } ?: "невідомо"})
            - Цілі Річної матриці (M0-M12): $annualGoals
            - Останні 10 тренувань: $recentLogsText
            - Сьогодні виконано: $completedSetsText
            - Тип метрики: $metricPolicy
            - Коментар користувача: $sanitizedFeedback
            """.trimIndent()
        }.joinToString("\n\n")

        return "${trainingPhaseContext.toPromptBlock()}\n\n$exerciseContexts"
    }

    private suspend fun updateExerciseRanks(
        sets: List<ExerciseSet>,
        matrix: List<ProgressionMatrixEntry>
    ) {
        val matrixMap = matrix.associateBy { it.exerciseId }
        sets.filter { it.isCompleted }.groupBy { it.exerciseId }.forEach { (exId, exerciseSets) ->
            val bestWorkingWeight = exerciseSets
                .filter { it.isCompleted && it.reps > 0 && it.weight > TECHNICAL_LOAD_WEIGHT }
                .maxOfOrNull { it.weight }
                ?: return@forEach

            val matrixEntry = matrixMap[exId] ?: return@forEach
            
            val newRank = calculateProgressRank(
                currentWeight = bestWorkingWeight,
                startWeight = matrixEntry.startWeight.toDouble(),
                targetWeight = matrixEntry.targetWeight.toDouble()
            ) ?: return@forEach
            
            if (RankProgressionPolicy.shouldPromote(matrixEntry.currentRank, newRank)) {
                progressionMatrixRepository.updateRank(matrixEntry.exerciseId, newRank)
            }
        }
    }

    private suspend fun completeWorkoutQuestIfPossible(questId: Long, sets: List<ExerciseSet>) {
        val completedExerciseIds = sets
            .filter { it.isCompleted }
            .map { it.exerciseId }
            .toSet()
        if (completedExerciseIds.isEmpty()) return
        if (questId <= 0 || questId > Int.MAX_VALUE) return

        val activeMainQuest = questRepository.getQuestById(questId.toInt())
            ?.takeIf { it.type == DomainQuestType.MAIN && it.status == DomainQuestStatus.ACTIVE }
            ?: return

        questRepository.completeQuestTasksForExercises(activeMainQuest.id, completedExerciseIds)
        completeQuest(activeMainQuest.id, mode = QuestCompletionMode.TaskUpdate)
    }

    private fun generateFallbackReport(
        sets: List<ExerciseSet>,
        matrix: List<ProgressionMatrixEntry>,
        recoveryHours: Double,
        sessionId: Long,
        judgment: SystemWorkoutJudgment
    ): AiArchitectReport {
        val matrixMap = matrix.associateBy { it.exerciseId }
        val fallbackDirectives = sets
            .filter { it.isCompleted && it.hasRealExternalLoad() }
            .groupBy { it.exerciseId }
            .map { (exId, exerciseSets) ->
                val entry = matrixMap[exId]
                val lastSet = exerciseSets.last()

                val fallbackWeight = lastSet.weight.takeIf { it > 0 }
                    ?: ((entry?.targetWeight?.toDouble() ?: lastSet.weight) * 0.95)

                WorkoutDirective(
                    exerciseId = exId,
                    targetWeight = fallbackWeight,
                    targetSets = entry?.nextRecommendedSets ?: 3,
                    targetReps = entry?.nextRecommendedReps ?: "10"
                )
            }

        return AiArchitectReport(
            architectFeedback = MessageText.Resource(MessageTextKey.AI_FALLBACK_ACTIVATED),
            currentStageStatus = "[ FALLBACK ]",
            completedExercises = sets.filter { it.isCompleted }.map { it.exerciseId }.distinct(),
            pendingExercises = emptyList(),
            nextWorkoutDirectives = fallbackDirectives,
            recoveryWindowHours = recoveryHours,
            isFallback = true,
            sessionId = sessionId,
            judgment = judgment
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
                    logger.w("Workout directive rejected for exercise ${raw.exerciseId}: missing validation audit")
                audit.status == DirectiveValidationStatus.REJECTED ->
                    logger.w("Workout directive rejected for exercise ${raw.exerciseId}: ${audit.reason}")
                audit.status == DirectiveValidationStatus.CLAMPED ->
                    logger.w("Workout directive clamped for exercise ${raw.exerciseId}: ${audit.original} -> ${audit.validated}. ${audit.reason}")
            }
        }
    }

    private fun resolveTrackingModeForLoggedSets(
        exerciseName: String,
        sets: List<ExerciseSet>
    ): ExerciseTrackingMode {
        val resolved = ExerciseTrackingModeResolver.resolve(name = exerciseName)
        return if (sets.none { it.hasRealExternalLoad() } && resolved == ExerciseTrackingMode.WEIGHT_REPS) {
            ExerciseTrackingMode.BODYWEIGHT_REPS
        } else {
            resolved
        }
    }

    private fun ExerciseSet.hasRealExternalLoad(): Boolean =
        weight > TECHNICAL_LOAD_WEIGHT

    private fun List<ExerciseSet>.toPlannedRecommendations(
        matrix: List<ProgressionMatrixEntry>
    ): List<SetRecommendation> {
        val matrixByExercise = matrix.associateBy { it.exerciseId }
        return groupBy { it.exerciseId }.map { (exerciseId, exerciseSets) ->
            val entry = matrixByExercise[exerciseId]
            SetRecommendation(
                weight = entry?.nextRecommendedWeight
                    ?: entry?.currentWeight?.toDouble()
                    ?: exerciseSets.maxOfOrNull { it.weight }
                    ?: 0.0,
                reps = entry?.nextRecommendedReps.toTargetReps()
                    ?: exerciseSets.maxOfOrNull { it.reps }
                    ?: DEFAULT_JUDGMENT_REPS,
                sets = entry?.nextRecommendedSets
                    ?: exerciseSets.count { it.isCompleted }.coerceAtLeast(DEFAULT_JUDGMENT_SETS),
                isProgression = false,
                exerciseId = exerciseId
            )
        }
    }
}

private const val TECHNICAL_LOAD_WEIGHT = 1.0
private const val DEFAULT_JUDGMENT_SETS = 3
private const val DEFAULT_JUDGMENT_REPS = 10

private fun WorkoutSession.localDate(clock: AppClock) =
    Instant.ofEpochMilli(timestamp)
        .atZone(clock.zoneId())
        .toLocalDate()

private fun String?.toTargetReps(): Int? =
    this
        ?.split("-", " ")
        ?.firstNotNullOfOrNull { token -> token.trim().toIntOrNull() }

private fun ProgressionMatrixEntry.annualGoalSummary(): String? {
    val parsedPlan = AnnualProgressionPlanNoteParser.parse(targetWeightNote)
    if (parsedPlan != null) {
        return parsedPlan.monthlyTargets.joinToString(", ") { target ->
            "M${target.monthIndex}: ${target.weight}кг"
        }
    }
    return targetWeight.takeIf { it > 0f }?.let { "Ціль: ${it}кг" }
}
