package com.ihor.thesystem.feature.architect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.R
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.domain.util.AppClock
import com.ihor.thesystem.core.util.DispatcherProvider
import com.ihor.thesystem.domain.model.AnnualProgressionPlan
import com.ihor.thesystem.domain.model.AnnualProgressionPlanInput
import com.ihor.thesystem.domain.model.ExerciseTrackingMode
import com.ihor.thesystem.domain.usecase.ANNUAL_PROGRESSION_ADAPTATION_DAYS
import com.ihor.thesystem.domain.usecase.GenerateAnnualProgressionPlanUseCase
import com.ihor.thesystem.domain.usecase.GetAnnualProgressionExerciseSnapshotUseCase
import com.ihor.thesystem.domain.usecase.GetTrainingPhaseContextUseCase
import com.ihor.thesystem.domain.usecase.SaveAnnualProgressionPlanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject

data class AnnualProgressionPlanUiState(
    val startDate: LocalDate = LocalDate.now(),
    val currentDate: LocalDate = LocalDate.now(),
    val selectedExercises: List<AnnualProgressionExerciseUiModel> = emptyList(),
    val generatedPlan: AnnualProgressionPlan? = null,
    val expandedResultIds: Set<Int> = emptySet(),
    val isLoadingExercise: Boolean = false,
    val isGenerating: Boolean = false,
    val isSaving: Boolean = false,
    val message: UiText? = null
) {
    val adaptationEndsOn: LocalDate = startDate.plusDays(ANNUAL_PROGRESSION_ADAPTATION_DAYS.toLong())
    val adaptationRemainingDays: Long =
        ChronoUnit.DAYS.between(currentDate, adaptationEndsOn).coerceAtLeast(0)
    val isAdaptationComplete: Boolean = adaptationRemainingDays == 0L
    val canGenerate: Boolean =
        isAdaptationComplete &&
            selectedExercises.isNotEmpty() &&
            selectedExercises.all { it.canGenerate }
}

data class AnnualProgressionExerciseUiModel(
    val exerciseId: Int,
    val exerciseName: String,
    val currentWorkingWeight: Double?,
    val reps: Int?,
    val lastTrainingTimestamp: Long?,
    val estimatedOneRepMax: Double?,
    val targetWeightInput: String,
    val inventoryStepInput: String,
    val trackingMode: ExerciseTrackingMode,
    val isExpanded: Boolean = false
) {
    val targetWeight: Double?
        get() = targetWeightInput.normalizedDouble()
    val inventoryStep: Double?
        get() = inventoryStepInput.normalizedDouble()
    val canGenerate: Boolean
        get() = currentWorkingWeight != null &&
            currentWorkingWeight > 0.0 &&
            (targetWeight ?: 0.0) > currentWorkingWeight &&
            (inventoryStep ?: 0.0) > 0.0
}

@HiltViewModel
class AnnualProgressionPlanViewModel @Inject constructor(
    private val getExerciseSnapshot: GetAnnualProgressionExerciseSnapshotUseCase,
    private val generateAnnualProgressionPlan: GenerateAnnualProgressionPlanUseCase,
    private val saveAnnualProgressionPlan: SaveAnnualProgressionPlanUseCase,
    private val getTrainingPhaseContext: GetTrainingPhaseContextUseCase,
    private val dispatchers: DispatcherProvider,
    private val clock: AppClock
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AnnualProgressionPlanUiState(
            startDate = today(),
            currentDate = today()
        )
    )
    val uiState: StateFlow<AnnualProgressionPlanUiState> = _uiState.asStateFlow()

    init {
        loadTrainingPhase()
    }

    private fun loadTrainingPhase() {
        viewModelScope.launch(dispatchers.io) {
            val today = today()
            runCatching { getTrainingPhaseContext() }
                .onSuccess { phase ->
                    _uiState.update {
                        it.copy(
                            startDate = phase.firstWorkoutDate ?: today,
                            currentDate = today,
                            generatedPlan = null,
                            message = null
                        )
                    }
                }
        }
    }

    fun onUseTodayAsStartDate() {
        val today = today()
        _uiState.update {
            it.copy(startDate = today, currentDate = today, generatedPlan = null, message = null)
        }
    }

    fun onStartDateSelected(date: LocalDate) {
        _uiState.update {
            it.copy(startDate = date, currentDate = today(), generatedPlan = null, message = null)
        }
    }

    fun onExerciseSelected(exerciseId: Int) {
        if (_uiState.value.selectedExercises.any { it.exerciseId == exerciseId }) return

        viewModelScope.launch(dispatchers.io) {
            _uiState.update { it.copy(isLoadingExercise = true, message = null) }
            runCatching { getExerciseSnapshot(exerciseId) }
                .onSuccess { snapshot ->
                    if (snapshot == null) {
                        _uiState.update {
                            it.copy(
                                isLoadingExercise = false,
                                message = UiText.StringResource(R.string.error_annual_progression_exercise_not_found)
                            )
                        }
                        return@onSuccess
                    }
                    if (!snapshot.trackingMode.usesWeightInput) {
                        _uiState.update {
                            it.copy(
                                isLoadingExercise = false,
                                message = UiText.StringResource(R.string.error_annual_progression_external_load_required)
                            )
                        }
                        return@onSuccess
                    }

                    val exerciseUiModel = AnnualProgressionExerciseUiModel(
                        exerciseId = snapshot.exercise.id,
                        exerciseName = snapshot.exercise.nameUk ?: snapshot.exercise.name,
                        currentWorkingWeight = snapshot.currentWorkingWeight,
                        reps = snapshot.reps,
                        lastTrainingTimestamp = snapshot.lastTrainingTimestamp,
                        estimatedOneRepMax = snapshot.estimatedOneRepMax,
                        targetWeightInput = snapshot.defaultTargetWeight?.formatWeight().orEmpty(),
                        inventoryStepInput = snapshot.inventoryStep.formatWeight(),
                        trackingMode = snapshot.trackingMode
                    )

                    _uiState.update {
                        it.copy(
                            selectedExercises = it.selectedExercises + exerciseUiModel,
                            generatedPlan = null,
                            isLoadingExercise = false,
                            message = null
                        )
                    }
                }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    _uiState.update {
                        it.copy(
                            isLoadingExercise = false,
                            message = UiText.StringResource(R.string.error_annual_progression_snapshot_failed)
                        )
                    }
                }
        }
    }

    fun onRemoveExercise(exerciseId: Int) {
        _uiState.update { state ->
            state.copy(
                selectedExercises = state.selectedExercises.filterNot { it.exerciseId == exerciseId },
                generatedPlan = null
            )
        }
    }

    fun onTargetWeightChanged(exerciseId: Int, value: String) {
        _uiState.update { state ->
            state.copy(
                selectedExercises = state.selectedExercises.map { exercise ->
                    if (exercise.exerciseId == exerciseId) {
                        exercise.copy(targetWeightInput = value)
                    } else {
                        exercise
                    }
                },
                generatedPlan = null
            )
        }
    }

    fun onInventoryStepChanged(exerciseId: Int, value: String) {
        _uiState.update { state ->
            state.copy(
                selectedExercises = state.selectedExercises.map { exercise ->
                    if (exercise.exerciseId == exerciseId) {
                        exercise.copy(inventoryStepInput = value)
                    } else {
                        exercise
                    }
                },
                generatedPlan = null
            )
        }
    }

    fun onExerciseExpanded(exerciseId: Int) {
        _uiState.update { state ->
            state.copy(
                selectedExercises = state.selectedExercises.map { exercise ->
                    if (exercise.exerciseId == exerciseId) {
                        exercise.copy(isExpanded = !exercise.isExpanded)
                    } else {
                        exercise
                    }
                }
            )
        }
    }

    fun onResultExpanded(exerciseId: Int) {
        _uiState.update { state ->
            val expandedIds = if (exerciseId in state.expandedResultIds) {
                state.expandedResultIds - exerciseId
            } else {
                state.expandedResultIds + exerciseId
            }
            state.copy(expandedResultIds = expandedIds)
        }
    }

    fun onGeneratePlan() {
        _uiState.update { it.copy(currentDate = today()) }
        val state = _uiState.value
        if (!state.isAdaptationComplete) {
            _uiState.update {
                it.copy(message = UiText.StringResource(R.string.annual_progression_adaptation_required))
            }
            return
        }

        val inputs = state.selectedExercises.mapNotNull { exercise ->
            val currentWeight = exercise.currentWorkingWeight ?: return@mapNotNull null
            val targetWeight = exercise.targetWeight ?: return@mapNotNull null
            val inventoryStep = exercise.inventoryStep ?: return@mapNotNull null
            AnnualProgressionPlanInput(
                exerciseId = exercise.exerciseId,
                exerciseName = exercise.exerciseName,
                currentWeight = currentWeight,
                targetWeight = targetWeight,
                inventoryStep = inventoryStep
            )
        }

        if (inputs.size != state.selectedExercises.size || inputs.isEmpty()) {
            _uiState.update {
                it.copy(message = UiText.StringResource(R.string.error_annual_progression_input_required))
            }
            return
        }

        _uiState.update { it.copy(isGenerating = true, message = null) }
        val plan = generateAnnualProgressionPlan(
            startDate = state.startDate,
            inputs = inputs
        )
        _uiState.update {
            it.copy(
                generatedPlan = plan,
                isGenerating = false,
                expandedResultIds = plan.exercises.map { exercise -> exercise.exerciseId }.toSet()
            )
        }
    }

    fun onSavePlan() {
        val plan = _uiState.value.generatedPlan ?: return
        viewModelScope.launch(dispatchers.io) {
            _uiState.update { it.copy(isSaving = true, message = null) }
            runCatching { saveAnnualProgressionPlan(plan) }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            message = UiText.StringResource(R.string.annual_progression_plan_saved)
                        )
                    }
                }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            message = UiText.StringResource(R.string.error_annual_progression_save_failed)
                        )
                    }
                }
        }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(message = null) }
    }

    private fun today(): LocalDate =
        Instant.ofEpochMilli(clock.now())
            .atZone(clock.zoneId())
            .toLocalDate()
}

private fun String.normalizedDouble(): Double? =
    replace(',', '.').toDoubleOrNull()

private fun Double.formatWeight(): String =
    if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", this)
    }
