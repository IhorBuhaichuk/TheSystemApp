package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.DomainQuestType
import com.ihor.thesystem.domain.model.PlayerRank
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.QuestRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class FinalizeDayUseCase @Inject constructor(
    private val playerRepo: PlayerRepository,
    private val questRepo: QuestRepository,
    private val generateDailyQuestsUseCase: GenerateDailyQuestsUseCase,
    private val calculateAttributes: CalculateAttributesUseCase
) {
    /**
     * Повертає Event, який View model має показати (LevelUp або Penalty)
     */
    suspend operator fun invoke(): DayFinalizationResult {
        val player = playerRepo.getPlayer().firstOrNull() ?: return DayFinalizationResult.None
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
                updatedPlayer = updatedPlayer.copy(consecutiveMainQuestFailures = newFailures)
                
                // Активуємо штраф, якщо 2 провали і він ще не активний
                if (newFailures >= 2 && !updatedPlayer.isPenaltyActive) {
                    penaltyActivated = true
                    updatedPlayer = updatedPlayer.copy(isPenaltyActive = true)
                }
            } else {
                // Успіх - знімаємо штрафи і обнуляємо лічильник провалів
                updatedPlayer = updatedPlayer.copy(
                    isPenaltyActive = false, 
                    consecutiveMainQuestFailures = 0
                )
            }
        }

        // 3. Математика Часу (Мікроцикли, Тижні, Місяці)
        var newCycleDay = updatedPlayer.currentCycleDay + 1
        var newWeek = updatedPlayer.currentWeek
        var newMonth = updatedPlayer.currentMonth

        if (newCycleDay > 4) {
            newCycleDay = 1 // Скидаємо мікроцикл
            newWeek += 1    // Переходимо на наступний тиждень
            
            if (newWeek > 4) {
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
            updatedPlayer = updatedPlayer.copy(playerClass = newRank.title)
        }

        // 5. РОБИМО ЄДИНИЙ ЗАПИС У БАЗУ ДАНИХ (Абсолютна стабільність)
        playerRepo.updatePlayer(updatedPlayer)

        // 6. Архівуємо старі квести та генеруємо нові (Генератор візьме новий день з БД)
        questRepo.archiveActiveQuests()
        generateDailyQuestsUseCase.invoke()

        // 7. Перераховуємо атрибути
        calculateAttributes()

        return when {
            levelUpTriggered -> DayFinalizationResult.LevelUp
            penaltyActivated -> DayFinalizationResult.PenaltyZoneEntered
            else -> DayFinalizationResult.Success
        }
    }
}

sealed class DayFinalizationResult {
    object Success : DayFinalizationResult()
    object LevelUp : DayFinalizationResult()
    object PenaltyZoneEntered : DayFinalizationResult()
    object None : DayFinalizationResult()
}
