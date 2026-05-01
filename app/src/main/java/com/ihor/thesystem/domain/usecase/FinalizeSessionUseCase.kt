package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.R
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.*
import com.ihor.thesystem.domain.util.AnnualProgressionPlanNoteParser
import com.ihor.thesystem.domain.util.sanitizeForPrompt
import com.ihor.thesystem.core.util.Result
import com.ihor.thesystem.core.util.*
import timber.log.Timber
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
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
    private val questRepository: QuestRepository,
    private val calculateProgressRank: CalculateProgressRankUseCase
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

                val calculatedTonnage = sets.filter { it.isCompleted }.sumOf { it.weight * it.reps }
                val finalTonnage = if (calculatedTonnage > 0) calculatedTonnage else session.totalTonnage
                val recoveryHours = calculateRecovery(finalTonnage).toDouble(DurationUnit.HOURS)

                recalculateGlobalRank()
                
                LocalSessionData(
                    playerWeight = playerWeight,
                    weight6MonthsAgo = weight6MonthsAgo,
                    matrix = matrix,
                    recoveryHours = recoveryHours
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Local session saving failed")
            return Result.Error(DataError.Local.SQLITE_EXCEPTION)
        }

        completeWorkoutQuestIfPossible(session.questId, sets)

        // 2. Асинхронний запит до AiArchitectRepository та оновлення матриці
        val exerciseContexts = generateAiPrompt(sets, localData.matrix, localData.playerWeight, localData.weight6MonthsAgo)
        
        val report = try {
            val chatMsg = sendArchitectAnalysis(exerciseContexts)
            
            // Оновлення цілей у матриці на основі AI-аналізу
            chatMsg.recommendations.forEach { rec ->
                progressionMatrixRepository.updateTarget(
                    exerciseId = rec.exerciseId,
                    weight = rec.weight.toDouble(),
                    sets = rec.sets,
                    reps = rec.reps
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
            Timber.e(e, "Architect analysis failed")
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
        Timber.e(e, "Unexpected error in FinalizeSessionUseCase")
        Result.Error(DataError.Local.UNKNOWN)
    }

    private data class LocalSessionData(
        val playerWeight: Double?,
        val weight6MonthsAgo: Float?,
        val matrix: List<ProgressionMatrixEntry>,
        val recoveryHours: Double
    )

    private suspend fun generateAiPrompt(
        sets: List<ExerciseSet>,
        matrix: List<ProgressionMatrixEntry>,
        playerWeight: Double?,
        weight6MonthsAgo: Float?
    ): String {
        val matrixMap = matrix.associateBy { it.exerciseId }
        
        return sets.filter { it.isCompleted }.groupBy { it.exerciseId }.map { (exId, exerciseSets) ->
            val matrixEntry = matrixMap[exId]
            val recentLogs = analyticsRepository.getRecentLogsForExercise(exId)
            val annualGoals = matrixEntry?.annualGoalSummary() ?: "немає"
            
            val sanitizedExerciseName = (matrixEntry?.exerciseName ?: "ID $exId").sanitizeForPrompt()
            val sanitizedFeedback = (exerciseSets.firstOrNull()?.userFeedback ?: "відсутній").sanitizeForPrompt()

            """
            Вправа: $sanitizedExerciseName
            - Поточна вага тіла: ${playerWeight?.let { "$it кг" } ?: "невідомо"} (6 міс. тому: ${weight6MonthsAgo?.let { "$it кг" } ?: "невідомо"})
            - Цілі Річної матриці (M0-M12): $annualGoals
            - Останні 10 тренувань: ${recentLogs.joinToString { "${it.weight}кг x ${it.reps}" }}
            - Сьогодні виконано: ${exerciseSets.joinToString { "${it.weight}кг x ${it.reps}" }}
            - Коментар користувача: $sanitizedFeedback
            """.trimIndent()
        }.joinToString("\n\n")
    }

    private suspend fun updateExerciseRanks(
        sets: List<ExerciseSet>,
        matrix: List<ProgressionMatrixEntry>
    ) {
        val matrixMap = matrix.associateBy { it.exerciseId }
        sets.filter { it.isCompleted }.groupBy { it.exerciseId }.forEach { (exId, exerciseSets) ->
            val bestWorkingWeight = exerciseSets
                .filter { it.isCompleted && it.reps > 0 }
                .maxOfOrNull { it.weight }
                ?: return@forEach

            val matrixEntry = matrixMap[exId] ?: return@forEach
            
            val newRank = calculateProgressRank(
                currentWeight = bestWorkingWeight,
                startWeight = matrixEntry.startWeight.toDouble(),
                targetWeight = matrixEntry.targetWeight.toDouble()
            ) ?: return@forEach
            
            if (newRank.weight > matrixEntry.currentRank.weight) {
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

        val player = playerRepository.getPlayer().firstOrNull() ?: return
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
            architectFeedback = UiText.StringResource(R.string.ai_fallback_activated),
            currentStageStatus = "[ FALLBACK ]",
            completedExercises = sets.map { it.exerciseId }.distinct(),
            pendingExercises = emptyList(),
            nextWorkoutDirectives = fallbackDirectives,
            recoveryWindowHours = recoveryHours,
            isFallback = true
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
