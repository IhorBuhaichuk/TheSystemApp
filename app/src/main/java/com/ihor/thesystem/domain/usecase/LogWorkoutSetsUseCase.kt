package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.core.util.AppClock
import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.domain.model.WorkoutSession
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.ProgressionMatrixRepository
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import com.ihor.thesystem.domain.model.ActiveSetInput
import kotlinx.coroutines.flow.firstOrNull
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

class LogWorkoutSetsUseCase @Inject constructor(
    private val playerRepo: PlayerRepository,
    private val matrixRepo: ProgressionMatrixRepository,
    private val analyticsRepo: WorkoutAnalyticsRepository,
    private val clock: AppClock
) {
    suspend operator fun invoke(
        exerciseId: Int,
        sets: List<ActiveSetInput>,
        timestamp: Long,
        userFeedback: String? = null
    ) {
        val validSets = sets.filter { 
            it.weight.toDoubleOrNull()?.let { w -> w > 0 } == true && 
            it.reps.toIntOrNull()?.let { r -> r > 0 } == true 
        }
        if (validSets.isEmpty()) return

        // 1. Отримуємо актуальний стан гравця для отримання реального cycleDay
        val player = playerRepo.getPlayer().firstOrNull()
        val currentCycleDay = player?.currentCycleDay ?: 0

        // Визначаємо межі дня на основі наданого timestamp та системного ZoneId
        val zoneId = ZoneId.systemDefault()
        val date = Instant.ofEpochMilli(timestamp).atZone(zoneId).toLocalDate()
        val startOfDay = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endOfDay = date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1

        val totalTonnage = validSets.sumOf { 
            (it.weight.toDoubleOrNull() ?: 0.0) * (it.reps.toIntOrNull() ?: 0)
        }

        // Перевіряємо, чи вже були записи цієї вправи в цей день
        val existingLogs = analyticsRepo.getLogsForExerciseOnDate(exerciseId, startOfDay, endOfDay)

        if (existingLogs.isNotEmpty()) {
            val sessionId = existingLogs.first().sessionId
            
            val sessionUpdate = WorkoutSession(
                sessionId = sessionId,
                questId = 0,
                timestamp = clock.now(),
                totalTonnage = totalTonnage,
                cycleDay = currentCycleDay,
                durationMinutes = 0
            )
            analyticsRepo.updateSessionLog(sessionUpdate)

            analyticsRepo.deleteSetsBySession(sessionId)
            val entities = validSets.map { input ->
                ExerciseSet(
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
            val sessionLog = WorkoutSession(
                questId = 0,
                timestamp = clock.now(),
                totalTonnage = totalTonnage,
                cycleDay = currentCycleDay,
                durationMinutes = 0
            )

            val entities = validSets.map { input ->
                ExerciseSet(
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
