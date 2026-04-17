package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.core.util.Result
import com.ihor.thesystem.domain.model.DayFinalizationResult
import com.ihor.thesystem.domain.model.DomainError
import com.ihor.thesystem.domain.repository.TransactionProvider
import com.ihor.thesystem.domain.util.TransactionRollbackException
import javax.inject.Inject

/**
 * Фасадний UseCase для транзакційного виконання оновлення статусів квестів
 * та фіналізації стану гравця з гарантованим відкатом при помилках.
 */
class FinalizeDayTransactionUseCase @Inject constructor(
    private val transactionProvider: TransactionProvider,
    private val advanceCycleDay: AdvanceCycleDayUseCase,
    private val finalizeDay: FinalizeDayUseCase
) {
    suspend operator fun invoke(forceComplete: Boolean): Result<DayFinalizationResult, DomainError> {
        return try {
            transactionProvider.runInTransaction {
                // Виконуємо перший UseCase
                val advanceResult = advanceCycleDay(forceComplete)
                if (advanceResult is Result.Error) {
                    // Викидаємо Exception для ініціації Rollback у Room
                    throw TransactionRollbackException(advanceResult.error)
                }
                
                // Виконуємо другий UseCase
                val finalizeResult = finalizeDay()
                if (finalizeResult is Result.Error) {
                    throw TransactionRollbackException(finalizeResult.error)
                }
                
                finalizeResult
            }
        } catch (e: TransactionRollbackException) {
            // Перехоплюємо сигнал відкату та повертаємо чистий Result.Error
            Result.Error(e.error)
        }
    }
}
