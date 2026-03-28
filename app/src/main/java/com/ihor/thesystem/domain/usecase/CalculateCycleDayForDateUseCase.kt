package com.ihor.thesystem.domain.usecase

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class CalculateCycleDayForDateUseCase @Inject constructor() {
    /**
     * Математично точний розрахунок дня 4-денного циклу.
     * @param targetDate Дата, для якої рахуємо день
     * @param anchorEpochDay Дата прив'язки (Epoch Day)
     * @param anchorCycleDay Який це був день циклу (1..4) в дату прив'язки
     */
    operator fun invoke(targetDate: LocalDate, anchorEpochDay: Long, anchorCycleDay: Int): Int {
        if (anchorEpochDay == 0L) return 1
        
        val anchorDate = LocalDate.ofEpochDay(anchorEpochDay)
        val daysBetween = ChronoUnit.DAYS.between(anchorDate, targetDate)

        // Безпечний modulo для від'ємних значень (дат до якоря)
        val offset = (daysBetween % 4 + 4) % 4
        
        // Повертаємо день від 1 до 4
        return ((anchorCycleDay - 1 + offset) % 4 + 1).toInt()
    }
}
