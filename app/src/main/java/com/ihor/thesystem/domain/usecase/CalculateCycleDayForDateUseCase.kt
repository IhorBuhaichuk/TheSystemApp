package com.ihor.thesystem.domain.usecase

import java.time.LocalDate
import javax.inject.Inject

class CalculateCycleDayForDateUseCase @Inject constructor() {
    /**
     * @param targetDate Дата, для якої рахуємо день
     * @param anchorEpochDay Дата прив'язки (Epoch Day)
     * @param anchorCycleDay Який це був день циклу (1..4)
     * @return 1 - День, 2 - Ніч, 3 - Відсипний, 4 - Вихідний
     */
    operator fun invoke(targetDate: LocalDate, anchorEpochDay: Long, anchorCycleDay: Int): Int {
        if (anchorEpochDay == 0L) return 1
        
        val daysDifference = targetDate.toEpochDay() - anchorEpochDay
        
        // Розрахунок зміщення відносно "Дня 1"
        // (anchorCycleDay - 1) - це номер дня в 0-індексній системі (0..3)
        val cycleDay = ((daysDifference + (anchorCycleDay - 1)) % 4 + 4) % 4 + 1
        
        return cycleDay.toInt()
    }
}
