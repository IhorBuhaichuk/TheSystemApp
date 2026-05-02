package com.ihor.thesystem.domain.model

import java.time.LocalDate

enum class CalendarCycleDayType {
    WORK,
    NIGHT,
    RECOVERY,
    OFF,
    CUSTOM;

    val isWorkDay: Boolean
        get() = this == WORK || this == NIGHT
}

enum class CalendarCycleTemplate {
    FIVE_TWO,
    DAY_NIGHT_RECOVERY_OFF,
    TWO_TWO,
    CUSTOM
}

data class CalendarCycleDay(
    val index: Int,
    val name: String,
    val type: CalendarCycleDayType
)

data class CalendarCycle(
    val id: Int = 1,
    val name: String,
    val startEpochDay: Long,
    val repeats: Boolean = true,
    val template: CalendarCycleTemplate = CalendarCycleTemplate.FIVE_TWO,
    val days: List<CalendarCycleDay>
) {
    init {
        require(days.isNotEmpty()) { "Calendar cycle must contain at least one day." }
        require(days.map { it.index }.sorted() == (1..days.size).toList()) {
            "Calendar cycle day indices must be sequential."
        }
    }

    fun dayFor(date: LocalDate): CalendarCycleDay {
        dayForOrNull(date)?.let { return it }
        if (date.toEpochDay() < startEpochDay) return days.first()
        return days.last()
    }

    fun dayForOrNull(date: LocalDate): CalendarCycleDay? {
        val dateEpochDay = date.toEpochDay()
        if (!repeats && dateEpochDay !in startEpochDay until (startEpochDay + days.size)) {
            return null
        }
        val offset = (date.toEpochDay() - startEpochDay).coerceAtLeast(0)
        val index = (offset % days.size).toInt()
        return days[index]
    }

    companion object {
        fun default(startEpochDay: Long): CalendarCycle =
            fromTemplate(CalendarCycleTemplate.FIVE_TWO, startEpochDay)

        fun fromTemplate(
            template: CalendarCycleTemplate,
            startEpochDay: Long,
            customDays: List<CalendarCycleDay> = emptyList()
        ): CalendarCycle {
            val days = when (template) {
                CalendarCycleTemplate.FIVE_TWO -> listOf(
                    CalendarCycleDay(1, "Робочий день", CalendarCycleDayType.WORK),
                    CalendarCycleDay(2, "Робочий день", CalendarCycleDayType.WORK),
                    CalendarCycleDay(3, "Робочий день", CalendarCycleDayType.WORK),
                    CalendarCycleDay(4, "Робочий день", CalendarCycleDayType.WORK),
                    CalendarCycleDay(5, "Робочий день", CalendarCycleDayType.WORK),
                    CalendarCycleDay(6, "Вихідний", CalendarCycleDayType.OFF),
                    CalendarCycleDay(7, "Вихідний", CalendarCycleDayType.OFF)
                )
                CalendarCycleTemplate.DAY_NIGHT_RECOVERY_OFF -> listOf(
                    CalendarCycleDay(1, "День", CalendarCycleDayType.WORK),
                    CalendarCycleDay(2, "Ніч", CalendarCycleDayType.NIGHT),
                    CalendarCycleDay(3, "Відсипний", CalendarCycleDayType.RECOVERY),
                    CalendarCycleDay(4, "Вихідний", CalendarCycleDayType.OFF)
                )
                CalendarCycleTemplate.TWO_TWO -> listOf(
                    CalendarCycleDay(1, "Робочий день", CalendarCycleDayType.WORK),
                    CalendarCycleDay(2, "Робочий день", CalendarCycleDayType.WORK),
                    CalendarCycleDay(3, "Вихідний", CalendarCycleDayType.OFF),
                    CalendarCycleDay(4, "Вихідний", CalendarCycleDayType.OFF)
                )
                CalendarCycleTemplate.CUSTOM -> customDays.ifEmpty {
                    listOf(CalendarCycleDay(1, "Власний день", CalendarCycleDayType.CUSTOM))
                }
            }

            return CalendarCycle(
                name = template.title,
                startEpochDay = startEpochDay,
                repeats = true,
                template = template,
                days = days
            )
        }
    }
}

val CalendarCycleTemplate.title: String
    get() = when (this) {
        CalendarCycleTemplate.FIVE_TWO -> "5/2"
        CalendarCycleTemplate.DAY_NIGHT_RECOVERY_OFF -> "День / Ніч / Відсипний / Вихідний"
        CalendarCycleTemplate.TWO_TWO -> "2/2"
        CalendarCycleTemplate.CUSTOM -> "Власний цикл"
    }
