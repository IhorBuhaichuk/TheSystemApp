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
    operator fun invoke(targetDate: LocalDate, anchorEpochDay: Long, anchorCycleDay: Int): Int {
        val anchorDate = LocalDate.ofEpochDay(anchorEpochDay)
        val daysBetween = ChronoUnit.DAYS.between(anchorDate, targetDate)
        
        // (daysBetween % 4 + 4) % 4 гарантує додатний зсув навіть для минулих дат
        val offset = (daysBetween % 4 + 4) % 4
        
        return ((anchorCycleDay - 1 + offset) % 4 + 1).toInt()
    }
}
