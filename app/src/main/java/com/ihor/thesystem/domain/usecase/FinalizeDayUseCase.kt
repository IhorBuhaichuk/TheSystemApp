package com.ihor.thesystem.domain.usecase

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ihor.thesystem.core.util.Result
import com.ihor.thesystem.domain.model.DomainError
import com.ihor.thesystem.domain.model.DayFinalizationResult
import com.ihor.thesystem.feature.status.worker.DayFinalizationWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class FinalizeDayUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Ставить завдання фіналізації дня в чергу WorkManager.
     */
    operator fun invoke(): Result<DayFinalizationResult, DomainError> {
        val workRequest = OneTimeWorkRequestBuilder<DayFinalizationWorker>()
            .build()
        
        WorkManager.getInstance(context).enqueue(workRequest)
        
        // Повертаємо Success, оскільки запит успішно поставлено в чергу.
        // UI може підписатися на стан WorkRequest за його ID, якщо потрібно.
        return Result.Success(DayFinalizationResult.Success)
    }
}
