package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.AiArchitectReport
import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.domain.repository.ProgressionMatrixEntry
import com.ihor.thesystem.domain.model.WorkoutDirective
import com.ihor.thesystem.domain.model.WorkoutSession
import com.ihor.thesystem.domain.repository.AiArchitectRepository
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
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
            // 1. Зберегти сесію та сети (Отримуємо ID збереженої сесії)
            val sessionId = analyticsRepository.saveSessionWithSets(session, sets)
            val currentSession = session.copy(sessionId = sessionId)

            // 2. Отримати матрицю прогресії
            val matrix = try {
                progressionMatrixRepository.getAllEntries().first()
            } catch (e: Exception) {
                emptyList<ProgressionMatrixEntry>()
            }

            // 3. Розрахувати тоннаж (сума вага * повторення для завершених сетів)
            val calculatedTonnage = sets.filter { it.isCompleted }.sumOf { it.weight * it.reps }
            val finalTonnage = if (calculatedTonnage > 0) calculatedTonnage else session.totalTonnage

            // 4. Викликати CalculateRecoveryWindowUseCase
            val recoveryDuration = calculateRecovery(finalTonnage, isNightShift).getOrDefault(24.hours)
            val recoveryHours = recoveryDuration.inWholeHours.toDouble()

            // 5. Запит до AiArchitectRepository
            val report = try {
                aiRepository.analyzeSession(currentSession, sets)
            } catch (e: Exception) {
                // FALLBACK: Якщо AI недоступний або таймаут
                generateFallbackReport(sets, matrix, recoveryHours)
            }

            // 6. ValidateDirectivesUseCase (фільтр через матрицю)
            val validatedDirectives = validateDirectives(report.nextWorkoutDirectives, matrix)
                .getOrDefault(report.nextWorkoutDirectives)

            // 7. Зберегти директиви
            analyticsRepository.saveDirectives(validatedDirectives)

            // 8. Повернути фінальний звіт з відфільтрованими даними
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
        // Резервний розрахунок на основі поточної матриці прогресії
        val fallbackDirectives = sets.map { set ->
            val entry = matrix.find { it.exerciseId.toString() == set.exerciseId }
            WorkoutDirective(
                exerciseId = set.exerciseId,
                targetWeight = entry?.startWeight?.toDouble() ?: set.weight,
                targetSets = 3, // Базові значення для резервного плану
                targetReps = 10
            )
        }.distinctBy { it.exerciseId }

        return AiArchitectReport(
            architectFeedback = "ЗВ'ЯЗОК З AI ВТРАЧЕНО. Активовано резервний протокол: розрахунок наступного тренування виконано локально згідно з обмеженнями матриці.",
            currentStageStatus = "[ FALLBACK PROTOCOL ACTIVE ]",
            completedExercises = sets.map { it.exerciseId }.distinct(),
            pendingExercises = emptyList(),
            nextWorkoutDirectives = fallbackDirectives,
            recoveryWindowHours = recoveryHours,
            isFallback = true
        )
    }
}