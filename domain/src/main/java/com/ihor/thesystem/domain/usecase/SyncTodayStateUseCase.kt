package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.DataError
import com.ihor.thesystem.domain.model.DayFinalizationResult
import com.ihor.thesystem.domain.model.DomainError
import com.ihor.thesystem.domain.model.SystemConfig
import com.ihor.thesystem.domain.repository.SystemConfigRepository
import com.ihor.thesystem.domain.repository.TransactionProvider
import com.ihor.thesystem.domain.util.AppClock
import com.ihor.thesystem.domain.util.AppLogger
import com.ihor.thesystem.domain.util.Result
import com.ihor.thesystem.domain.util.TransactionRollbackException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.firstOrNull
import java.time.Instant
import javax.inject.Inject

class SyncTodayStateUseCase @Inject constructor(
    private val transactionProvider: TransactionProvider,
    private val configRepo: SystemConfigRepository,
    private val finalizeDay: FinalizeDayUseCase,
    private val generateDailyQuests: GenerateDailyQuestsUseCase,
    private val calculateAttributes: CalculateAttributesUseCase,
    private val clock: AppClock,
    private val logger: AppLogger
) {
    suspend operator fun invoke(): Result<DayFinalizationResult, DomainError> {
        return try {
            val config = configRepo.getConfigFlow().firstOrNull() ?: SystemConfig()
            val todayEpochDay = todayEpochDay()

            if (config.lastInitEpochDay > 0L && config.lastInitEpochDay < todayEpochDay) {
                return finalizeDay(forceComplete = false)
            }

            val result = transactionProvider.runInTransaction {
                if (config.needsDailyInit || config.lastInitEpochDay <= 0L) {
                    configRepo.setNeedsDailyInit(true)
                }

                generateDailyQuests()
                when (val attributesResult = calculateAttributes()) {
                    is Result.Success -> Unit
                    is Result.Error -> throw TransactionRollbackException(attributesResult.error)
                }

                configRepo.saveLastInitDate(todayEpochDay)
                configRepo.setNeedsDailyInit(false)
                DayFinalizationResult.None
            }

            Result.Success(result)
        } catch (e: TransactionRollbackException) {
            logger.e(e, "Today state sync rolled back")
            Result.Error(e.error)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.e(e, "Unexpected error during today state sync")
            Result.Error(DataError.Local.SQLITE_EXCEPTION)
        }
    }

    private fun todayEpochDay(): Long =
        Instant.ofEpochMilli(clock.now())
            .atZone(clock.zoneId())
            .toLocalDate()
            .toEpochDay()
}
