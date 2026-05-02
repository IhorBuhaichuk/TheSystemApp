package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.core.util.AppClock
import com.ihor.thesystem.data.local.room.dao.CalendarCycleDao
import com.ihor.thesystem.data.local.room.entity.CalendarCycleConfigEntity
import com.ihor.thesystem.data.local.room.entity.CalendarCycleDayEntity
import com.ihor.thesystem.domain.model.CalendarCycle
import com.ihor.thesystem.domain.model.CalendarCycleDay
import com.ihor.thesystem.domain.model.CalendarCycleDayType
import com.ihor.thesystem.domain.model.CalendarCycleTemplate
import com.ihor.thesystem.domain.repository.CalendarCycleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import java.time.DayOfWeek
import java.time.Instant
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

class CalendarCycleRepositoryImpl @Inject constructor(
    private val dao: CalendarCycleDao,
    private val clock: AppClock
) : CalendarCycleRepository {

    override fun getCalendarCycle(): Flow<CalendarCycle> = flow {
        ensureDefaultCycle()
        emitAll(
            combine(dao.getConfigFlow(), dao.getDaysFlow()) { config, days ->
                if (config == null || days.isEmpty()) {
                    defaultCycle()
                } else {
                    config.toDomain(days)
                }
            }
        )
    }

    override suspend fun saveCalendarCycle(cycle: CalendarCycle) {
        dao.replaceCycle(cycle.toConfigEntity(), cycle.days.map { it.toEntity(cycle.id) })
    }

    override suspend fun applyTemplate(template: CalendarCycleTemplate, startEpochDay: Long) {
        saveCalendarCycle(CalendarCycle.fromTemplate(template, startEpochDay))
    }

    private suspend fun ensureDefaultCycle() {
        val config = dao.getConfigSync()
        val days = dao.getDaysSync()
        if (config == null || days.isEmpty()) {
            saveCalendarCycle(defaultCycle())
        }
    }

    private fun defaultCycle(): CalendarCycle =
        CalendarCycle.default(defaultStartEpochDay())

    private fun defaultStartEpochDay(): Long {
        val today = Instant.ofEpochMilli(clock.now())
            .atZone(clock.zoneId())
            .toLocalDate()
        return today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toEpochDay()
    }
}

private fun CalendarCycleConfigEntity.toDomain(days: List<CalendarCycleDayEntity>): CalendarCycle =
    CalendarCycle(
        id = id,
        name = name,
        startEpochDay = startEpochDay,
        repeats = repeats,
        template = runCatching { CalendarCycleTemplate.valueOf(template) }
            .getOrDefault(CalendarCycleTemplate.CUSTOM),
        days = days.map { it.toDomain() }
    )

private fun CalendarCycleDayEntity.toDomain(): CalendarCycleDay =
    CalendarCycleDay(
        index = dayIndex,
        name = name,
        type = runCatching { CalendarCycleDayType.valueOf(type) }
            .getOrDefault(CalendarCycleDayType.CUSTOM)
    )

private fun CalendarCycle.toConfigEntity(): CalendarCycleConfigEntity =
    CalendarCycleConfigEntity(
        id = id,
        name = name,
        startEpochDay = startEpochDay,
        repeats = repeats,
        template = template.name
    )

private fun CalendarCycleDay.toEntity(cycleId: Int): CalendarCycleDayEntity =
    CalendarCycleDayEntity(
        cycleId = cycleId,
        dayIndex = index,
        name = name,
        type = type.name
    )
