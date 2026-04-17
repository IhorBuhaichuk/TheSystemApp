package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.core.util.Result
import com.ihor.thesystem.domain.model.DayFinalizationResult
import com.ihor.thesystem.domain.model.DomainError
import com.ihor.thesystem.domain.repository.TransactionProvider
import javax.inject.Inject

/**
 * Фасадний UseCase для транзакційного виконання оновлення статусів квестів
 * та фіналізації стану гравця.
 */
class FinalizeDayTransactionUseCase @Inject constructor(
    private val transactionProvider: TransactionProvider,
    private val advanceCycleDay: AdvanceCycleDayUseCase,
    private val finalizeDay: FinalizeDayUseCase
) {
    suspend operator fun invoke(forceComplete: Boolean): Result<DayFinalizationResult, DomainError> {
        return transactionProvider.runInTransaction {
            val advanceResult = advanceCycleDay(forceComplete)
            if (advanceResult is Result.Error) return@runInTransaction Result.Error(advanceResult.error)
            
            finalizeDay()
        }
    }
}
