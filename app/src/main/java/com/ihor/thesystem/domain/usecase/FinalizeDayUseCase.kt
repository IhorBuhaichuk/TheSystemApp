package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.core.util.Result
import com.ihor.thesystem.domain.model.*
import com.ihor.thesystem.domain.repository.*
import com.ihor.thesystem.domain.util.TransactionRollbackException
import kotlinx.coroutines.flow.firstOrNull
import timber.log.Timber
import javax.inject.Inject

class FinalizeDayUseCase @Inject constructor(
    private val transactionProvider: TransactionProvider,
    private val playerRepo: PlayerRepository,
    private val questRepo: QuestRepository,
    private val configRepo: SystemConfigRepository,
    private val generateDailyQuests: GenerateDailyQuestsUseCase,
    private val calculateAttributes: CalculateAttributesUseCase,
    private val advanceCycleDayStatus: AdvanceCycleDayUseCase
) {
    /**
     * Фіналізація дня з оптимізованим обсягом транзакції для запобігання ANR.
     */
    suspend operator fun invoke(forceComplete: Boolean = false): Result<DayFinalizationResult, DomainError> {
        return try {
            val transactionResult = transactionProvider.runInTransaction {
                // 1. Оновлюємо статуси активних квестів (успіх/провал)
                val statusResult = advanceCycleDayStatus(forceComplete)
                if (statusResult is Result.Error) {
                    throw TransactionRollbackException(statusResult.error)
                }

                // 2. Отримуємо актуальний стан даних
                val player = playerRepo.getPlayer().firstOrNull() 
                    ?: throw TransactionRollbackException(DataError.Local.NOT_FOUND)
                val config = configRepo.getConfigFlow().firstOrNull() 
                    ?: SystemConfig()
                val activeQuests = questRepo.getActiveQuests().firstOrNull() ?: emptyList()

                // 3. Розрахунок XP та стріків на основі результатів квестів
                val mainQuests = activeQuests.filter { it.type == DomainQuestType.MAIN }
                val wasPenaltyActive = player.isPenaltyActive
                val (playerAfterXP, levelUpTriggered) = player.evaluateQuests(mainQuests).checkLevelUp()

                // 4. Просування часу (Cycle Day / Week / Month)
                val finalPlayer = playerAfterXP.advanceTime(config)

                // 5. Встановлюємо прапорець потреби ініціалізації перед оновленням гравця
                configRepo.setNeedsDailyInit(true)

                // 6. Збереження оновленого стану гравця
                playerRepo.updatePlayer(finalPlayer)

                // 7. Архівація квестів
                questRepo.archiveActiveQuests()

                // 8. Генерація нового дня та оновлення атрибутів всередині транзакції
                generateDailyQuests.invoke()
                calculateAttributes.invoke()

                // 9. Скидаємо прапорець після успішної ініціалізації
                configRepo.setNeedsDailyInit(false)

                val result = when {
                    levelUpTriggered -> DayFinalizationResult.LevelUp
                    !wasPenaltyActive && finalPlayer.isPenaltyActive -> DayFinalizationResult.PenaltyZoneEntered
                    else -> DayFinalizationResult.Success
                }
                result
            }

            Timber.d("Day Finalization completed successfully")
            Result.Success(transactionResult)

        } catch (e: TransactionRollbackException) {
            Timber.e(e, "Transaction rolled back during day finalization")
            Result.Error(e.error)
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error during day finalization")
            Result.Error(DataError.Local.SQLITE_EXCEPTION)
        }
    }
}
