package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.AiArchitectRepository
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours

class FinalizeSessionUseCase @Inject constructor(
    private val analyticsRepository: WorkoutAnalyticsRepository,
    private val aiRepository: AiArchitectRepository,
    private val progressionMatrixRepository: ProgressionMatrixRepository,
    private val calculateRecovery: CalculateRecoveryWindowUseCase,
    private val validateDirectives: ValidateDirectivesUseCase
) {
    /**
     * @param session Дані сесії.
     * @param sets Список виконаних підходів.
     * @param isNightShift Чи була зміна нічною (для розрахунку відновлення ЦНС).
     */
    suspend operator fun invoke(
        session: WorkoutSession,
        sets: List<ExerciseSet>,
        isNightShift: Boolean
    ): Result<AiArchitectReport> {
        return runCatching {
            // 1. Зберегти сесію та сети
            val sessionId = analyticsRepository.saveSessionWithSets(session, sets)
            val currentSession = session.copy(sessionId = sessionId)

            // 2. Отримати матрицю прогресії
            val matrix = try {
                progressionMatrixRepository.getAllEntries().first()
            } catch (e: Exception) {
                emptyList<ProgressionMatrixEntry>()
            }

            // 3. Розрахувати тоннаж
            val calculatedTonnage = sets.filter { it.isCompleted }.sumOf { it.weight * it.reps }
            val finalTonnage = if (calculatedTonnage > 0) calculatedTonnage else session.totalTonnage

            // 4. Розрахунок відновлення
            val recoveryDuration = calculateRecovery(finalTonnage, isNightShift).getOrDefault(24.hours)
            val recoveryHours = recoveryDuration.inWholeHours.toDouble()

            // 5. Запит до AI (через новий метод чату для сумісності)
            val context = sets.joinToString("\n") { "- Вправа ${it.exerciseId}: ${it.weight}кг х ${it.reps}" }
            val prompt = "Швидкий аналіз для логу: $context. Поверни JSON з feedback_text та next_workout_targets."
            
            val report = try {
                val chatMsg = aiRepository.getChatResponse(prompt)
                AiArchitectReport(
                    architectFeedback = chatMsg.text,
                    currentStageStatus = "[ LOGGED ]",
                    completedExercises = sets.map { it.exerciseId }.distinct(),
                    pendingExercises = emptyList(),
                    nextWorkoutDirectives = chatMsg.recommendations.map { 
                        WorkoutDirective(it.exerciseId.toString(), it.weight.toDouble(), 3, it.reps)
                    },
                    recoveryWindowHours = recoveryHours,
                    isFallback = false
                )
            } catch (e: Exception) {
                generateFallbackReport(sets, matrix, recoveryHours)
            }

            // 6. Валідація директив
            val validatedDirectives = validateDirectives(report.nextWorkoutDirectives, matrix)
                .getOrDefault(report.nextWorkoutDirectives)

            // 7. Зберегти директиви
            analyticsRepository.saveDirectives(validatedDirectives)

            // 8. Повернути фінальний звіт
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
                targetReps = 10
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
