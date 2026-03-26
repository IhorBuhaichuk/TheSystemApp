package com.ihor.thesystem.domain.usecase

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class CalculateCycleDayForDateUseCase @Inject constructor() {
    /**
     * @param targetDate Дата, для якої рахуємо день
     * @param anchorTimestamp Таймстемп Дня 1 (перша зміна)
     * @return 1 - День, 2 - Ніч, 3 - Комплекс А, 4 - Комплекс Б
     */
    operator fun invoke(targetDate: LocalDate, anchorTimestamp: Long): Int {
        if (anchorTimestamp == 0L) return 1
        
        val anchorDate = Instant.ofEpochMilli(anchorTimestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        
        val daysDifference = ChronoUnit.DAYS.between(anchorDate, targetDate)
        
        // Математично коректний modulo для від'ємних значень
        val cycleDay = ((daysDifference % 4) + 4) % 4 + 1
        return cycleDay.toInt()
    }
}
