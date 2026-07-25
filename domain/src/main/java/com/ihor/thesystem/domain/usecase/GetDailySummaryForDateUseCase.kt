package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.DomainQuestType
import com.ihor.thesystem.domain.model.ExerciseDetails
import com.ihor.thesystem.domain.model.ExerciseSet
import com.ihor.thesystem.domain.model.ExerciseTrackingMode
import com.ihor.thesystem.domain.model.ExerciseTrackingModeResolver
import com.ihor.thesystem.domain.model.formatForTrackingMode
import com.ihor.thesystem.domain.repository.QuestRepository
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import com.ihor.thesystem.domain.repository.WorkoutRepository
import com.ihor.thesystem.domain.util.AppClock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

enum class LogType { WORKOUT, QUEST }

data class CalendarLogItem(
    val type: LogType,
    val title: String,
    val subtitle: String,
    val isCompleted: Boolean
)

class GetDailySummaryForDateUseCase @Inject constructor(
    private val questRepo: QuestRepository,
    private val analyticsRepo: WorkoutAnalyticsRepository,
    private val workoutRepository: WorkoutRepository,
    private val clock: AppClock
) {
    operator fun invoke(date: LocalDate): Flow<List<CalendarLogItem>> {
        val startOfDay = date.atStartOfDay(clock.zoneId()).toInstant().toEpochMilli()

        val workoutsFlow = analyticsRepo.getSessionsByDate(startOfDay).map { sessions ->
            val exercises = workoutRepository.getAllExercisesSync().associateBy { it.id }
            sessions.flatMap { session ->
                session.sets.groupBy { it.exerciseId }.map { (id, sets) ->
                    val completedSets = sets.filter { it.isCompleted }
                    val exercise = exercises[id]
                    val exerciseName = exercise?.nameUk ?: exercise?.name ?: "Вправа"
                    val trackingMode = resolveTrackingMode(
                        exercise = exercise,
                        exerciseName = exerciseName,
                        sets = completedSets
                    )
                    CalendarLogItem(
                        type = LogType.WORKOUT,
                        title = "Тренування",
                        subtitle = buildWorkoutSubtitle(
                            exerciseName = exerciseName,
                            trackingMode = trackingMode,
                            sets = completedSets
                        ),
                        isCompleted = true
                    )
                }
            }
        }

        val questsFlow = questRepo.getQuestsByDate(startOfDay).map { quests ->
            quests.map { quest ->
                CalendarLogItem(
                    type = LogType.QUEST,
                    title = if (quest.type == DomainQuestType.MAIN) "Головний квест" else "Щоденне завдання",
                    subtitle = quest.title,
                    isCompleted = quest.isCompleted
                )
            }
        }

        return combine(workoutsFlow, questsFlow) { workouts, quests ->
            (workouts + quests).distinctBy { it.subtitle }
        }
    }

    private fun resolveTrackingMode(
        exercise: ExerciseDetails?,
        exerciseName: String,
        sets: List<ExerciseSet>
    ): ExerciseTrackingMode {
        val resolved = exercise?.let(ExerciseTrackingModeResolver::resolve)
            ?: ExerciseTrackingModeResolver.resolve(name = exerciseName)
        return if (
            resolved == ExerciseTrackingMode.WEIGHT_REPS &&
            sets.none { it.weight > TECHNICAL_LOAD_WEIGHT }
        ) {
            ExerciseTrackingMode.BODYWEIGHT_REPS
        } else {
            resolved
        }
    }

    private fun buildWorkoutSubtitle(
        exerciseName: String,
        trackingMode: ExerciseTrackingMode,
        sets: List<ExerciseSet>
    ): String {
        val totalSets = sets.size
        val bestSet = sets.maxWithOrNull(
            when (trackingMode) {
                ExerciseTrackingMode.WEIGHT_REPS -> compareBy<ExerciseSet> { it.weight }.thenBy { it.reps }
                else -> compareBy<ExerciseSet> { it.reps }
            }
        )
        val bestMetric = bestSet?.formatForTrackingMode(trackingMode) ?: "—"
        return "$exerciseName: $totalSets підх. · $bestMetric"
    }
}

private const val TECHNICAL_LOAD_WEIGHT = 1.0
