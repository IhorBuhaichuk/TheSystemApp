package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class GetLastWorkoutContextUseCase @Inject constructor(
    private val analyticsRepo: WorkoutAnalyticsRepository,
    private val getTrainingPhaseContext: GetTrainingPhaseContextUseCase,
    private val textProvider: WorkoutContextTextProvider
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
        val trainingPhaseContext = getTrainingPhaseContext(
            referenceTimestamp = mostRecent.session.timestamp
        )
        
        val contextBuilder = StringBuilder()
        val totalDayTonnage = sameDaySessions.sumOf { it.session.totalTonnage }
        
        contextBuilder.append(trainingPhaseContext.toPromptBlock())
        contextBuilder.append("\n\n")
        contextBuilder.append(textProvider.workoutResultsHeader(totalDayTonnage.toInt()))
        contextBuilder.append("\n")
        
        // 4. Збираємо дані про всі вправи та їх підходи, виконані протягом дня
        // Сортуємо за часом виконання, щоб зберегти послідовність
        sameDaySessions.sortedBy { it.session.timestamp }.forEach { sessionWithSets ->
            sessionWithSets.sets.forEach { set ->
                val name = allExercises[set.exerciseId] ?: textProvider.exerciseLabel(set.exerciseId)
                contextBuilder.append(
                    textProvider.workoutResultsItem(set.exerciseId, name, set.weight, set.reps)
                )
                contextBuilder.append("\n")
            }
        }
        
        return contextBuilder.toString().trim()
    }
}
