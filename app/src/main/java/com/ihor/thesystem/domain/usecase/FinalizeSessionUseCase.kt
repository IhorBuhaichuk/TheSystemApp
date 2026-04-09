package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import java.util.Calendar
import kotlin.time.Duration.Companion.hours

class FinalizeSessionUseCase @Inject constructor(
    private val analyticsRepository: WorkoutAnalyticsRepository,
    private val sendChatMessageUseCase: SendChatMessageUseCase,
    private val progressionMatrixRepository: ProgressionMatrixRepository,
    private val playerRepository: PlayerRepository,
    private val recalculateGlobalRank: RecalculateGlobalRankUseCase,
    private val calculateRecovery: CalculateRecoveryWindowUseCase,
    private val validateDirectives: ValidateDirectivesUseCase
) {
    suspend operator fun invoke(
        session: WorkoutSession,
        sets: List<ExerciseSet>,
        isNightShift: Boolean
    ): Result<AiArchitectReport> {
        return runCatching {
            // 1. Зберегти сесію та сети
            val sessionId = analyticsRepository.saveSessionWithSets(session, sets)
            
            // 2. Отримати актуальну вагу гравця та вагу 6 місяців тому
            val playerWeight = playerRepository.getLatestWeight().firstOrNull()?.toDouble() ?: 80.0
            
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, -6)
            val timestampSixMonthsAgo = calendar.timeInMillis
            val weight6MonthsAgo = playerRepository.getWeightAtOrBefore(timestampSixMonthsAgo) ?: playerWeight.toFloat()

            // 3. Отримати матрицю прогресії (одноразовий запит)
            val matrix = try {
                progressionMatrixRepository.getAllEntries().first()
            } catch (e: Exception) {
                emptyList<ProgressionMatrixEntry>()
            }

            // 4. Розрахувати тоннаж та оновити ранги вправ на основі нормативів
            val calculatedTonnage = sets.filter { it.isCompleted }.sumOf { it.weight * it.reps }
            
            sets.filter { it.isCompleted }.groupBy { it.exerciseId }.forEach { (exId, exerciseSets) ->
                val maxWeight = exerciseSets.maxOf { it.weight }
                val matrixEntry = matrix.find { it.exerciseId == exId }
                
                if (matrixEntry != null) {
                    val newRank = AnnualMatrixProvider.getExerciseRankById(
                        exerciseId = matrixEntry.exerciseId,
                        current1RM = maxWeight,
                        playerWeight = playerWeight
                    )
                    
                    if (newRank.weight > matrixEntry.currentRank.weight) {
                        progressionMatrixRepository.updateRank(matrixEntry.exerciseId, newRank)
                    }
                }
            }

            val finalTonnage = if (calculatedTonnage > 0) calculatedTonnage else session.totalTonnage

            // 5. Розрахунок відновлення
            val recoveryDuration = calculateRecovery(finalTonnage, isNightShift).getOrDefault(24.hours)
            val recoveryHours = recoveryDuration.inWholeHours.toDouble()

            // 6. Формування контексту для AI
            val exerciseContexts = sets.filter { it.isCompleted }.groupBy { it.exerciseId }.map { (exId, exerciseSets) ->
                val matrixEntry = matrix.find { it.exerciseId == exId }
                val recentLogs = analyticsRepository.getRecentLogsForExercise(exId)
                val annualGoals = AnnualMatrixProvider.getMatrix().find { it.exerciseId == exId }?.targets?.joinToString(", ") ?: "немає"
                
                """
                Вправа: ${matrixEntry?.exerciseName ?: "ID $exId"}
                - Поточна вага тіла: $playerWeight кг (6 міс. тому: $weight6MonthsAgo кг)
                - Цілі Річної матриці (M0-M12): $annualGoals
                - Останні 10 тренувань: ${recentLogs.joinToString { "${it.weight}кг x ${it.reps}" }}
                - Сьогодні виконано: ${exerciseSets.joinToString { "${it.weight}кг x ${it.reps}" }}
                - Коментар користувача: ${exerciseSets.firstOrNull()?.userFeedback ?: "відсутній"}
                """.trimIndent()
            }.joinToString("\n\n")

            // 7. Виконання запиту через SendChatMessageUseCase для уніфікації промпту
            val report = try {
                val chatMsg = sendChatMessageUseCase(
                    sessionId = sessionId,
                    userMessage = "Аналіз завершеного тренування",
                    workoutContext = exerciseContexts
                )
                
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
                    recoveryWindowHours = recoveryHours,
                    isFallback = false
                )
            } catch (e: Exception) {
                generateFallbackReport(sets, matrix, recoveryHours)
            }

            // 8. Валідація директив
            val validatedDirectives = validateDirectives(report.nextWorkoutDirectives, matrix)
                .getOrDefault(report.nextWorkoutDirectives)

            // 9. Зберегти директиви
            analyticsRepository.saveDirectives(validatedDirectives)

            // 10. Оновити Глобальний Ранг
            recalculateGlobalRank()

            report.copy(
                nextWorkoutDirectives = validatedDirectives,
                recoveryWindowHours = recoveryHours
            )
        }
    }

    private fun generateFallbackReport(
        sets: List<ExerciseSet>,
        matrix: List<ProgressionMatrixEntry>,
        recoveryHours: Double
    ): AiArchitectReport {
        val fallbackDirectives = sets.map { set ->
            val entry = matrix.find { it.exerciseId == set.exerciseId }
            WorkoutDirective(
                exerciseId = set.exerciseId,
                targetWeight = entry?.startWeight?.toDouble() ?: set.weight,
                targetSets = 3,
                targetReps = "10"
            )
        }.distinctBy { it.exerciseId }

        return AiArchitectReport(
            architectFeedback = "ЗВ'ЯЗОК З AI ВТРАЧЕНО. Активовано резервний протокол.",
            currentStageStatus = "[ FALLBACK ]",
            completedExercises = sets.map { it.exerciseId }.distinct(),
            pendingExercises = emptyList(),
            nextWorkoutDirectives = fallbackDirectives,
            recoveryWindowHours = recoveryHours,
            isFallback = true
        )
    }
}
