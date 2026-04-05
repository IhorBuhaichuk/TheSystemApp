package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import com.ihor.thesystem.data.local.room.dao.WorkoutDao
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class GetLastWorkoutContextUseCase @Inject constructor(
    private val analyticsRepo: WorkoutAnalyticsRepository,
    private val workoutDao: WorkoutDao
) {
    suspend operator fun invoke(): String? {
        // Отримуємо останню сесію
        val lastSession = analyticsRepo.getAllLogs().firstOrNull()?.firstOrNull() ?: return null
        
        // Отримуємо всі вправи для мапінгу імен
        val allExercises = workoutDao.getAllExercisesSync().associateBy { it.id }
        
        val contextBuilder = StringBuilder()
        contextBuilder.append("Останнє тренування (Тоннаж: ${lastSession.session.totalTonnage}кг):\n")
        
        lastSession.sets.forEach { set ->
            val name = allExercises[set.exerciseId]?.name ?: "Вправа ${set.exerciseId}"
            contextBuilder.append("[ID: ${set.exerciseId}] $name: ${set.weight}кг х ${set.reps}\n")
        }
        
        return contextBuilder.toString().trim()
    }
}
