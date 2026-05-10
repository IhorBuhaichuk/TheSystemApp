package com.ihor.thesystem.domain.usecase

import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

class CalculateRecoveryWindowUseCase @Inject constructor() {

    /**
     * Розраховує час відновлення на основі тоннажу.
     * Базовий час: 24 год. + 2 год за кожну тонну (1000 кг).
     * Обмеження: від 24 до 72 годин.
     */
    operator fun invoke(tonnage: Double): Duration =
        (24.hours + ((tonnage / 1000.0) * 2).hours).coerceIn(24.hours, 72.hours)
}
