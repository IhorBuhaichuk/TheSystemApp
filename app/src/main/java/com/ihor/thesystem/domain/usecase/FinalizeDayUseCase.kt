package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.core.util.Result
import com.ihor.thesystem.domain.model.DomainError
import com.ihor.thesystem.domain.model.DomainQuestType
import com.ihor.thesystem.domain.model.PlayerRank
import com.ihor.thesystem.domain.model.SystemConfig
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.QuestRepository
import com.ihor.thesystem.domain.repository.SystemConfigRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class FinalizeDayUseCase @Inject constructor(
    private val playerRepo: PlayerRepository,
    private val questRepo: QuestRepository,
    private val configRepo: SystemConfigRepository,
    private val generateDailyQuestsUseCase: GenerateDailyQuestsUseCase,
    private val calculateAttributes: CalculateAttributesUseCase
) {
    /**
     * Повертає Event, який View model має показати (LevelUp або Penalty)
     */
    suspend operator fun invoke(): Result<DayFinalizationResult, DomainError> {
        val player = playerRepo.getPlayer().firstOrNull() ?: return Result.Success(DayFinalizationResult.None)
        val config = configRepo.getConfigFlow().firstOrNull() ?: SystemConfig(
            defaultPenalty = 20,
            targetSets = 3,
            targetReps = 12,
            matrixWeeks = 48
        )
        val todayQuests = questRepo.getActiveQuests().firstOrNull() ?: emptyList()

        val mainQuests = todayQuests.filter { it.type == DomainQuestType.MAIN }
        val allMainCompleted = mainQuests.isNotEmpty() && mainQuests.all { it.isCompleted }

        var penaltyActivated = false
        var levelUpTriggered = false

        // 1. Створюємо мутабельну копію гравця для розрахунків у пам'яті
        var updatedPlayer = player

        // 2. Оцінка Квестів (Penalty Zone Logic)
        if (mainQuests.isNotEmpty()) {
            if (!allMainCompleted) {
                val newFailures = updatedPlayer.consecutiveMainQuestFailures + 1
                updatedPlayer = updatedPlayer.copy(
                    consecutiveMainQuestFailures = newFailures,
                    currentStreak = 0
                )
                
                // Активуємо штраф, якщо 2 провали і він ще не активний
                if (newFailures >= 2 && !updatedPlayer.isPenaltyActive) {
                    penaltyActivated = true
                    updatedPlayer = updatedPlayer.copy(isPenaltyActive = true)
                }
            } else {
                // Успіх - знімаємо штрафи і обнуляємо лічильник провалів
                val newStreak = updatedPlayer.currentStreak + 1
                updatedPlayer = updatedPlayer.copy(
                    isPenaltyActive = false, 
                    consecutiveMainQuestFailures = 0,
                    currentStreak = newStreak,
                    maxStreak = if (newStreak > updatedPlayer.maxStreak) newStreak else updatedPlayer.maxStreak,
                    xpTotal = updatedPlayer.xpTotal + 100,
                    xpThisWeek = updatedPlayer.xpThisWeek + 100
                )
            }
        }

        // 3. Математика Часу (Мікроцикли, Тижні, Місяці)
        var newCycleDay = updatedPlayer.currentCycleDay + 1
        var newWeek = updatedPlayer.currentWeek
        var newMonth = updatedPlayer.currentMonth

        if (newCycleDay > config.cycleDaysPerMicrocycle) {
            newCycleDay = 1 // Скидаємо мікроцикл
            newWeek += 1    // Переходимо на наступний тиждень
            
            if (newWeek > config.microCyclesPerMonth) {
                newWeek = 1 // Скидаємо тиждень
                newMonth += 1 // Переходимо на новий місяць
                levelUpTriggered = true
            }
        }

        // 4. Оновлюємо поля часу та Ранг (якщо був Level Up)
        updatedPlayer = updatedPlayer.copy(
            currentCycleDay = newCycleDay,
            currentWeek = newWeek,
            currentMonth = newMonth
        )

        if (levelUpTriggered) {
            val newRank = PlayerRank.resolveByMonth(newMonth)
            updatedPlayer = updatedPlayer.copy(
                playerClass = newRank.title,
                xpTotal = updatedPlayer.xpTotal + 200,
                xpThisWeek = updatedPlayer.xpThisWeek + 200
            )
        }

        // Reset xpThisWeek to 0 when starting a new microcycle (newCycleDay == 1 && newWeek == 1)
        if (newCycleDay == 1 && newWeek == 1) {
            updatedPlayer = updatedPlayer.copy(xpThisWeek = 0)
        }

        // 5. РОБИМО ЄДИНИЙ ЗАПИС У БАЗУ ДАНИХ (Абсолютна стабільність)
        val updateResult = playerRepo.updatePlayer(updatedPlayer)
        if (updateResult is Result.Error) return Result.Error(updateResult.error)

        // 6. Архівуємо старі квести та генеруємо нові (Генератор візьме новий день з БД)
        questRepo.archiveActiveQuests()
        generateDailyQuestsUseCase.invoke()

        // 7. Перераховуємо атрибути
        val attrResult = calculateAttributes()
        if (attrResult is Result.Error) return Result.Error(attrResult.error)

        val finalResult = when {
            levelUpTriggered -> DayFinalizationResult.LevelUp
            penaltyActivated -> DayFinalizationResult.PenaltyZoneEntered
            else -> DayFinalizationResult.Success
        }
        
        return Result.Success(finalResult)
    }
}

sealed class DayFinalizationResult {
    object Success : DayFinalizationResult()
    object LevelUp : DayFinalizationResult()
    object PenaltyZoneEntered : DayFinalizationResult()
    object None : DayFinalizationResult()
}
