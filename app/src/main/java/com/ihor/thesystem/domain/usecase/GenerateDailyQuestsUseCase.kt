package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.core.util.AppClock
import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.*
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import kotlin.math.round
import java.time.Instant
import java.time.ZoneId

class GenerateDailyQuestsUseCase @Inject constructor(
    private val questRepo: QuestRepository,
    private val scheduleRepo: ScheduleRepository,
    private val configRepo: SystemConfigRepository,
    private val matrixRepo: ProgressionMatrixRepository,
    private val calculateRecommendation: CalculateRecommendedSetUseCase,
    private val calculateCycleDay: CalculateCycleDayForDateUseCase,
    private val clock: AppClock
) {
    suspend operator fun invoke() {
        val config = configRepo.getConfigFlow().firstOrNull() ?: return
        
        // 1. РОЗРАХУНОК ПОТОЧНОГО ДНЯ ЦИКЛУ (ЛОГІЧНА ДОБА)
        val now = clock.now()
        val todayDate = Instant.ofEpochMilli(now)
            .atZone(ZoneId.systemDefault())
            .minusHours(config.dayStartOffsetHours.toLong())
            .toLocalDate()

        val currentDay = if (config.cycleAnchorDateTimestamp > 0) {
            calculateCycleDay(
                targetDate = todayDate,
                anchorEpochDay = java.time.Instant.ofEpochMilli(config.cycleAnchorDateTimestamp)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
                    .toEpochDay(),
                anchorCycleDay = config.cycleAnchorDay,
                cycleDaysPerMicrocycle = config.cycleDaysPerMicrocycle
            )
        } else {
            1
        }

        // 2. Отримуємо розклад
        val schedule = scheduleRepo.getScheduleForDay(currentDay)
            .firstOrNull() ?: return

        // 3. Отримуємо існуючі квести
        val todayQuests = questRepo.getDailyQuestsForDate(now).first()
        
        // 4. ОЧИЩЕННЯ та ПЕРЕВІРКА ДУБЛІКАТІВ
        val existingMainQuest = todayQuests.find { it.type == DomainQuestType.MAIN }
        
        if (existingMainQuest != null) {
            if (existingMainQuest.scheduleId == schedule.id) {
                // Квест для цього розкладу вже існує, нічого не робимо
                return 
            } else {
                // Розклад змінився, видаляємо старий квест
                questRepo.deleteQuestWithTasks(existingMainQuest.id)
            }
        }

        val hasRoutine = todayQuests.any { it.type == DomainQuestType.DAILY }

        // 5. ГЕНЕРАЦІЯ
        if (!hasRoutine) {
            questRepo.createDailyQuest(
                title = "РУТИНА | ДЕНЬ $currentDay",
                tasks = listOf("Прийом вітамінів", "Водний баланс (2л+)", "Звіт"),
                scheduleId = schedule.id
            )
        }

        val workoutTemplateName = schedule.workoutTemplateName
        if (workoutTemplateName != null) {
            val recommendations = schedule.exercises.map { ex ->
                val rec = calculateRecommendation(ex.id, ex.name)
                ExerciseRecommendation(ex.id, ex.name, rec.weight, rec.sets, rec.reps)
            }

            questRepo.createMainQuest(
                title = workoutTemplateName.uppercase(),
                exercises = recommendations,
                scheduleId = schedule.id
            )
        }

        generatePromotionQuests()
    }

    private suspend fun generatePromotionQuests() {
        val matrixEntries = matrixRepo.getAllEntries().firstOrNull() ?: emptyList()
        matrixEntries.filter { it.isPromotionPending }.forEach { pending ->
            val active = questRepo.getActiveQuests().firstOrNull() ?: emptyList()
            if (active.none { it.type == DomainQuestType.PROMOTION && it.targetExerciseId == pending.exerciseId }) {
                val examWeight = (round((pending.targetWeight * 1.025f) / 2.5f) * 2.5f).toDouble()
                questRepo.createPromotionQuest(pending.exerciseId, "ЕКЗАМЕН: ${pending.exerciseName.uppercase()}", "Тест 1RM", examWeight, 1)
            }
        }
    }
}
