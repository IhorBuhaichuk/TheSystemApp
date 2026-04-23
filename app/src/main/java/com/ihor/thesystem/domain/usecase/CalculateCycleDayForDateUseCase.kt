package com.ihor.thesystem.domain.usecase

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class CalculateCycleDayForDateUseCase @Inject constructor() {
    /**
     * Розрахунок дня циклу на основі дати.
     * 
     * @param targetDate Цільова дата
     * @param anchorEpochDay Дата прив'язки (Epoch Day)
     * @param anchorCycleDay Який це був день циклу (1..4)
     * @param cycleDaysPerMicrocycle Кількість днів у мікроциклі
     */
    operator fun invoke(
        targetDate: LocalDate,
        anchorEpochDay: Long,
        anchorCycleDay: Int,
        cycleDaysPerMicrocycle: Int = 4
    ): Int {
        val anchorDate = LocalDate.ofEpochDay(anchorEpochDay)
        val daysBetween = ChronoUnit.DAYS.between(anchorDate, targetDate)
        
        val cycleDays = cycleDaysPerMicrocycle.coerceAtLeast(1)
        
        // (daysBetween % cycleDays + cycleDays) % cycleDays гарантує коректний результат для минулих дат
        val offset = (daysBetween % cycleDays + cycleDays) % cycleDays
        
        return ((anchorCycleDay - 1 + offset) % cycleDays + 1).toInt()
    }

    /**
     * Математично точний розрахунок дня мікроциклу з урахуванням зміщення для нічних змін.
     * 
     * @param targetInstant Точний момент часу
     * @param zoneId Часовий пояс
     * @param dayOffsetHours Година, до якої час вважається "попереднім днем" (напр. 4 для 04:00)
     * @param anchorEpochDay Дата прив'язки (Epoch Day)
     * @param anchorCycleDay Який це був день циклу (1..4)
     */
    operator fun invoke(
        targetInstant: Instant,
        zoneId: ZoneId = ZoneId.systemDefault(),
        dayOffsetHours: Int = 0,
        anchorEpochDay: Long,
        anchorCycleDay: Int,
        cycleDaysPerMicrocycle: Int = 4
    ): Int {
        // 1. Враховуємо "логічну добу" шляхом віднімання годин зміщення.
        val adjustedZonedDateTime = ZonedDateTime.ofInstant(targetInstant, zoneId)
            .minusHours(dayOffsetHours.toLong())
            
        return invoke(
            targetDate = adjustedZonedDateTime.toLocalDate(),
            anchorEpochDay = anchorEpochDay,
            anchorCycleDay = anchorCycleDay,
            cycleDaysPerMicrocycle = cycleDaysPerMicrocycle
        )
    }
}
