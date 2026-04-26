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
        
        // 1. РОЗРАХУНОК ПОТОЧНОГО ДНЯ ЦИКЛУ
        val now = clock.now()
        val todayDate = Instant.ofEpochMilli(now)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val currentDay = if (config.cycleAnchorDateTimestamp > 0) {
            calculateCycleDay(
                targetDate = todayDate,
                anchorEpochDay = config.cycleAnchorDateTimestamp,
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
        
        // 4. ОЧИЩЕННЯ ТА ХАРМОНІЗАЦІЯ
        // Перевіряємо, чи існуючі квести відповідають поточному розкладу.
        // Якщо розклад змінився (наприклад, після синхронізації), старі квести треба видалити.
        val existingMainQuest = todayQuests.find { it.type == DomainQuestType.MAIN }
        
        if (existingMainQuest != null) {
            if (existingMainQuest.scheduleId == schedule.id) {
                // Квест для цього розкладу вже існує
            } else {
                // Розклад змінився, видаляємо старий квест
                questRepo.deleteQuestWithTasks(existingMainQuest.id)
            }
        } else if (schedule.workoutTemplateId == null) {
             // Ми на дні відпочинку, але можливо залишився старий MAIN квест від попереднього розкладу
             // (хоча logic вище має це покрити, додамо для надійності)
             todayQuests.filter { it.type == DomainQuestType.MAIN }.forEach {
                 questRepo.deleteQuestWithTasks(it.id)
             }
        }

        val existingDailyQuest = todayQuests.find { it.type == DomainQuestType.DAILY }
        if (existingDailyQuest != null && existingDailyQuest.scheduleId != schedule.id) {
             // Видаляємо рутину, якщо вона від іншого дня циклу
             questRepo.deleteQuestWithTasks(existingDailyQuest.id)
        }

        val updatedQuests = questRepo.getDailyQuestsForDate(now).first()
        val hasRoutine = updatedQuests.any { it.type == DomainQuestType.DAILY }
        val hasMain = updatedQuests.any { it.type == DomainQuestType.MAIN }

        // 5. ГЕНЕРАЦІЯ
        if (!hasRoutine) {
            questRepo.createDailyQuest(
                title = "РУТИНА | ДЕНЬ $currentDay",
                tasks = listOf("Прийом вітамінів", "Водний баланс (2л+)", "Звіт"),
                scheduleId = schedule.id
            )
        }

        val workoutTemplateName = schedule.workoutTemplateName
        if (!hasMain && workoutTemplateName != null && schedule.exercises.isNotEmpty()) {
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
