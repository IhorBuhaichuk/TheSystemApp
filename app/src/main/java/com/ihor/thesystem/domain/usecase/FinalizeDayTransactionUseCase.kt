package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.core.util.Result
import com.ihor.thesystem.domain.model.DayFinalizationResult
import com.ihor.thesystem.domain.model.DomainError
import com.ihor.thesystem.domain.repository.TransactionProvider
import javax.inject.Inject

/**
 * Фасадний UseCase для транзакційного виконання оновлення статусів квестів
 * та фіналізації стану гравця.
 *
 * SRP: Цей клас відповідає лише за оркестрацію БД-транзакції.
 * Блок runInTransaction повинен містити виключно операції з базою даних
 * для уникнення блокування потоків БД тривалими обчисленнями або мережевими запитами.
 */
class FinalizeDayTransactionUseCase @Inject constructor(
    private val transactionProvider: TransactionProvider,
    private val advanceCycleDay: AdvanceCycleDayUseCase,
    private val finalizeDay: FinalizeDayUseCase
) {
    suspend operator fun invoke(forceComplete: Boolean): Result<DayFinalizationResult, DomainError> {
        
        // 1. Поза транзакцією: Місце для мережевих запитів або AI-аналізу
        // val aiContext = aiUseCase.prepareContext() 

        // 2. Транзакційний блок: Тільки атомарні зміни в БД
        val result = transactionProvider.runInTransaction {
            val advanceResult = advanceCycleDay(forceComplete)
            if (advanceResult is Result.Error) return@runInTransaction Result.Error(advanceResult.error)
            
            finalizeDay()
        }

        // 3. Поза транзакцією: Пост-обробка, логування або синхронізація
        // if (result is Result.Success) { syncWithCloud(result.data) }

        return result
    }
}
