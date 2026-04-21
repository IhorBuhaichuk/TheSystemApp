package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.core.util.AppClock
import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import kotlin.math.round
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

class GenerateDailyQuestsUseCase @Inject constructor(
    private val questRepo: QuestRepository,
    private val scheduleRepo: ScheduleRepository,
    private val configRepo: SystemConfigRepository,
    private val matrixRepo: ProgressionMatrixRepository,
    private val calculateRecommendation: CalculateRecommendedSetUseCase,
    private val clock: AppClock
) {
    suspend operator fun invoke() {
        val config = configRepo.getConfigFlow().firstOrNull() ?: return
        
        // 1. РОЗРАХУНОК ПОТОЧНОГО ДНЯ ЦИКЛУ (КАЛЕНДАРНИЙ)
        val now = clock.now()
        val currentDay = if (config.cycleAnchorDateTimestamp > 0) {
            val anchorDate = Instant.ofEpochMilli(config.cycleAnchorDateTimestamp)
                .atZone(ZoneId.systemDefault()).toLocalDate()
            val todayDate = Instant.ofEpochMilli(now)
                .atZone(ZoneId.systemDefault()).toLocalDate()
            
            val daysPassed = ChronoUnit.DAYS.between(anchorDate, todayDate).toInt()
            (config.cycleAnchorDay + daysPassed - 1) % config.cycleDaysPerMicrocycle + 1
        } else {
            1
        }

        // 2. Отримуємо розклад
        var schedule = scheduleRepo.getScheduleForDay(currentDay).firstOrNull()
        if (schedule == null) {
            delay(500)
            schedule = scheduleRepo.getScheduleForDay(currentDay).firstOrNull()
        }
        if (schedule == null) return

        // 3. Отримуємо існуючі квести
        val todayQuests = questRepo.getDailyQuestsForDate(now).first()
        
        // 4. ОЧИЩЕННЯ: Видаляємо старі основні квести за сьогодні, щоб не було дублів при зміні дня
        todayQuests.filter { it.type == DomainQuestType.MAIN }.forEach { 
            // Тут ми можемо або видалити, або просто не створювати новий. 
            // Краще не створювати новий, якщо вже є.
        }

        val hasRoutine = todayQuests.any { it.title.contains("РУТИНА", ignoreCase = true) }
        val hasMainWorkout = todayQuests.any { it.type == DomainQuestType.MAIN }

        // 5. ГЕНЕРАЦІЯ
        if (!hasRoutine) {
            questRepo.createDailyQuest(
                title = "РУТИНА | ДЕНЬ $currentDay",
                tasks = listOf("Прийом вітамінів", "Водний баланс (2л+)", "Звіт"),
                scheduleId = schedule.id
            )
        }

        val workoutTemplateName = schedule.workoutTemplateName
        if (!hasMainWorkout && workoutTemplateName != null) {
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
