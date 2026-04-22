package com.ihor.thesystem.domain.usecase

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class CalculateCycleDayForDateUseCase @Inject constructor() {
    /**
     * Математично точний розрахунок дня 4-денного мікроциклу.
     * @param targetDate Дата, для якої рахуємо день
     * @param anchorEpochDay Дата прив'язки (Epoch Day)
     * @param anchorCycleDay Який це був день циклу (1..4)
     */
    operator fun invoke(targetDate: LocalDate, anchorEpochDay: Long, anchorCycleDay: Int, cycleDaysPerMicrocycle: Int = 4): Int {
        val anchorDate = LocalDate.ofEpochDay(anchorEpochDay)
        val daysBetween = ChronoUnit.DAYS.between(anchorDate, targetDate)
        
        val cycleDays = cycleDaysPerMicrocycle.coerceAtLeast(1)
        // (daysBetween % cycleDays + cycleDays) % cycleDays гарантує додатний зсув навіть для минулих дат
        val offset = (daysBetween % cycleDays + cycleDays) % cycleDays
        
        return ((anchorCycleDay - 1 + offset) % cycleDays + 1).toInt()
    }
}
