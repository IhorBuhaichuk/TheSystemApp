package com.ihor.thesystem.core.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import com.ihor.thesystem.domain.repository.QuestRepository
import com.ihor.thesystem.domain.repository.SystemConfigRepository
import com.ihor.thesystem.domain.usecase.GenerateDailyQuestsUseCase
import com.ihor.thesystem.domain.usecase.CalculateAttributesUseCase
import timber.log.Timber
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@HiltWorker
class DailyResetWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val questRepo: QuestRepository,
    private val configRepo: SystemConfigRepository,
    private val generateDailyQuests: GenerateDailyQuestsUseCase,
    private val calculateAttributes: CalculateAttributesUseCase
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            Timber.d("DailyResetWorker: starting midnight reset")

            // 1. Архівуємо поточні квести (виконані + активні → LOCKED)
            questRepo.archiveActiveQuests()

            // 2. Встановлюємо прапорець ініціалізації
            configRepo.setNeedsDailyInit(false)

            // 3. Генеруємо нові завдання на новий день
            generateDailyQuests()

            // 4. Перераховуємо атрибути
            calculateAttributes()

            Timber.d("DailyResetWorker: completed successfully")

            // 5. Плануємо наступний запуск на наступну північ
            scheduleNext()

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "DailyResetWorker: failed, will retry")
            // При помилці — retry через 15 хвилин, але не більше 3 разів
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
