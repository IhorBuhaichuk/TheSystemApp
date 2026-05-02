package com.ihor.thesystem.domain.repository

import com.ihor.thesystem.domain.model.CalendarCycle
import com.ihor.thesystem.domain.model.CalendarCycleTemplate
import kotlinx.coroutines.flow.Flow

interface CalendarCycleRepository {
    fun getCalendarCycle(): Flow<CalendarCycle>
    suspend fun saveCalendarCycle(cycle: CalendarCycle)
    suspend fun applyTemplate(template: CalendarCycleTemplate, startEpochDay: Long)
}
