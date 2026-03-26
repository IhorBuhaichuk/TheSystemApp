package com.ihor.thesystem.domain.usecase

import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

class CalculateRecoveryWindowUseCase @Inject constructor() {

    /**
     * Розраховує час відновлення на основі тоннажу та робочого графіка.
     * * @param tonnage Загальний піднятий тоннаж за тренування (в кілограмах).
     * @param isNightShift Чи була попередня зміна нічною.
     * @return Result з об'єктом Duration, що містить час відновлення.
     */
    operator fun invoke(tonnage: Double, isNightShift: Boolean): Result<Duration> {
        return runCatching {
            var baseRecovery = 24.hours

            // Штраф за нічну зміну (виснаження ЦНС)
            if (isNightShift) {
                baseRecovery += 12.hours
            }

            // Додатковий час за об'єм роботи: 2 години за кожну 1000 кг (1 тонну)
            val tonnagePenaltyHours = ((tonnage / 1000.0) * 2).hours
            baseRecovery += tonnagePenaltyHours

            // Жорсткий математичний фільтр (clamp): мінімум 24 год, максимум 72 год
            baseRecovery.coerceIn(24.hours, 72.hours)
        }
    }
}