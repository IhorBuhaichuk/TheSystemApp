package com.ihor.thesystem.feature.calendar.viewmodel

import com.ihor.thesystem.R
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.domain.util.AppClock
import com.ihor.thesystem.core.util.DispatcherProvider
import com.ihor.thesystem.domain.model.CalendarCycle
import com.ihor.thesystem.domain.model.CalendarCycleDay
import com.ihor.thesystem.domain.model.CalendarCycleDayType
import com.ihor.thesystem.domain.model.CalendarCycleTemplate
import com.ihor.thesystem.domain.repository.CalendarCycleRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarSettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = CalendarMainDispatcherRule()

    @Test
    fun `invalid start date maps to localized error`() {
        val viewModel = viewModel()
        mainDispatcherRule.advanceUntilIdle()

        viewModel.onStartDateChanged("02-05-2026")
        viewModel.onSaveCycle()

        val error = viewModel.uiState.value.errorMessage as UiText.StringResource
        assertEquals(R.string.calendar_error_invalid_start_date, error.resId)
    }

    @Test
    fun `save failure clears saving and maps to localized error`() {
        val repository = FakeCalendarCycleRepository(defaultCycle()).apply {
            failSave = true
        }
        val viewModel = viewModel(repository)
        mainDispatcherRule.advanceUntilIdle()

        viewModel.onSaveCycle()
        mainDispatcherRule.advanceUntilIdle()

        val error = viewModel.uiState.value.errorMessage as UiText.StringResource
        assertFalse(viewModel.uiState.value.isSaving)
        assertEquals(R.string.calendar_error_save_failed, error.resId)
    }

    private fun viewModel(
        repository: FakeCalendarCycleRepository = FakeCalendarCycleRepository(defaultCycle())
    ): CalendarSettingsViewModel =
        CalendarSettingsViewModel(
            calendarCycleRepository = repository,
            clock = FixedClock(LocalDate.of(2026, 5, 2)),
            dispatchers = CalendarTestDispatcherProvider(mainDispatcherRule.dispatcher)
        )

    private fun defaultCycle(): CalendarCycle =
        CalendarCycle(
            id = 1,
            name = "5/2",
            startEpochDay = LocalDate.of(2026, 5, 2).toEpochDay(),
            repeats = true,
            template = CalendarCycleTemplate.FIVE_TWO,
            days = listOf(
                CalendarCycleDay(index = 1, name = "Work", type = CalendarCycleDayType.WORK)
            )
        )
}

private class FakeCalendarCycleRepository(
    initialCycle: CalendarCycle
) : CalendarCycleRepository {
    private val cycle = MutableStateFlow(initialCycle)
    var failSave: Boolean = false
    var savedCycle: CalendarCycle? = null

    override fun getCalendarCycle(): Flow<CalendarCycle> = cycle

    override suspend fun saveCalendarCycle(cycle: CalendarCycle) {
        if (failSave) throw IllegalStateException("save failed")
        savedCycle = cycle
        this.cycle.value = cycle
    }

    override suspend fun applyTemplate(template: CalendarCycleTemplate, startEpochDay: Long) {
        saveCalendarCycle(CalendarCycle.fromTemplate(template, startEpochDay))
    }
}

private class FixedClock(
    private val date: LocalDate
) : AppClock {
    override fun now(): Long =
        date.atStartOfDay(zoneId()).toInstant().toEpochMilli()

    override fun zoneId(): ZoneId = ZoneId.of("Europe/Kyiv")
}

private class CalendarTestDispatcherProvider(
    private val dispatcher: CoroutineDispatcher
) : DispatcherProvider {
    override val main: CoroutineDispatcher = dispatcher
    override val io: CoroutineDispatcher = dispatcher
    override val default: CoroutineDispatcher = dispatcher
    override val mainImmediate: CoroutineDispatcher = dispatcher
}

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarMainDispatcherRule(
    val dispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }

    fun advanceUntilIdle() {
        dispatcher.scheduler.advanceUntilIdle()
    }
}
