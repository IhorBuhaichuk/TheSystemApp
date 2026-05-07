package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.core.util.AppClock
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlinx.coroutines.flow.firstOrNull

class GetTrainingPhaseContextUseCase @Inject constructor(
    private val analyticsRepository: WorkoutAnalyticsRepository,
    private val clock: AppClock
) {
    suspend operator fun invoke(referenceTimestamp: Long? = null): TrainingPhaseContext {
        val logs = analyticsRepository.getAllLogs().firstOrNull().orEmpty()
        val referenceDate = (
            referenceTimestamp
                ?: logs.maxOfOrNull { it.session.timestamp }
                ?: clock.now()
            ).toLocalDate()
        val firstWorkoutDate = logs.minOfOrNull { it.session.timestamp }?.toLocalDate()

        return TrainingPhaseContext(
            firstWorkoutDate = firstWorkoutDate,
            referenceDate = referenceDate
        )
    }

    private fun Long.toLocalDate(): LocalDate =
        Instant.ofEpochMilli(this).atZone(clock.zoneId()).toLocalDate()
}

data class TrainingPhaseContext(
    val firstWorkoutDate: LocalDate?,
    val referenceDate: LocalDate
) {
    val adaptationEndsOn: LocalDate? =
        firstWorkoutDate?.plusDays(ANNUAL_PROGRESSION_ADAPTATION_DAYS.toLong())

    val daysCollected: Long =
        firstWorkoutDate
            ?.let { ChronoUnit.DAYS.between(it, referenceDate).coerceAtLeast(0) + 1 }
            ?: 0L

    val remainingAdaptationDays: Long =
        adaptationEndsOn
            ?.let { ChronoUnit.DAYS.between(referenceDate, it).coerceAtLeast(0) }
            ?: ANNUAL_PROGRESSION_ADAPTATION_DAYS.toLong()

    val isInitialDataCollection: Boolean =
        adaptationEndsOn?.let { referenceDate.isBefore(it) } ?: true

    fun toPromptBlock(): String =
        if (isInitialDataCollection) {
            """
            Стан річної прогресії:
            - Фаза: перші 14 днів збору базових даних.
            - Зібрано: ${daysCollected.coerceAtMost(ANNUAL_PROGRESSION_ADAPTATION_DAYS.toLong())}/$ANNUAL_PROGRESSION_ADAPTATION_DAYS днів.
            - До фінального графіка M0-M12: $remainingAdaptationDays дн.
            - Графік річної прогресії ще не оцінюється і не згадується як проблема.
            - Правило тону: тільки похвала, підтримка і м'яке заохочення. Без критики, сорому, слів про провал, стагнацію або відставання.
            """.trimIndent()
        } else {
            """
            Стан річної прогресії:
            - Фаза: базові 14 днів завершено.
            - Зібрано: $daysCollected днів стартових даних.
            - Якщо в даних є M0-M12, оцінюй прогрес відносно цих цілей.
            - Якщо M0-M12 ще немає, природно скажи, що вже можна сформувати річний графік на 12 місяців з цілями на кожен місяць. Не критикуй користувача за відсутність графіка.
            """.trimIndent()
        }
}
