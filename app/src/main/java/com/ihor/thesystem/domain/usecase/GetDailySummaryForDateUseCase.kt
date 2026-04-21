package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.DomainQuestType
import com.ihor.thesystem.domain.repository.QuestRepository
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId
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
    private val analyticsRepo: WorkoutAnalyticsRepository
) {
    operator fun invoke(date: LocalDate): Flow<List<CalendarLogItem>> {
        val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val workoutsFlow = analyticsRepo.getSessionsByDate(startOfDay).map { sessions ->
            val exerciseNames = analyticsRepo.getAllExercisesMap()
            sessions.flatMap { session ->
                session.sets.groupBy { it.exerciseId }.map { (id, _) ->
                    CalendarLogItem(
                        type = LogType.WORKOUT,
                        title = "Main Quest",
                        subtitle = exerciseNames[id] ?: "Вправа",
                        isCompleted = true
                    )
                }
            }
        }

        val questsFlow = questRepo.getQuestsByDate(startOfDay).map { quests ->
            quests.map { quest ->
                CalendarLogItem(
                    type = LogType.QUEST,
                    title = if (quest.type == DomainQuestType.MAIN) "Main Quest" else "Daily",
                    subtitle = quest.title,
                    isCompleted = quest.isCompleted
                )
            }
        }

        return combine(workoutsFlow, questsFlow) { workouts, quests ->
            (workouts + quests).distinctBy { it.subtitle }
        }
    }
}
