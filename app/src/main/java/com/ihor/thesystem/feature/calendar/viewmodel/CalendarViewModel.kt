package com.ihor.thesystem.feature.calendar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.domain.repository.ScheduleRepository
import com.ihor.thesystem.domain.repository.SystemConfigRepository
import com.ihor.thesystem.domain.usecase.CalculateCycleDayForDateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class CalendarUiState(
    val anchorTimestamp: Long = 0L,
    val selectedDate: LocalDate? = null,
    val isLoading: Boolean = false
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val configRepo: SystemConfigRepository,
    private val scheduleRepo: ScheduleRepository,
    private val calculateCycleDay: CalculateCycleDayForDateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            configRepo.getConfig().collect { config ->
                _uiState.update { it.copy(anchorTimestamp = config?.cycleAnchorDateTimestamp ?: 0L) }
            }
        }
    }

    fun getCycleDay(date: LocalDate): Int {
        return calculateCycleDay(date, _uiState.value.anchorTimestamp)
    }

    fun getScheduleForDay(cycleDay: Int) = scheduleRepo.getScheduleForDay(cycleDay)

    fun onDateSelected(date: LocalDate?) {
        _uiState.update { it.copy(selectedDate = date) }
    }
}
