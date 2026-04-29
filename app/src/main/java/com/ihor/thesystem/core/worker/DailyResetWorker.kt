package com.ihor.thesystem.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import com.ihor.thesystem.domain.usecase.FinalizeDayUseCase
import timber.log.Timber
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@HiltWorker
class DailyResetWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val finalizeDay: FinalizeDayUseCase
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            Timber.d("DailyResetWorker: starting midnight reset via FinalizeDayUseCase")

            // Використовуємо єдиний UseCase для фіналізації дня
            finalizeDay(forceComplete = false)

            Timber.d("DailyResetWorker: completed successfully")

            // Плануємо наступний запуск на наступну північ
            scheduleNext()

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "DailyResetWorker: failed, will retry")
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

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
        const val WORK_TAG  = "daily_reset"

        /**
         * Викликати один раз при старті застосунку.
         * ExistingWorkPolicy.KEEP — якщо worker вже запланований, не перепланувати.
         */
        fun scheduleIfNotRunning(context: Context) {
            val now = LocalDateTime.now()
            val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay()
            val delayMillis = Duration.between(now, nextMidnight).toMillis().coerceAtLeast(1000L)

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.KEEP,   // ← не замінювати вже запланований
                OneTimeWorkRequestBuilder<DailyResetWorker>()
                    .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                    .setBackoffCriteria(BackoffPolicy.LINEAR, 15, TimeUnit.MINUTES)
                    .addTag(WORK_TAG)
                    .build()
            )
        }
    }
}
