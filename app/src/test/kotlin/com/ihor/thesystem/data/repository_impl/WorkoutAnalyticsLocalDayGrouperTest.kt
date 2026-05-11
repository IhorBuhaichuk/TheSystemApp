package com.ihor.thesystem.data.repository_impl

import com.ihor.thesystem.domain.model.WeightHistoryEntry
import com.ihor.thesystem.domain.repository.DailyTonnageStats
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class WorkoutAnalyticsLocalDayGrouperTest {

    @Test
    fun `tonnage near UTC midnight groups by user local day`() {
        val zone = ZoneId.of("Europe/Bucharest")
        val rows = listOf(
            DailyTonnageStats(
                dateUnixTimestamp = Instant.parse("2026-05-10T21:30:00Z").toEpochMilli(),
                totalTonnage = 100.0
            ),
            DailyTonnageStats(
                dateUnixTimestamp = Instant.parse("2026-05-11T20:00:00Z").toEpochMilli(),
                totalTonnage = 150.0
            )
        )

        val result = WorkoutAnalyticsLocalDayGrouper.groupTonnageByLocalDay(rows, zone)

        assertEquals(1, result.size)
        assertEquals(250.0, result.single().totalTonnage, 0.0)
        assertEquals(
            LocalDate.of(2026, 5, 11),
            Instant.ofEpochMilli(result.single().dateUnixTimestamp).atZone(zone).toLocalDate()
        )
    }

    @Test
    fun `daily max weight history uses local day boundary`() {
        val zone = ZoneId.of("Europe/Bucharest")
        val rows = listOf(
            WeightHistoryEntry(
                weight = 80.0,
                timestamp = Instant.parse("2026-05-10T21:30:00Z").toEpochMilli()
            ),
            WeightHistoryEntry(
                weight = 85.0,
                timestamp = Instant.parse("2026-05-11T20:00:00Z").toEpochMilli()
            )
        )

        val result = WorkoutAnalyticsLocalDayGrouper.dailyMaxWeightHistory(rows, zone)

        assertEquals(1, result.size)
        assertEquals(85.0, result.single().weight, 0.0)
        assertEquals(
            LocalDate.of(2026, 5, 11),
            Instant.ofEpochMilli(result.single().timestamp).atZone(zone).toLocalDate()
        )
    }
}
