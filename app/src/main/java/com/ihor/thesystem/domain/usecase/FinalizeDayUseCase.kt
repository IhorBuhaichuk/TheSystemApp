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
     * Повна синхронна фіналізація дня в межах однієї транзакції.
     */
    suspend operator fun invoke(forceComplete: Boolean = false): Result<DayFinalizationResult, DomainError> {
        return try {
            transactionProvider.runInTransaction {
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
                val (playerAfterXP, _) = player.evaluateQuests(mainQuests).checkLevelUp()

                // 4. Просування часу (Cycle Day / Week / Month) - ВИКЛИКАЄТЬСЯ ОДИН РАЗ
                val finalPlayer = playerAfterXP.advanceTime(config)

                // 5. Збереження оновленого стану гравця
                playerRepo.updatePlayer(finalPlayer)

                // 6. Архівація та генерація нового дня
                questRepo.archiveActiveQuests()
                generateDailyQuests.invoke()
                
                // 7. Оновлення атрибутів RPG на основі Matrix
                calculateAttributes.invoke()

                Timber.d("Day Finalization transaction completed successfully")
                Result.Success(DayFinalizationResult.Success)
            }
        } catch (e: TransactionRollbackException) {
            Timber.e(e, "Transaction rolled back during day finalization")
            Result.Error(e.error)
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error during day finalization")
            Result.Error(DataError.Local.SQLITE_EXCEPTION)
        }
    }
}
