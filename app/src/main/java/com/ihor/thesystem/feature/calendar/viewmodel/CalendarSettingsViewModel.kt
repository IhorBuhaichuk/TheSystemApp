package com.ihor.thesystem.feature.calendar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.R
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.core.util.AppClock
import com.ihor.thesystem.core.util.DispatcherProvider
import com.ihor.thesystem.domain.model.CalendarCycle
import com.ihor.thesystem.domain.model.CalendarCycleDay
import com.ihor.thesystem.domain.model.CalendarCycleDayType
import com.ihor.thesystem.domain.model.CalendarCycleTemplate
import com.ihor.thesystem.domain.model.title
import com.ihor.thesystem.domain.repository.CalendarCycleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject

data class CalendarCycleDayDraftUiModel(
    val index: Int,
    val name: String,
    val type: CalendarCycleDayType
)

data class CalendarSettingsUiState(
    val cycleId: Int = 1,
    val selectedTemplate: CalendarCycleTemplate = CalendarCycleTemplate.FIVE_TWO,
    val cycleName: String = "",
    val startDateInput: String = "",
    val repeats: Boolean = true,
    val days: List<CalendarCycleDayDraftUiModel> = emptyList(),
    val todayCycleDayIndex: Int? = null,
    val isSaving: Boolean = false,
    val isDirty: Boolean = false,
    val errorMessage: UiText? = null
) {
    val cycleLength: Int get() = days.size
}

sealed interface CalendarSettingsEvent {
    data object Saved : CalendarSettingsEvent
}

@HiltViewModel
class CalendarSettingsViewModel @Inject constructor(
    private val calendarCycleRepository: CalendarCycleRepository,
    private val clock: AppClock,
    private val dispatchers: DispatcherProvider
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    private val _uiState = MutableStateFlow(CalendarSettingsUiState())
    val uiState: StateFlow<CalendarSettingsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CalendarSettingsEvent>()
    val events: SharedFlow<CalendarSettingsEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            calendarCycleRepository.getCalendarCycle().collect { cycle ->
                _uiState.update { currentState ->
                    if (currentState.isDirty || currentState.isSaving) {
                        currentState.copy(isSaving = false)
                    } else {
                        cycle.toUiState()
                    }
                }
            }
        }
    }

    fun onTemplateSelected(template: CalendarCycleTemplate) {
        _uiState.update { state ->
            val startEpochDay = parseStartDate(state.startDateInput)?.toEpochDay() ?: todayEpochDay()
            val draftDays = if (template == CalendarCycleTemplate.CUSTOM && state.days.isNotEmpty()) {
                state.days
            } else {
                CalendarCycle.fromTemplate(template, startEpochDay).days.toDraftDays()
            }

            state.copy(
                selectedTemplate = template,
                cycleName = if (template == CalendarCycleTemplate.CUSTOM && state.cycleName.isNotBlank()) {
                    state.cycleName
                } else {
                    template.title
                },
                days = draftDays.reindex(),
                repeats = true,
                isDirty = true,
                errorMessage = null
            ).withTodayCycleDayIndex()
        }
    }

    fun onCycleNameChanged(name: String) {
        _uiState.update {
            it.copy(cycleName = name, isDirty = true, errorMessage = null)
        }
    }

    fun onStartDateChanged(startDate: String) {
        _uiState.update {
            it.copy(startDateInput = startDate, isDirty = true, errorMessage = null)
                .withTodayCycleDayIndex()
        }
    }

    fun onRepeatsChanged(repeats: Boolean) {
        _uiState.update {
            it.copy(repeats = repeats, isDirty = true, errorMessage = null)
                .withTodayCycleDayIndex()
        }
    }

    fun onCycleLengthIncreased() {
        onAddDay()
    }

    fun onCycleLengthDecreased() {
        val lastIndex = _uiState.value.days.lastOrNull()?.index ?: return
        onRemoveDay(lastIndex)
    }

    fun onAddDay() {
        _uiState.update { state ->
            val nextIndex = state.days.size + 1
            state.copy(
                selectedTemplate = CalendarCycleTemplate.CUSTOM,
                cycleName = state.cycleName.ifBlank { CalendarCycleTemplate.CUSTOM.title },
                days = (state.days + CalendarCycleDayDraftUiModel(
                    index = nextIndex,
                    name = "Власний день $nextIndex",
                    type = CalendarCycleDayType.CUSTOM
                )).reindex(),
                isDirty = true,
                errorMessage = null
            ).withTodayCycleDayIndex()
        }
    }

    fun onRemoveDay(index: Int) {
        _uiState.update { state ->
            if (state.days.size <= MIN_CYCLE_LENGTH) {
                state.copy(errorMessage = UiText.StringResource(R.string.calendar_error_min_cycle_length))
            } else {
                state.copy(
                    selectedTemplate = CalendarCycleTemplate.CUSTOM,
                    days = state.days.filterNot { it.index == index }.reindex(),
                    isDirty = true,
                    errorMessage = null
                ).withTodayCycleDayIndex()
            }
        }
    }

    fun onMoveDayUp(index: Int) {
        moveDay(index = index, direction = -1)
    }

    fun onMoveDayDown(index: Int) {
        moveDay(index = index, direction = 1)
    }

    fun onDayNameChanged(index: Int, name: String) {
        _uiState.update { state ->
            state.copy(
                selectedTemplate = CalendarCycleTemplate.CUSTOM,
                days = state.days.map { day ->
                    if (day.index == index) day.copy(name = name) else day
                },
                isDirty = true,
                errorMessage = null
            ).withTodayCycleDayIndex()
        }
    }

    fun onDayTypeChanged(index: Int, type: CalendarCycleDayType) {
        _uiState.update { state ->
            state.copy(
                selectedTemplate = CalendarCycleTemplate.CUSTOM,
                days = state.days.map { day ->
                    if (day.index == index) day.copy(type = type) else day
                },
                isDirty = true,
                errorMessage = null
            ).withTodayCycleDayIndex()
        }
    }

    fun onTodayCycleDaySelected(index: Int) {
        _uiState.update { state ->
            val cycleLength = state.days.size.coerceAtLeast(MIN_CYCLE_LENGTH)
            val safeIndex = index.coerceIn(1, cycleLength)
            val startDate = LocalDate.ofEpochDay(todayEpochDay() - (safeIndex - 1).toLong())
            state.copy(
                startDateInput = startDate.format(dateFormatter),
                isDirty = true,
                errorMessage = null
            ).withTodayCycleDayIndex()
        }
    }

    fun onSaveCycle() {
        val state = _uiState.value
        val startDate = parseStartDate(state.startDateInput)
        if (startDate == null) {
            _uiState.update {
                it.copy(errorMessage = UiText.StringResource(R.string.calendar_error_invalid_start_date))
            }
            return
        }

        val sanitizedDays = state.days
            .filter { it.name.isNotBlank() }
            .reindex()
            .map { day ->
                CalendarCycleDay(
                    index = day.index,
                    name = day.name.trim(),
                    type = day.type
                )
            }

        if (sanitizedDays.isEmpty()) {
            _uiState.update {
                it.copy(errorMessage = UiText.StringResource(R.string.calendar_error_empty_cycle_days))
            }
            return
        }

        viewModelScope.launch(dispatchers.io) {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val cycle = CalendarCycle(
                id = state.cycleId,
                name = state.cycleName.trim().ifBlank { state.selectedTemplate.title },
                startEpochDay = startDate.toEpochDay(),
                repeats = state.repeats,
                template = state.selectedTemplate,
                days = sanitizedDays
            )
            runCatching { calendarCycleRepository.saveCalendarCycle(cycle) }
                .onSuccess {
                    _uiState.update { cycle.toUiState() }
                    _events.emit(CalendarSettingsEvent.Saved)
                }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = UiText.StringResource(R.string.calendar_error_save_failed)
                        )
                    }
                }
        }
    }

    private fun moveDay(index: Int, direction: Int) {
        _uiState.update { state ->
            val currentPosition = state.days.indexOfFirst { it.index == index }
            val targetPosition = currentPosition + direction
            if (currentPosition == -1 || targetPosition !in state.days.indices) {
                state
            } else {
                val mutableDays = state.days.toMutableList()
                val day = mutableDays.removeAt(currentPosition)
                mutableDays.add(targetPosition, day)
                state.copy(
                    selectedTemplate = CalendarCycleTemplate.CUSTOM,
                    days = mutableDays.reindex(),
                    isDirty = true,
                    errorMessage = null
                ).withTodayCycleDayIndex()
            }
        }
    }

    private fun CalendarCycle.toUiState(): CalendarSettingsUiState =
        CalendarSettingsUiState(
            cycleId = id,
            selectedTemplate = template,
            cycleName = name,
            startDateInput = LocalDate.ofEpochDay(startEpochDay).format(dateFormatter),
            repeats = repeats,
            days = days.toDraftDays(),
            isSaving = false,
            isDirty = false
        ).withTodayCycleDayIndex()

    private fun CalendarSettingsUiState.withTodayCycleDayIndex(): CalendarSettingsUiState {
        val startDate = parseStartDate(startDateInput)
        val cycleLength = days.size
        if (startDate == null || cycleLength == 0) {
            return copy(todayCycleDayIndex = null)
        }

        val today = todayEpochDay()
        val start = startDate.toEpochDay()
        val todayIndex = if (!repeats && today !in start until (start + cycleLength)) {
            null
        } else {
            (((today - start).coerceAtLeast(0) % cycleLength) + 1).toInt()
        }
        return copy(todayCycleDayIndex = todayIndex)
    }

    private fun List<CalendarCycleDay>.toDraftDays(): List<CalendarCycleDayDraftUiModel> =
        map { day ->
            CalendarCycleDayDraftUiModel(
                index = day.index,
                name = day.name,
                type = day.type
            )
        }.reindex()

    private fun List<CalendarCycleDayDraftUiModel>.reindex(): List<CalendarCycleDayDraftUiModel> =
        mapIndexed { position, day -> day.copy(index = position + 1) }

    private fun parseStartDate(value: String): LocalDate? =
        try {
            LocalDate.parse(value.trim(), dateFormatter)
        } catch (_: DateTimeParseException) {
            null
        }

    private fun todayEpochDay(): Long =
        Instant.ofEpochMilli(clock.now())
            .atZone(clock.zoneId())
            .toLocalDate()
            .toEpochDay()

    private companion object {
        const val MIN_CYCLE_LENGTH = 1
    }
}
