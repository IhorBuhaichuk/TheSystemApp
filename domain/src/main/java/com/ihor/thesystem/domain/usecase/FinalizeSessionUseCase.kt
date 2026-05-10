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
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours
import kotlin.time.DurationUnit

class FinalizeSessionUseCase @Inject constructor(
    private val analyticsRepository: WorkoutAnalyticsRepository,
    private val sendArchitectAnalysis: SendArchitectAnalysisUseCase,
    private val progressionMatrixRepository: ProgressionMatrixRepository,
    private val playerRepository: PlayerRepository,
    private val recalculateGlobalRank: RecalculateGlobalRankUseCase,
    private val calculateRecovery: CalculateRecoveryWindowUseCase,
    private val validateDirectives: ValidateDirectivesUseCase,
    private val transactionProvider: TransactionProvider,
    private val getWeightContext: GetPlayerWeightContextUseCase,
    private val getTrainingPhaseContext: GetTrainingPhaseContextUseCase,
    private val questRepository: QuestRepository,
    private val calculateProgressRank: CalculateProgressRankUseCase,
    private val clock: AppClock,
    private val logger: AppLogger
) {
    suspend operator fun invoke(
        session: WorkoutSession,
        sets: List<ExerciseSet>
    ): Result<AiArchitectReport, DomainError> = try {
        
        // 1. Гарантоване локальне збереження в БД
        val localData = try {
            transactionProvider.runInTransaction {
                val sessionId = analyticsRepository.saveFullSessionLog(session, sets)
                
                val weightContext = getWeightContext()
                val playerWeight = weightContext.currentWeight
                val weight6MonthsAgo = weightContext.weightSixMonthsAgo

                val matrix = progressionMatrixRepository.getAllEntries().first()
                updateExerciseRanks(sets, matrix)

                val calculatedTonnage = sets.filter { it.isCompleted && it.weight > TECHNICAL_LOAD_WEIGHT }
                    .sumOf { it.weight * it.reps }
                val finalTonnage = session.totalTonnage.takeIf { it > 0.0 } ?: calculatedTonnage
                val recoveryHours = calculateRecovery(finalTonnage).toDouble(DurationUnit.HOURS)

                recalculateGlobalRank()
                completeWorkoutQuestIfPossible(session.questId, sets)
                
                LocalSessionData(
                    playerWeight = playerWeight,
                    weight6MonthsAgo = weight6MonthsAgo,
                    matrix = matrix,
                    recoveryHours = recoveryHours
                )
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            logger.e(e, "Local session saving failed")
            return Result.Error(DataError.Local.SQLITE_EXCEPTION)
        }

        // 2. Асинхронний запит до AiArchitectRepository та оновлення матриці
        val exerciseContexts = generateAiPrompt(
            sessionTimestamp = session.timestamp,
            sets = sets,
            matrix = localData.matrix,
            playerWeight = localData.playerWeight,
            weight6MonthsAgo = localData.weight6MonthsAgo
        )
        
        val report = try {
            val chatMsg = sendArchitectAnalysis(exerciseContexts)
            if (chatMsg.recommendations.isEmpty()) {
                throw IllegalStateException("AI architect returned no actionable workout directives.")
            }
            
            // Оновлення цілей у матриці на основі AI-аналізу
            chatMsg.recommendations.forEach { rec ->
                progressionMatrixRepository.updateTarget(
                    exerciseId = rec.exerciseId,
                    weight = rec.weight.toDouble(),
                    sets = rec.sets,
                    reps = rec.reps,
                    aiFeedback = rec.aiFeedback ?: chatMsg.aiFeedback,
                    timestamp = clock.now()
                )
            }

            AiArchitectReport(
                architectFeedback = chatMsg.text,
                currentStageStatus = "[ LOGGED ]",
                completedExercises = sets.map { it.exerciseId }.distinct(),
                pendingExercises = emptyList(),
                nextWorkoutDirectives = chatMsg.recommendations.map { 
                    WorkoutDirective(it.exerciseId, it.weight.toDouble(), it.sets, it.reps)
                },
                recoveryWindowHours = localData.recoveryHours,
                isFallback = false
            )
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            logger.e(e, "Architect analysis failed")
            generateFallbackReport(sets, localData.matrix, localData.recoveryHours)
        }

        // 3. Валідація та збереження фінальних директив
        val validatedDirectives = validateDirectives(report.nextWorkoutDirectives, localData.matrix)
            .getOrDefault(report.nextWorkoutDirectives)

        analyticsRepository.saveDirectives(validatedDirectives)

        Result.Success(
            report.copy(
                nextWorkoutDirectives = validatedDirectives,
                recoveryWindowHours = localData.recoveryHours
            )
        )
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        logger.e(e, "Unexpected error in FinalizeSessionUseCase")
        Result.Error(DataError.Local.UNKNOWN)
    }

    private data class LocalSessionData(
        val playerWeight: Double?,
        val weight6MonthsAgo: Float?,
        val matrix: List<ProgressionMatrixEntry>,
        val recoveryHours: Double
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
            val trackingMode = ExerciseTrackingModeResolver.resolve(name = exerciseName)
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
        val refreshedQuest = questRepository.getQuestById(activeMainQuest.id) ?: return
        if (refreshedQuest.status != DomainQuestStatus.COMPLETED) return

        questRepository.logQuestResult(
            questId = refreshedQuest.id,
            questType = refreshedQuest.type,
            wasSuccessful = true
        )

        val player = playerRepository.getPlayerSnapshot() ?: return
        playerRepository.updatePlayer(player.rewardWorkoutCompletion())
    }

    private fun generateFallbackReport(
        sets: List<ExerciseSet>,
        matrix: List<ProgressionMatrixEntry>,
        recoveryHours: Double
    ): AiArchitectReport {
        val matrixMap = matrix.associateBy { it.exerciseId }
        val fallbackDirectives = sets.groupBy { it.exerciseId }.map { (exId, exerciseSets) ->
            val entry = matrixMap[exId]
            val lastSet = exerciseSets.lastOrNull { it.isCompleted } ?: exerciseSets.last()
            
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
            completedExercises = sets.map { it.exerciseId }.distinct(),
            pendingExercises = emptyList(),
            nextWorkoutDirectives = fallbackDirectives,
            recoveryWindowHours = recoveryHours,
            isFallback = true
        )
    }
}

private const val TECHNICAL_LOAD_WEIGHT = 1.0

private fun ProgressionMatrixEntry.annualGoalSummary(): String? {
    val parsedPlan = AnnualProgressionPlanNoteParser.parse(targetWeightNote)
    if (parsedPlan != null) {
        return parsedPlan.monthlyTargets.joinToString(", ") { target ->
            "M${target.monthIndex}: ${target.weight}кг"
        }
    }
    return targetWeight.takeIf { it > 0f }?.let { "Ціль: ${it}кг" }
}
