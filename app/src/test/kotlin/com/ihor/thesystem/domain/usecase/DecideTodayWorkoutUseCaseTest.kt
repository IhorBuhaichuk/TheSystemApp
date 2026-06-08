package com.ihor.thesystem.domain.usecase

import com.ihor.thesystem.domain.model.CalendarCycle
import com.ihor.thesystem.domain.model.CalendarCycleDay
import com.ihor.thesystem.domain.model.CalendarCycleDayType
import com.ihor.thesystem.domain.model.DomainQuestStatus
import com.ihor.thesystem.domain.model.DomainQuestType
import com.ihor.thesystem.domain.model.Quest
import com.ihor.thesystem.domain.model.ReadinessEntry
import com.ihor.thesystem.domain.model.ReadinessInput
import com.ihor.thesystem.domain.model.ReadinessLevel
import com.ihor.thesystem.domain.model.RecoveryDebtLevel
import com.ihor.thesystem.domain.model.ScheduleDay
import com.ihor.thesystem.domain.model.SystemConfig
import com.ihor.thesystem.domain.model.TodayTrainingDecisionType
import com.ihor.thesystem.domain.model.WorkoutLog
import com.ihor.thesystem.domain.model.WorkoutSession
import com.ihor.thesystem.domain.repository.CalendarCycleRepository
import com.ihor.thesystem.domain.repository.PlayerRepository
import com.ihor.thesystem.domain.repository.QuestRepository
import com.ihor.thesystem.domain.repository.ReadinessRepository
import com.ihor.thesystem.domain.repository.ScheduleRepository
import com.ihor.thesystem.domain.repository.SystemConfigRepository
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import com.ihor.thesystem.domain.util.AppClock
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class DecideTodayWorkoutUseCaseTest {

    private val configRepository: SystemConfigRepository = mockk()
    private val playerRepository: PlayerRepository = mockk()
    private val scheduleRepository: ScheduleRepository = mockk()
    private val readinessRepository: ReadinessRepository = mockk()
    private val workoutAnalyticsRepository: WorkoutAnalyticsRepository = mockk()
    private val questRepository: QuestRepository = mockk()
    private val calendarCycleRepository: CalendarCycleRepository = mockk()
    private val clock = FixedClock(TODAY)

    private val useCase = DecideTodayWorkoutUseCase(
        configRepository = configRepository,
        playerRepository = playerRepository,
        scheduleRepository = scheduleRepository,
        readinessRepository = readinessRepository,
        workoutAnalyticsRepository = workoutAnalyticsRepository,
        questRepository = questRepository,
        calendarCycleRepository = calendarCycleRepository,
        resolveTrainingCycleDay = ResolveTrainingCycleDayUseCase(
            calculateCycleDay = CalculateCycleDayForDateUseCase(),
            clock = clock
        ),
        calculateReadiness = CalculateReadinessUseCase(),
        calculateRecoveryDebt = CalculateRecoveryDebtUseCase(),
        clock = clock
    )

    @Test
    fun `high readiness and low debt allow progression`() = runTest {
        arrange(
            schedule = workoutSchedule(),
            readinessEntry = readinessEntry(score = 90, level = ReadinessLevel.PROGRESS),
            logs = emptyList()
        )

        val decision = useCase(TODAY)

        assertEquals(TodayTrainingDecisionType.PROGRESS_ALLOWED, decision.decisionType)
        assertEquals(1.025f, decision.loadMultiplier)
        assertEquals(1.0f, decision.volumeMultiplier)
        assertTrue(decision.isTrainingAllowed)
    }

    @Test
    fun `low readiness switches planned workout to active recovery`() = runTest {
        arrange(
            schedule = workoutSchedule(),
            readinessEntry = readinessEntry(
                score = 35,
                level = ReadinessLevel.RECOVERY,
                input = ReadinessInput(sleepHours = 4f, stress = 5, soreness = 5)
            ),
            logs = emptyList()
        )

        val decision = useCase(TODAY)

        assertEquals(TodayTrainingDecisionType.ACTIVE_RECOVERY, decision.decisionType)
        assertFalse(decision.isTrainingAllowed)
        assertEquals(ReadinessLevel.RECOVERY, decision.readinessLevel)
    }

    @Test
    fun `reduced readiness creates no excuse protocol instead of progression workout`() = runTest {
        arrange(
            schedule = workoutSchedule(),
            readinessEntry = readinessEntry(
                score = 58,
                level = ReadinessLevel.REDUCED,
                input = ReadinessInput(sleepHours = 5.5f, stress = 4)
            ),
            logs = emptyList()
        )

        val decision = useCase(TODAY)

        assertEquals(TodayTrainingDecisionType.NO_EXCUSE, decision.decisionType)
        assertTrue(decision.isTrainingAllowed)
        assertEquals(0.0f, decision.loadMultiplier)
        assertTrue(decision.reason.contains("short bodyweight protocol"))
    }

    @Test
    fun `recovery readiness creates active recovery even when schedule has no workout`() = runTest {
        arrange(
            schedule = restSchedule(),
            readinessEntry = readinessEntry(
                score = 35,
                level = ReadinessLevel.RECOVERY,
                input = ReadinessInput(sleepHours = 4f, stress = 5, soreness = 5)
            ),
            logs = emptyList()
        )

        val decision = useCase(TODAY)

        assertEquals(TodayTrainingDecisionType.ACTIVE_RECOVERY, decision.decisionType)
        assertFalse(decision.isTrainingAllowed)
    }

    @Test
    fun `no workout in schedule returns rest`() = runTest {
        arrange(
            schedule = restSchedule(),
            readinessEntry = readinessEntry(score = 92, level = ReadinessLevel.PROGRESS),
            logs = emptyList()
        )

        val decision = useCase(TODAY)

        assertEquals(TodayTrainingDecisionType.REST, decision.decisionType)
        assertEquals(0f, decision.loadMultiplier)
        assertEquals(0f, decision.volumeMultiplier)
        assertFalse(decision.isTrainingAllowed)
    }

    @Test
    fun `high recovery debt returns deload before standard training`() = runTest {
        arrange(
            schedule = workoutSchedule(),
            readinessEntry = readinessEntry(score = 72, level = ReadinessLevel.STANDARD),
            logs = listOf(
                workoutLog(date = TODAY.minusDays(1), tonnage = 5_000.0),
                workoutLog(date = TODAY.minusDays(2), tonnage = 5_000.0),
                workoutLog(date = TODAY.minusDays(3), tonnage = 5_000.0),
                workoutLog(date = TODAY.minusDays(4), tonnage = 5_000.0)
            )
        )

        val decision = useCase(TODAY)

        assertEquals(TodayTrainingDecisionType.DELOAD, decision.decisionType)
        assertEquals(0.8f, decision.loadMultiplier)
        assertEquals(0.6f, decision.volumeMultiplier)
        assertTrue(decision.recoveryDebt.value in 50..74)
    }

    @Test
    fun `critical recovery debt blocks hard training even when readiness is only reduced`() = runTest {
        arrange(
            schedule = workoutSchedule(),
            readinessEntry = readinessEntry(
                score = 58,
                level = ReadinessLevel.REDUCED,
                input = ReadinessInput(stress = 5, soreness = 5)
            ),
            logs = listOf(
                workoutLog(date = TODAY.minusDays(1), tonnage = 12_000.0),
                workoutLog(date = TODAY.minusDays(2), tonnage = 12_000.0),
                workoutLog(date = TODAY.minusDays(3), tonnage = 12_000.0),
                workoutLog(date = TODAY.minusDays(4), tonnage = 12_000.0)
            )
        )

        val decision = useCase(TODAY)

        assertEquals(TodayTrainingDecisionType.ACTIVE_RECOVERY, decision.decisionType)
        assertEquals(ReadinessLevel.REDUCED, decision.readinessLevel)
        assertEquals(RecoveryDebtLevel.CRITICAL, decision.recoveryDebt.level)
        assertFalse(decision.isTrainingAllowed)
    }

    @Test
    fun `readiness score boundaries map to deterministic workout decisions`() = runTest {
        val cases = listOf(
            Triple(64, ReadinessLevel.REDUCED, TodayTrainingDecisionType.NO_EXCUSE),
            Triple(65, ReadinessLevel.STANDARD, TodayTrainingDecisionType.STANDARD_TRAINING),
            Triple(84, ReadinessLevel.STANDARD, TodayTrainingDecisionType.STANDARD_TRAINING),
            Triple(85, ReadinessLevel.PROGRESS, TodayTrainingDecisionType.PROGRESS_ALLOWED)
        )

        cases.forEach { (score, level, expectedDecision) ->
            arrange(
                schedule = workoutSchedule(),
                readinessEntry = readinessEntry(score = score, level = level),
                logs = emptyList()
            )

            val decision = useCase(TODAY)

            assertEquals("score $score", expectedDecision, decision.decisionType)
            assertEquals("score $score", level, decision.readinessLevel)
        }
    }

    @Test
    fun `missing readiness entry uses neutral fallback`() = runTest {
        arrange(
            schedule = workoutSchedule(),
            readinessEntry = null,
            latestReadinessEntries = emptyList(),
            logs = emptyList()
        )

        val decision = useCase(TODAY)

        assertEquals(TodayTrainingDecisionType.STANDARD_TRAINING, decision.decisionType)
        assertEquals(70, decision.readinessScore)
        assertEquals(ReadinessLevel.STANDARD, decision.readinessLevel)
        assertTrue(decision.warnings.any { it.contains("neutral fallback") })
    }

    @Test
    fun `calendar recovery day overrides workout schedule`() = runTest {
        arrange(
            schedule = workoutSchedule(),
            readinessEntry = readinessEntry(score = 85, level = ReadinessLevel.PROGRESS),
            logs = emptyList(),
            calendarDayType = CalendarCycleDayType.RECOVERY
        )

        val decision = useCase(TODAY)

        assertEquals(TodayTrainingDecisionType.ACTIVE_RECOVERY, decision.decisionType)
        assertFalse(decision.isTrainingAllowed)
        assertTrue(decision.warnings.any { it.contains("recovery") })
    }

    @Test
    fun `calendar off day overrides workout schedule`() = runTest {
        arrange(
            schedule = workoutSchedule(),
            readinessEntry = readinessEntry(score = 85, level = ReadinessLevel.PROGRESS),
            logs = emptyList(),
            calendarDayType = CalendarCycleDayType.OFF
        )

        val decision = useCase(TODAY)

        assertEquals(TodayTrainingDecisionType.REST, decision.decisionType)
        assertFalse(decision.isTrainingAllowed)
        assertTrue(decision.warnings.any { it.contains("off") })
    }

    @Test
    fun `missed planned workout with acceptable recovery returns no excuse`() = runTest {
        arrange(
            schedule = workoutSchedule(),
            readinessEntry = readinessEntry(score = 75, level = ReadinessLevel.STANDARD),
            logs = listOf(
                workoutLog(date = TODAY.minusDays(10), tonnage = 2_000.0)
            ),
            activeMainQuest = mainQuest()
        )

        val decision = useCase(TODAY)

        assertEquals(TodayTrainingDecisionType.NO_EXCUSE, decision.decisionType)
        assertTrue(decision.isTrainingAllowed)
        assertEquals(1.0f, decision.loadMultiplier)
    }

    @Test
    fun `rest day wins over missed workout debt`() = runTest {
        arrange(
            schedule = restSchedule(),
            schedulesForDays = listOf(
                restSchedule(cycleDay = 1),
                workoutSchedule(cycleDay = 4)
            ),
            readinessEntry = readinessEntry(score = 75, level = ReadinessLevel.STANDARD),
            logs = listOf(workoutLog(date = TODAY.minusDays(10), tonnage = 2_000.0)),
            activeMainQuest = mainQuest()
        )

        val decision = useCase(TODAY)

        assertEquals(TodayTrainingDecisionType.REST, decision.decisionType)
        assertFalse(decision.isTrainingAllowed)
        assertTrue(decision.recoveryDebt.reasons.any { it == "Missed planned workouts 2: +24" })
    }

    @Test
    fun `missing active main quest warns but does not block standard workout`() = runTest {
        arrange(
            schedule = workoutSchedule(),
            readinessEntry = readinessEntry(score = 75, level = ReadinessLevel.STANDARD),
            logs = emptyList(),
            activeMainQuest = null
        )

        val decision = useCase(TODAY)

        assertEquals(TodayTrainingDecisionType.STANDARD_TRAINING, decision.decisionType)
        assertTrue(decision.isTrainingAllowed)
        assertTrue(decision.warnings.any { it.contains("No active main quest") })
    }

    private fun arrange(
        schedule: ScheduleDay,
        readinessEntry: ReadinessEntry?,
        latestReadinessEntries: List<ReadinessEntry> = emptyList(),
        logs: List<WorkoutLog>,
        activeMainQuest: Quest? = null,
        schedulesForDays: List<ScheduleDay> = listOf(schedule),
        calendarDayType: CalendarCycleDayType = CalendarCycleDayType.WORK
    ) {
        every { configRepository.getConfigFlow() } returns flowOf(
            SystemConfig(
                cycleAnchorDateTimestamp = TODAY.toEpochDay(),
                cycleAnchorDay = 1,
                cycleDaysPerMicrocycle = 4
            )
        )
        every { playerRepository.getPlayer() } returns flowOf(null)
        every { scheduleRepository.getScheduleForDay(1) } returns flowOf(schedule)
        every { scheduleRepository.getSchedulesForDays(any()) } returns flowOf(schedulesForDays)
        coEvery { readinessRepository.getEntryForDate(TODAY.toEpochDay()) } returns readinessEntry
        coEvery { readinessRepository.getEntriesBetween(any(), any()) } returns latestReadinessEntries
        every { workoutAnalyticsRepository.getAllLogs() } returns flowOf(logs)
        every { questRepository.getActiveMainQuest() } returns flowOf(activeMainQuest)
        every { calendarCycleRepository.getCalendarCycle() } returns flowOf(
            CalendarCycle(
                name = "Work cycle",
                startEpochDay = TODAY.toEpochDay(),
                days = listOf(CalendarCycleDay(index = 1, name = "Work", type = calendarDayType))
            )
        )
    }

    private fun workoutSchedule(cycleDay: Int = 1): ScheduleDay =
        ScheduleDay(
            id = cycleDay,
            cycleDay = cycleDay,
            workoutTemplateId = 10,
            workoutTemplateName = "Upper Day",
            dailyTaskNames = emptyList(),
            exercises = emptyList()
        )

    private fun restSchedule(cycleDay: Int = 1): ScheduleDay =
        ScheduleDay(
            id = cycleDay,
            cycleDay = cycleDay,
            workoutTemplateId = null,
            workoutTemplateName = null,
            dailyTaskNames = emptyList(),
            exercises = emptyList()
        )

    private fun readinessEntry(
        score: Int,
        level: ReadinessLevel,
        input: ReadinessInput = ReadinessInput(sleepHours = 7.5f, energy = 4)
    ): ReadinessEntry =
        ReadinessEntry(
            id = 1L,
            dateEpochDay = TODAY.toEpochDay(),
            input = input,
            score = score,
            level = level,
            createdAtMillis = clock.now()
        )

    private fun workoutLog(date: LocalDate, tonnage: Double): WorkoutLog =
        WorkoutLog(
            session = WorkoutSession(
                sessionId = date.toEpochDay(),
                questId = 1L,
                timestamp = date.atStartOfDay(TEST_ZONE).toInstant().toEpochMilli(),
                totalTonnage = tonnage,
                cycleDay = 1
            ),
            sets = emptyList()
        )

    private fun mainQuest(): Quest =
        Quest(
            id = 1,
            title = "Main quest",
            type = DomainQuestType.MAIN,
            date = clock.now(),
            status = DomainQuestStatus.ACTIVE,
            tasks = emptyList(),
            scheduleId = 1
        )

    private class FixedClock(private val today: LocalDate) : AppClock {
        override fun now(): Long =
            today.atStartOfDay(TEST_ZONE).toInstant().toEpochMilli()

        override fun zoneId(): ZoneId = TEST_ZONE
    }

    private companion object {
        val TODAY: LocalDate = LocalDate.of(2026, 5, 11)
        val TEST_ZONE: ZoneId = ZoneId.of("UTC")
    }
}
