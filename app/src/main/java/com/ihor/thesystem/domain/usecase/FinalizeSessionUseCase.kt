package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.R
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.core.util.runSuspendCatching
import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.*
import com.ihor.thesystem.domain.util.sanitizeForPrompt
import com.ihor.thesystem.core.util.*
import timber.log.Timber
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import java.util.Calendar
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
    private val annualMatrixRepository: AnnualMatrixRepository
) {
    suspend operator fun invoke(
        session: WorkoutSession,
        sets: List<ExerciseSet>
    ): kotlin.Result<AiArchitectReport> = runSuspendCatching {
        
        // 1. Гарантоване локальне збереження в БД
        val localData = transactionProvider.runInTransaction {
            val sessionId = analyticsRepository.saveSessionWithSets(session, sets)
            
            val playerWeight = playerRepository.getLatestWeight().firstOrNull()?.toDouble() ?: 80.0
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, -6)
            val weight6MonthsAgo = playerRepository.getWeightAtOrBefore(calendar.timeInMillis).getOrNull() ?: playerWeight.toFloat()

            val matrix = progressionMatrixRepository.getAllEntries().first()
            updateExerciseRanks(sets, matrix, playerWeight)

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

        report.copy(
            nextWorkoutDirectives = validatedDirectives,
            recoveryWindowHours = localData.recoveryHours
        )
    }

    private data class LocalSessionData(
        val playerWeight: Double,
        val weight6MonthsAgo: Float,
        val matrix: List<ProgressionMatrixEntry>,
        val recoveryHours: Double
    )

    private suspend fun generateAiPrompt(
        sets: List<ExerciseSet>,
        matrix: List<ProgressionMatrixEntry>,
        playerWeight: Double,
        weight6MonthsAgo: Float
    ): String {
        val matrixMap = matrix.associateBy { it.exerciseId }
        val annualMatrixMap = annualMatrixRepository.getMatrix().associateBy { it.exerciseId }
        
        return sets.filter { it.isCompleted }.groupBy { it.exerciseId }.map { (exId, exerciseSets) ->
            val matrixEntry = matrixMap[exId]
            val recentLogs = analyticsRepository.getRecentLogsForExercise(exId)
            val annualGoals = annualMatrixMap[exId]?.targets?.joinToString(", ") ?: "немає"
            
            val sanitizedExerciseName = (matrixEntry?.exerciseName ?: "ID $exId").sanitizeForPrompt()
            val sanitizedFeedback = (exerciseSets.firstOrNull()?.userFeedback ?: "відсутній").sanitizeForPrompt()

            """
            Вправа: $sanitizedExerciseName
            - Поточна вага тіла: $playerWeight кг (6 міс. тому: $weight6MonthsAgo кг)
            - Цілі Річної матриці (M0-M12): $annualGoals
            - Останні 10 тренувань: ${recentLogs.joinToString { "${it.weight}кг x ${it.reps}" }}
            - Сьогодні виконано: ${exerciseSets.joinToString { "${it.weight}кг x ${it.reps}" }}
            - Коментар користувача: $sanitizedFeedback
            """.trimIndent()
        }.joinToString("\n\n")
    }

    private suspend fun updateExerciseRanks(
        sets: List<ExerciseSet>,
        matrix: List<ProgressionMatrixEntry>,
        playerWeight: Double
    ) {
        val matrixMap = matrix.associateBy { it.exerciseId }
        sets.filter { it.isCompleted }.groupBy { it.exerciseId }.forEach { (exId, exerciseSets) ->
            val estimated1RM = exerciseSets
                .filter { it.isCompleted && it.reps > 0 }
                .maxOfOrNull { OneRepMaxCalculator.calculate(it.weight, it.reps) }
                ?: return@forEach

            val matrixEntry = matrixMap[exId] ?: return@forEach
            
            val newRank = annualMatrixRepository.getExerciseRankById(
                exerciseId = matrixEntry.exerciseId,
                current1RM = estimated1RM,
                playerWeight = playerWeight
            )
            
            if (newRank.weight > matrixEntry.currentRank.weight) {
                progressionMatrixRepository.updateRank(matrixEntry.exerciseId, newRank)
            }
        }
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
