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
        // Захист від некоректних або порожніх даних
        val safeAnchorEpochDay = if (anchorEpochDay <= 0L || anchorEpochDay > 300000L) {
            LocalDate.now().toEpochDay()
        } else {
            anchorEpochDay
        }

        val anchorDate = LocalDate.ofEpochDay(safeAnchorEpochDay)
        val daysBetween = ChronoUnit.DAYS.between(anchorDate, targetDate)
        
        // Безпечний modulo для від'ємних значень (дати до якоря)
        val offset = (daysBetween % 4 + 4) % 4
        
        return ((anchorCycleDay - 1 + offset) % 4 + 1).toInt()
    }
}
