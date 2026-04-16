package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.core.util.AppClock
import com.ihor.thesystem.data.local.room.entity.ExerciseSetLogEntity
import com.ihor.thesystem.data.local.room.entity.WorkoutSessionLogEntity
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import com.ihor.thesystem.feature.statistics.viewmodel.WorkoutSetInput
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class LogWorkoutSetsUseCase @Inject constructor(
    private val matrixRepo: ProgressionMatrixRepository,
    private val analyticsRepo: WorkoutAnalyticsRepository,
    private val clock: AppClock
) {
    suspend operator fun invoke(
        exerciseId: Int,
        sets: List<WorkoutSetInput>,
        timestamp: Long,
        userFeedback: String? = null
    ) {
        val validSets = sets.filter { it.weight.isNotEmpty() && it.reps.isNotEmpty() }
        if (validSets.isEmpty()) return

        // Визначаємо межі дня на основі наданого timestamp та системного ZoneId
        // (Для повної чистоти ZoneId також можна було б винести в AppClock, але поки що обмежимося цим)
        val zoneId = ZoneId.systemDefault()
        val date = Instant.ofEpochMilli(timestamp).atZone(zoneId).toLocalDate()
        val startOfDay = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1

        val totalTonnage = validSets.sumOf { 
            (it.weight.toDoubleOrNull() ?: 0.0) * (it.reps.toIntOrNull() ?: 0)
        }

        // Перевіряємо, чи вже був запис цієї вправи в цей день
        val existingSetLog = analyticsRepo.getLogForExerciseOnDate(exerciseId, startOfDay, endOfDay)

        if (existingSetLog != null) {
            val sessionId = existingSetLog.sessionId
            
            val sessionUpdate = WorkoutSessionLogEntity(
                sessionId = sessionId,
                questId = 0,
                timestamp = clock.now(),
                totalTonnage = totalTonnage,
                cycleDay = 0,
                durationMinutes = 0
            )
            analyticsRepo.updateSessionLog(sessionUpdate)

            analyticsRepo.deleteSetsBySession(sessionId)
            val entities = validSets.map { input ->
                ExerciseSetLogEntity(
                    sessionId = sessionId,
                    exerciseId = exerciseId,
                    weight = input.weight.toDoubleOrNull() ?: 0.0,
                    reps = input.reps.toIntOrNull() ?: 0,
                    isCompleted = true,
                    userFeedback = userFeedback
                )
            }
            analyticsRepo.saveSetLogs(entities)
        } else {
            val sessionLog = WorkoutSessionLogEntity(
                questId = 0,
                timestamp = clock.now(),
                totalTonnage = totalTonnage,
                cycleDay = 0,
                durationMinutes = 0
            )

            val entities = validSets.map { input ->
                ExerciseSetLogEntity(
                    sessionId = 0,
                    exerciseId = exerciseId,
                    weight = input.weight.toDoubleOrNull() ?: 0.0,
                    reps = input.reps.toIntOrNull() ?: 0,
                    isCompleted = true,
                    userFeedback = userFeedback
                )
            }
            analyticsRepo.saveFullSessionLog(sessionLog, entities)
        }
        
        // Оновлюємо поточну вагу в матриці
        val maxWeight = validSets.mapNotNull { it.weight.toFloatOrNull() }.maxOrNull()
        if (maxWeight != null) {
            matrixRepo.updateCurrentWeight(exerciseId, maxWeight)
        }
    }
}
