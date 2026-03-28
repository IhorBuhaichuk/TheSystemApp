package com.ihor.thesystem.domain.usecase

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class CalculateCycleDayForDateUseCase @Inject constructor() {
    /**
     * Математично точний розрахунок дня 4-денного мікроциклу.
     */
    operator fun invoke(targetDate: LocalDate, anchorEpochDay: Long, anchorCycleDay: Int): Int {
        if (anchorEpochDay == 0L) return 1
        
        val anchorDate = LocalDate.ofEpochDay(anchorEpochDay)
        val daysBetween = ChronoUnit.DAYS.between(anchorDate, targetDate)
        
        // Безпечний modulo для від'ємних значень (дати до якоря)
        val offset = (daysBetween % 4 + 4) % 4
        
        return ((anchorCycleDay - 1 + offset) % 4 + 1).toInt()
    }
}
