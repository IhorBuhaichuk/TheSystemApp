package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.core.util.Result
import com.ihor.thesystem.domain.model.DataError
import com.ihor.thesystem.domain.model.DomainError
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

class CalculateRecoveryWindowUseCase @Inject constructor() {

    /**
     * Розраховує час відновлення на основі тоннажу та робочого графіка.
     * @param tonnage Загальний піднятий тоннаж за тренування (в кілограмах).
     * @return Result з об'єктом Duration, що містить час відновлення.
     */
    operator fun invoke(tonnage: Double): Result<Duration, DomainError> {
        return try {
            var baseRecovery = 24.hours

            // Додатковий час за об'єм роботи: 2 години за кожну 1000 кг (1 тонну)
            val tonnagePenaltyHours = ((tonnage / 1000.0) * 2).hours
            baseRecovery += tonnagePenaltyHours

            // Жорсткий математичний фільтр (clamp): мінімум 24 год, максимум 72 год
            Result.Success(baseRecovery.coerceIn(24.hours, 72.hours))
        } catch (e: Exception) {
            Result.Error(DataError.Local.SQLITE_EXCEPTION) // Or a more appropriate error
        }
    }
}
