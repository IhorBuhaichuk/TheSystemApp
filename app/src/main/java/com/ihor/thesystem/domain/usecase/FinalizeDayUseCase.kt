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
     * Фіналізація дня з оптимізованим обсягом транзакції для запобігання ANR та Deadlocks.
     * Reads виконуються ДО транзакції, heavy logic — ПІСЛЯ.
     */
    suspend operator fun invoke(forceComplete: Boolean = false): Result<DayFinalizationResult, DomainError> {
        return try {
            // 1. Зчитуємо всі необхідні дані ДО початку транзакції для уникнення дедлоків
            val player = playerRepo.getPlayer().firstOrNull()
                ?: return Result.Error(DataError.Local.NOT_FOUND)
            val config = configRepo.getConfigFlow().firstOrNull()
                ?: SystemConfig()
            val activeQuests = questRepo.getActiveQuests().firstOrNull() ?: emptyList()

            // 2. Виконуємо транзакцію лише для швидких записів
            val transactionResult = transactionProvider.runInTransaction {
                // Оновлюємо статуси (успіх/провал)
                val statusResult = advanceCycleDayStatus(forceComplete)
                if (statusResult is Result.Error) {
                    throw TransactionRollbackException(statusResult.error)
                }

                val mainQuests = activeQuests.filter { it.type == DomainQuestType.MAIN }
                val wasPenaltyActive = player.isPenaltyActive
                
                // Розрахунки в пам'яті на основі зчитаних даних
                val (playerAfterXP, levelUpTriggered) = player.evaluateQuests(mainQuests).checkLevelUp()
                val finalPlayer = playerAfterXP.advanceTime(config)

                // Атомарні записи стану
                configRepo.setNeedsDailyInit(true)
                playerRepo.updatePlayer(finalPlayer)
                questRepo.archiveActiveQuests()

                // Формуємо результат для повернення з транзакції
                val result = when {
                    levelUpTriggered -> DayFinalizationResult.LevelUp
                    !wasPenaltyActive && finalPlayer.isPenaltyActive -> DayFinalizationResult.PenaltyZoneEntered
                    else -> DayFinalizationResult.Success
                }
                result
            }

            // 3. Важкі операції генерації та розрахунку атрибутів ПІСЛЯ транзакції
            generateDailyQuests.invoke()
            calculateAttributes.invoke()
            
            // Скидаємо прапорець ініціалізації після успішного виконання важких задач
            configRepo.setNeedsDailyInit(false)

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
