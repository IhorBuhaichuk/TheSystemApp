package com.ihor.thesystem.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker.Result as WorkResult
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ihor.thesystem.domain.usecase.FinalizeDayUseCase
import com.ihor.thesystem.domain.util.Result as DomainResult
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException
import timber.log.Timber

@HiltWorker
class DailyResetWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val finalizeDay: FinalizeDayUseCase
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): WorkResult {
        return try {
            Timber.d("DailyResetWorker: starting midnight reset via FinalizeDayUseCase")

            when (val resetResult = finalizeDay(forceComplete = false)) {
                is DomainResult.Success -> {
                    Timber.d("DailyResetWorker: completed successfully")
                    scheduleNext()
                    WorkResult.success()
                }
                is DomainResult.Error -> {
                    Timber.e("DailyResetWorker: finalization failed: ${resetResult.error.message}")
                    retryOrFail()
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Timber.e(e, "DailyResetWorker: failed, will retry")
            retryOrFail()
        }
    }

    private fun retryOrFail(): WorkResult =
        if (runAttemptCount < MAX_RUN_ATTEMPTS) WorkResult.retry() else WorkResult.failure()

    private fun scheduleNext() {
        val now = LocalDateTime.now()
        val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay()
        val delayMillis = Duration.between(now, nextMidnight).toMillis().coerceAtLeast(1000L)

        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<DailyResetWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 15, TimeUnit.MINUTES)
                .addTag(WORK_TAG)
                .build()
        )
    }

    companion object {
        const val WORK_NAME = "daily_reset_worker"
        const val WORK_TAG = "daily_reset"
        private const val MAX_RUN_ATTEMPTS = 3

        fun scheduleIfNotRunning(context: Context) {
            val now = LocalDateTime.now()
            val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay()
            val delayMillis = Duration.between(now, nextMidnight).toMillis().coerceAtLeast(1000L)

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.KEEP,
                OneTimeWorkRequestBuilder<DailyResetWorker>()
                    .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                    .setBackoffCriteria(BackoffPolicy.LINEAR, 15, TimeUnit.MINUTES)
                    .addTag(WORK_TAG)
                    .build()
            )
        }
    }
}
