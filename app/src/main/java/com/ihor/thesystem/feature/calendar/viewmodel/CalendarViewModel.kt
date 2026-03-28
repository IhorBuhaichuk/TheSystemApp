package com.ihor.thesystem.feature.calendar.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.data.local.room.dao.WorkoutDao
import com.ihor.thesystem.data.local.room.relations.SessionWithSets
import com.ihor.thesystem.domain.repository.ScheduleRepository
import com.ihor.thesystem.domain.repository.SystemConfigRepository
import com.ihor.thesystem.domain.repository.WorkoutAnalyticsRepository
import com.ihor.thesystem.domain.usecase.CalculateCycleDayForDateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class CalendarUiState(
    val anchorTimestamp: Long = 0L,
    val anchorCycleDay: Int = 1,
    val selectedDate: LocalDate? = null,
    val workoutResults: List<WorkoutResultUiModel> = emptyList(),
    val isLoading: Boolean = false
)

data class WorkoutResultUiModel(
    val exerciseName: String,
    val sets: List<SetResultUiModel>
)

data class SetResultUiModel(
    val weight: Double,
    val reps: Int
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val configRepo: SystemConfigRepository,
    private val scheduleRepo: ScheduleRepository,
    private val analyticsRepo: WorkoutAnalyticsRepository,
    private val workoutDao: WorkoutDao,
    private val calculateCycleDay: CalculateCycleDayForDateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            configRepo.getConfig().collect { config ->
                _uiState.update { 
                    it.copy(
                        anchorTimestamp = config?.cycleAnchorDateTimestamp ?: 0L,
                        anchorCycleDay = config?.cycleAnchorDay ?: 1
                    ) 
                }
            }
        }
    }

    fun getCycleDay(date: LocalDate): Int {
        return calculateCycleDay(
            targetDate = date, 
            anchorEpochDay = _uiState.value.anchorTimestamp,
            anchorCycleDay = _uiState.value.anchorCycleDay
        )
    }

    fun getScheduleForDay(cycleDay: Int) = scheduleRepo.getScheduleForDay(cycleDay)

    fun onDateSelected(date: LocalDate?) {
        _uiState.update { it.copy(selectedDate = date) }
        if (date != null) {
            loadWorkoutResults(date)
        } else {
            _uiState.update { it.copy(workoutResults = emptyList()) }
        }
    }

    private fun loadWorkoutResults(date: LocalDate) {
        val millis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        viewModelScope.launch {
            analyticsRepo.getSessionsByDate(millis).collect { sessions ->
                val results = sessions.flatMap { sessionWithSets ->
                    sessionWithSets.sets.groupBy { it.exerciseId }.map { (exId, sets) ->
                        val name = workoutDao.getExerciseNameById(exId) ?: "Вправа $exId"
                        WorkoutResultUiModel(
                            exerciseName = name,
                            sets = sets.map { SetResultUiModel(it.weight, it.reps) }
                        )
                    }
                }
                _uiState.update { it.copy(workoutResults = results) }
            }
        }
    }
}
