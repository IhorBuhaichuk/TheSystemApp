package com.ihor.thesystem.domain.model

import java.time.LocalDate

enum class CalendarDayCompletionStatus {
    COMPLETED,
    PARTIAL,
    MISSED,
    PLANNED,
    NO_DATA
}

data class CalendarWeekDay(
    val date: LocalDate,
    val calendarDay: CalendarCycleDay,
    val trainingCycleDay: Int,
    val hasTraining: Boolean,
    val completionStatus: CalendarDayCompletionStatus,
    val completedTasks: Int,
    val totalTasks: Int,
    val isToday: Boolean
)
