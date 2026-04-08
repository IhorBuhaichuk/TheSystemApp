package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.AiArchitectRepository
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.feature.statistics.model.AnnualMatrixProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours

class FinalizeSessionUseCase @Inject constructor(
    private val analyticsRepository: WorkoutAnalyticsRepository,
    private val aiRepository: AiArchitectRepository,
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
            
            // 2. Отримати актуальну вагу гравця для парсингу "BW" нормативів
            val playerWeight = playerRepository.getLatestWeight().firstOrNull()?.toDouble() ?: 80.0

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
                val matrixEntry = matrix.find { it.exerciseId.toString() == exId }
                
                if (matrixEntry != null) {
                    // Використовуємо AnnualMatrixProvider для визначення рангу на основі РЕАЛЬНОЇ ваги
                    val newRank = AnnualMatrixProvider.getExerciseRank(
                        exerciseName = matrixEntry.exerciseName,
                        current1RM = maxWeight,
                        playerWeight = playerWeight
                    )
                    
                    if (newRank.value > matrixEntry.currentRank.value) {
                        progressionMatrixRepository.updateRank(matrixEntry.exerciseId, newRank)
                    }
                }
            }

            val finalTonnage = if (calculatedTonnage > 0) calculatedTonnage else session.totalTonnage

            // 5. Розрахунок відновлення
            val recoveryDuration = calculateRecovery(finalTonnage, isNightShift).getOrDefault(24.hours)
            val recoveryHours = recoveryDuration.inWholeHours.toDouble()

            // 6. Запит до AI
            val context = sets.joinToString("\n") { "- Вправа ${it.exerciseId}: ${it.weight}кг х ${it.reps}" }
            val prompt = "Швидкий аналіз для логу: $context. Поверни JSON з feedback_text та next_workout_targets."
            
            val report = try {
                val chatMsg = aiRepository.getChatResponse(prompt)
                
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
                        WorkoutDirective(it.exerciseId.toString(), it.weight.toDouble(), it.sets, it.reps)
                    },
                    recoveryWindowHours = recoveryHours,
                    isFallback = false
                )
            } catch (e: Exception) {
                generateFallbackReport(sets, matrix, recoveryHours)
            }

            // 7. Валідація директив
            val validatedDirectives = validateDirectives(report.nextWorkoutDirectives, matrix)
                .getOrDefault(report.nextWorkoutDirectives)

            // 8. Зберегти директиви
            analyticsRepository.saveDirectives(validatedDirectives)

            // 9. ФІНАЛЬНИЙ КРОК: Оновити Глобальний Ранг гравця на основі всіх нових даних
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
            val entry = matrix.find { it.exerciseId.toString() == set.exerciseId }
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
