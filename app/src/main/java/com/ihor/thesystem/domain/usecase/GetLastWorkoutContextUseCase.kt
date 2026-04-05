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
        // 1. Отримуємо список останніх сесій (за замовчуванням Room повертає Flow)
        val allLogs = analyticsRepo.getAllLogs().firstOrNull() ?: return null
        val mostRecent = allLogs.firstOrNull() ?: return null
        
        // 2. Отримуємо ВСІ сесії за цю ж календарну дату.
        // Це необхідно, тому що кожна вправа зараз зберігається як окрема сесія у базі.
        val sameDaySessions = analyticsRepo.getSessionsByDate(mostRecent.session.timestamp).firstOrNull() ?: emptyList()
        if (sameDaySessions.isEmpty()) return null
        
        // 3. Отримуємо всі вправи для мапінгу імен
        val allExercises = workoutDao.getAllExercisesSync().associateBy { it.id }
        
        val contextBuilder = StringBuilder()
        val totalDayTonnage = sameDaySessions.sumOf { it.session.totalTonnage }
        
        contextBuilder.append("Результати останнього тренувального дня (Тоннаж: ${totalDayTonnage}кг):\n")
        
        // 4. Збираємо дані про всі вправи та їх підходи, виконані протягом дня
        // Сортуємо за часом виконання, щоб зберегти послідовність
        sameDaySessions.sortedBy { it.session.timestamp }.forEach { sessionWithSets ->
            sessionWithSets.sets.forEach { set ->
                val name = allExercises[set.exerciseId]?.name ?: "Вправа ${set.exerciseId}"
                contextBuilder.append("[ID: ${set.exerciseId}] $name: ${set.weight}кг х ${set.reps}\n")
            }
        }
        
        return contextBuilder.toString().trim()
    }
}
