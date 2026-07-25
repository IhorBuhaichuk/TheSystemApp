package com.ihor.thesystem.feature.statistics.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.R
import com.ihor.thesystem.core.ui.UiEvent
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.core.ui.asUiText
import com.ihor.thesystem.domain.util.AppClock
import com.ihor.thesystem.domain.util.Result
import com.ihor.thesystem.domain.usecase.GetBetaMetricsUseCase
import com.ihor.thesystem.domain.usecase.GetStatisticsDataUseCase
import com.ihor.thesystem.domain.usecase.LogWorkoutSetsUseCase
import com.ihor.thesystem.domain.usecase.LogWeightUseCase
import com.ihor.thesystem.domain.usecase.RecordBetaAppOpenUseCase
import com.ihor.thesystem.domain.usecase.RecalculateGlobalRankUseCase
import com.ihor.thesystem.domain.usecase.SelectViewingDateUseCase
import com.ihor.thesystem.domain.usecase.UpdateMatrixGoalsUseCase
import com.ihor.thesystem.domain.usecase.UpdatePlayerAgeUseCase
import com.ihor.thesystem.domain.usecase.UpdatePlayerHeightUseCase
import com.ihor.thesystem.domain.model.ActiveSetInput
import com.ihor.thesystem.domain.model.DomainError
import com.ihor.thesystem.domain.model.ValidationError
import com.ihor.thesystem.presentation.common.model.MatrixEntryUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getStatisticsDataUseCase: GetStatisticsDataUseCase,
    private val getBetaMetricsUseCase: GetBetaMetricsUseCase,
    private val logWorkoutSetsUseCase: LogWorkoutSetsUseCase,
    private val updateMatrixGoalsUseCase: UpdateMatrixGoalsUseCase,
    private val recalculateGlobalRankUseCase: RecalculateGlobalRankUseCase,
    private val selectViewingDateUseCase: SelectViewingDateUseCase,
    private val logWeightUseCase: LogWeightUseCase,
    private val updatePlayerHeightUseCase: UpdatePlayerHeightUseCase,
    private val updatePlayerAgeUseCase: UpdatePlayerAgeUseCase,
    private val recordBetaAppOpen: RecordBetaAppOpenUseCase,
    private val clock: AppClock
) : ViewModel() {

    private val _refreshRequests = MutableStateFlow(0L)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<UiState<StatisticsUiData>> = _refreshRequests
        .flatMapLatest {
            combine(
                getStatisticsDataUseCase(),
                getBetaMetricsUseCase()
            ) { statisticsData, betaMetrics ->
                statisticsData.toStatisticsUiData(betaMetrics)
            }
        }
        .map<StatisticsUiData, UiState<StatisticsUiData>> { UiState.Content(it) }
        .catch { e ->
            if (e is CancellationException) throw e
            Timber.e(e, "Failed to load statistics screen data")
            emit(UiState.Error(UiText.StringResource(R.string.error_generic)))
        }
        .stateIn(
            scope        = viewModelScope,
            started      = SharingStarted.WhileSubscribed(5_000L),
            initialValue = UiState.Loading
        )

    private val _dialogState = MutableStateFlow<StatisticsDialogState>(StatisticsDialogState.None)
    val dialogState: StateFlow<StatisticsDialogState> = _dialogState.asStateFlow()

    private val _currentSetInputs = MutableStateFlow<List<ActiveSetInput>>(emptyList())

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    init {
        recordAppOpen()
    }

    fun refreshForCurrentDay() {
        selectViewingDateUseCase(todayDate())
        recordAppOpen()
        _refreshRequests.value = clock.now()
    }

    fun onOpenSetup(entry: MatrixEntryUiModel) {
        _dialogState.value = StatisticsDialogState.SetupMatrix(entry, entry.startWeight.toString(), entry.targetWeight.toString())
    }

    fun onOpenLogSets(entry: MatrixEntryUiModel) {
        val initialSets = listOf(ActiveSetInput())
        _currentSetInputs.value = initialSets
        _dialogState.value = StatisticsDialogState.LogWorkoutSets(entry, initialSets)
    }

    fun updateSetInput(id: Long, weight: String, reps: String) {
        _currentSetInputs.update { list ->
            list.map { if (it.id == id) it.copy(weight = weight, reps = reps) else it }
        }
        updateLogDialogState()
    }

    fun addSet() {
        _currentSetInputs.update { it + ActiveSetInput() }
        updateLogDialogState()
    }

    fun removeSet() {
        _currentSetInputs.update { if (it.size > 1) it.dropLast(1) else it }
        updateLogDialogState()
    }

    private fun updateLogDialogState() {
        val current = _dialogState.value
        if (current is StatisticsDialogState.LogWorkoutSets) {
            _dialogState.value = current.copy(sets = _currentSetInputs.value)
        }
    }

    fun onLogSetsConfirmed(exerciseId: Int, sets: List<ActiveSetInput>, feedback: String) {
        viewModelScope.launch {
            try {
                logWorkoutSetsUseCase(
                    exerciseId = exerciseId,
                    sets = sets,
                    timestamp = clock.now(),
                    userFeedback = feedback
                )
                onDismissDialog()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.e(e, "Failed to log exercise sets from statistics")
                _uiEvents.emit(UiEvent.ShowError(UiText.StringResource(R.string.error_saving)))
            }
        }
    }

    fun onConfirmSetup(exerciseId: Int, start: String, target: String) {
        val setupState = _dialogState.value as? StatisticsDialogState.SetupMatrix
        if (setupState?.entry?.usesExternalLoad == false) {
            onDismissDialog()
            return
        }
        viewModelScope.launch {
            try {
                updateMatrixGoalsUseCase(exerciseId, start.toFloatOrNull() ?: 0f, target.toFloatOrNull() ?: 0f)
                recalculateGlobalRank()
                onDismissDialog()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.e(e, "Failed to update matrix setup from statistics")
                _uiEvents.emit(UiEvent.ShowError(UiText.StringResource(R.string.error_operation_failed)))
            }
        }
    }

    private suspend fun recalculateGlobalRank() {
        try {
            recalculateGlobalRankUseCase()
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Timber.e(e, "Failed to recalculate global rank from statistics")
            _uiEvents.emit(UiEvent.ShowError(UiText.StringResource(R.string.error_rank_update)))
        }
    }


    fun onOpenLogWeight() {
        _dialogState.value = StatisticsDialogState.LogWeight
    }

    fun onOpenEditHeight() {
        _dialogState.value = StatisticsDialogState.EditHeight
    }

    fun onOpenEditAge() {
        _dialogState.value = StatisticsDialogState.EditAge
    }

    fun onWeightConfirmed(weight: Float) {
        viewModelScope.launch {
            handleProfileUpdate(
                result = logWeightUseCase(weight),
                fallback = UiText.StringResource(R.string.error_weight_update)
            )
        }
    }

    fun onHeightConfirmed(height: Float) {
        viewModelScope.launch {
            handleProfileUpdate(
                result = updatePlayerHeightUseCase(height),
                fallback = UiText.StringResource(R.string.error_height_update)
            )
        }
    }

    fun onAgeConfirmed(age: Int) {
        viewModelScope.launch {
            handleProfileUpdate(
                result = updatePlayerAgeUseCase(age),
                fallback = UiText.StringResource(R.string.error_age_update)
            )
        }
    }

    fun onDismissDialog() { _dialogState.value = StatisticsDialogState.None }

    private fun todayDate() =
        Instant.ofEpochMilli(clock.now())
            .atZone(clock.zoneId())
            .toLocalDate()

    private fun recordAppOpen() {
        viewModelScope.launch {
            try {
                recordBetaAppOpen()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Timber.w(e, "Failed to record beta app open metric")
            }
        }
    }

    private suspend fun handleProfileUpdate(result: Result<Unit, DomainError>, fallback: UiText) {
        when (result) {
            is Result.Success -> onDismissDialog()
            is Result.Error -> {
                _uiEvents.emit(UiEvent.ShowError(result.error.validationTextOr(fallback)))
            }
        }
    }

    private fun DomainError.validationTextOr(fallback: UiText): UiText =
        if (this is ValidationError) asUiText() else fallback
}
