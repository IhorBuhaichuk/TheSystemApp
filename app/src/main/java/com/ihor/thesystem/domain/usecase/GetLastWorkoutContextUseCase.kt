package com.ihor.thesystem.domain.usecase

import android.content.Context
import com.ihor.thesystem.R
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class GetLastWorkoutContextUseCase @Inject constructor(
    private val analyticsRepo: WorkoutAnalyticsRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(): String? {
        // 1. Отримуємо список останніх сесій (за замовчуванням Room повертає Flow)
        val allLogs = analyticsRepo.getAllLogs().firstOrNull() ?: return null
        val mostRecent = allLogs.firstOrNull() ?: return null
        
        // 2. Отримуємо ВСІ сесії за цю ж календарну дату.
        // Це необхідно, тому що кожна вправа зараз зберігається як окрема сесія у базі.
        val sameDaySessions = analyticsRepo.getSessionsByDate(mostRecent.session.timestamp).firstOrNull() ?: emptyList()
        if (sameDaySessions.isEmpty()) return null
        
        // 3. Отримуємо всі вправи для мапінгу імен через репозиторій
        val allExercises = analyticsRepo.getAllExercisesMap()
        
        val contextBuilder = StringBuilder()
        val totalDayTonnage = sameDaySessions.sumOf { it.session.totalTonnage }
        
        contextBuilder.append(context.getString(R.string.text_workout_results_header, totalDayTonnage.toInt()))
        contextBuilder.append("\n")
        
        // 4. Збираємо дані про всі вправи та їх підходи, виконані протягом дня
        // Сортуємо за часом виконання, щоб зберегти послідовність
        sameDaySessions.sortedBy { it.session.timestamp }.forEach { sessionWithSets ->
            sessionWithSets.sets.forEach { set ->
                val name = allExercises[set.exerciseId] ?: context.getString(R.string.text_exercise_label, set.exerciseId)
                contextBuilder.append(context.getString(R.string.text_workout_results_item, set.exerciseId, name, set.weight, set.reps))
                contextBuilder.append("\n")
            }
        }
        
        return contextBuilder.toString().trim()
    }
}
