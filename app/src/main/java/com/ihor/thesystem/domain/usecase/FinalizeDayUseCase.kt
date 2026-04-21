package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.core.util.Result
import com.ihor.thesystem.domain.model.DataError
import com.ihor.thesystem.domain.model.DayFinalizationResult
import com.ihor.thesystem.domain.model.DomainError
import com.ihor.thesystem.domain.model.DomainQuestType
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
     * Виконує фіналізацію дня, делегуючи бізнес-логіку доменній моделі Player.
     */
    suspend operator fun invoke(): Result<DayFinalizationResult, DomainError> {
        val player = playerRepo.getPlayer().firstOrNull() 
            ?: return Result.Error(DataError.Local.NOT_FOUND)
        
        val config = configRepo.getConfigFlow().firstOrNull() ?: SystemConfig()
        
        val todayQuests = questRepo.getActiveQuests().firstOrNull() ?: emptyList()
        val mainQuests = todayQuests.filter { it.type == DomainQuestType.MAIN }

        // 1. Ланцюжок трансформацій доменної моделі (Immutable logic)
        val (updatedPlayer, levelUpTriggered) = player
            .evaluateQuests(mainQuests)
            .advanceTime(config)
            .checkLevelUp()

        // 2. Збереження оновленого стану в репозиторій
        val updateResult = playerRepo.updatePlayer(updatedPlayer)
        if (updateResult is Result.Error) return Result.Error(updateResult.error)

        // 3. Оркестрація побічних ефектів
        questRepo.archiveActiveQuests()
        generateDailyQuestsUseCase.invoke()
        
        val attrResult = calculateAttributes()
        if (attrResult is Result.Error) return Result.Error(attrResult.error)

        // 4. Визначення результату для UI
        val finalResult = when {
            levelUpTriggered -> DayFinalizationResult.LevelUp
            else -> DayFinalizationResult.Success
        }
        
        return Result.Success(finalResult)
    }
}
