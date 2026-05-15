package com.ihor.thesystem.feature.statistics.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.domain.util.AppClock
import com.ihor.thesystem.core.util.DispatcherProvider
import com.ihor.thesystem.domain.model.AnnualProgressionAdjustment
import com.ihor.thesystem.domain.model.AnnualProgressionDetailStatus
import com.ihor.thesystem.domain.model.AnnualProgressionDetailsData
import com.ihor.thesystem.domain.model.AnnualProgressionExerciseDetails
import com.ihor.thesystem.domain.model.AnnualProgressionExercisePlan
import com.ihor.thesystem.domain.model.AnnualProgressionMonthlyTarget
import com.ihor.thesystem.domain.model.AnnualProgressionPlan
import com.ihor.thesystem.domain.model.AnnualProgressionPlanStatus
import com.ihor.thesystem.domain.model.ExerciseTrackingMode
import com.ihor.thesystem.domain.model.ExerciseTrackingModeResolver
import com.ihor.thesystem.domain.repository.ScheduleRepository
import com.ihor.thesystem.domain.usecase.GetAnnualProgressionDetailsUseCase
import com.ihor.thesystem.domain.usecase.GetAnnualProgressionExerciseSnapshotUseCase
import com.ihor.thesystem.domain.usecase.GetSystemConfigUseCase
import com.ihor.thesystem.domain.usecase.SaveAnnualProgressionPlanUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import kotlin.math.abs
import javax.inject.Inject

data class AnnualProgressionDetailsUiState(
    val isLoading: Boolean = true,
    val data: AnnualProgressionDetailsData = AnnualProgressionDetailsData(),
    val selectedExerciseId: Int? = null,
    val errorMessage: UiText? = null
) {
    val selectedExercise: AnnualProgressionExerciseDetails?
        get() = data.exercises.firstOrNull { it.exerciseId == selectedExerciseId }
            ?: data.exercises.firstOrNull()
}

data class AnnualProgressionManualEditorUiState(
    val isOpen: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val currentDate: LocalDate = LocalDate.now(),
    val exercises: List<AnnualProgressionManualExerciseUiModel> = emptyList(),
    val message: UiText? = null
) {
    val canSave: Boolean
        get() = isOpen && !isLoading && !isSaving && exercises.isNotEmpty()
}

data class AnnualProgressionManualExerciseUiModel(
    val exerciseId: Int,
    val exerciseName: String,
    val trackingMode: ExerciseTrackingMode,
    val cycleDays: List<Int>,
    val currentWorkingWeight: Double?,
    val inventoryStep: Double,
    val monthTargets: List<String> = List(MANUAL_MONTH_COUNT) { "" }
)

@HiltViewModel
class AnnualProgressionDetailsViewModel @Inject constructor(
    getAnnualProgressionDetails: GetAnnualProgressionDetailsUseCase,
    private val getSystemConfig: GetSystemConfigUseCase,
    private val scheduleRepository: ScheduleRepository,
    private val getExerciseSnapshot: GetAnnualProgressionExerciseSnapshotUseCase,
    private val saveAnnualProgressionPlan: SaveAnnualProgressionPlanUseCase,
    private val dispatchers: DispatcherProvider,
    private val clock: AppClock
) : ViewModel() {

    private val selectedExerciseId = MutableStateFlow<Int?>(null)
    private val _manualEditorState = MutableStateFlow(
        AnnualProgressionManualEditorUiState(currentDate = today())
    )
    val manualEditorState: StateFlow<AnnualProgressionManualEditorUiState> =
        _manualEditorState.asStateFlow()

    val uiState: StateFlow<AnnualProgressionDetailsUiState> =
        combine(
            getAnnualProgressionDetails()
                .catch {
                    emit(AnnualProgressionDetailsData())
                },
            selectedExerciseId
        ) { data, selectedId ->
            val resolvedSelectedId = selectedId
                ?.takeIf { id -> data.exercises.any { it.exerciseId == id } }
                ?: data.exercises.firstOrNull()?.exerciseId

            AnnualProgressionDetailsUiState(
                isLoading = false,
                data = data,
                selectedExerciseId = resolvedSelectedId
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = AnnualProgressionDetailsUiState()
        )

    fun onExerciseSelected(exerciseId: Int) {
        selectedExerciseId.value = exerciseId
    }

    fun onCreateManually() {
        _manualEditorState.update {
            it.copy(
                isOpen = true,
                isLoading = true,
                isSaving = false,
                currentDate = today(),
                exercises = emptyList(),
                message = null
            )
        }

        viewModelScope.launch(dispatchers.io) {
            runCatching {
                val config = getSystemConfig().first()
                val totalCycleDays = config?.cycleDaysPerMicrocycle
                    ?.coerceAtLeast(1)
                    ?: DEFAULT_CYCLE_DAYS
                val schedules = scheduleRepository
                    .getSchedulesForDays((1..totalCycleDays).toList())
                    .first()
                    .sortedBy { it.cycleDay }
                val exerciseRows = linkedMapOf<Int, ScheduledExerciseSeed>()

                schedules.forEach { schedule ->
                    schedule.exercises.forEach { exercise ->
                        val seed = exerciseRows.getOrPut(exercise.id) {
                            ScheduledExerciseSeed(
                                exerciseId = exercise.id,
                                exerciseName = exercise.nameUk ?: exercise.name,
                                exerciseNameUk = exercise.nameUk,
                                category = exercise.category,
                                equipment = exercise.equipment,
                                externalId = exercise.externalId,
                                trackingMode = exercise.trackingMode,
                                cycleDays = mutableListOf()
                            )
                        }
                        if (schedule.cycleDay !in seed.cycleDays) {
                            seed.cycleDays += schedule.cycleDay
                        }
                    }
                }

                exerciseRows.values.mapNotNull { seed ->
                    val snapshot = runCatching { getExerciseSnapshot(seed.exerciseId) }
                        .getOrNull()
                    val trackingMode = ExerciseTrackingModeResolver.resolve(
                        trackingModeOverride = seed.trackingMode,
                        name = seed.exerciseName,
                        nameUk = seed.exerciseNameUk,
                        externalId = seed.externalId,
                        category = seed.category,
                        equipment = seed.equipment
                    )
                    if (!trackingMode.usesWeightInput) return@mapNotNull null

                    AnnualProgressionManualExerciseUiModel(
                        exerciseId = seed.exerciseId,
                        exerciseName = seed.exerciseName,
                        trackingMode = trackingMode,
                        cycleDays = seed.cycleDays.toList(),
                        currentWorkingWeight = snapshot?.currentWorkingWeight,
                        inventoryStep = snapshot?.inventoryStep ?: DEFAULT_INVENTORY_STEP
                    )
                }
            }.onSuccess { exercises ->
                _manualEditorState.update {
                    it.copy(
                        isLoading = false,
                        exercises = exercises,
                        message = if (exercises.isEmpty()) {
                            UiText.DynamicString("У розкладі поки немає вагових вправ для річної прогресії.")
                        } else {
                            null
                        }
                    )
                }
            }.onFailure { error ->
                if (error is CancellationException) throw error
                _manualEditorState.update {
                    it.copy(
                        isLoading = false,
                        message = UiText.DynamicString("Не вдалося підготувати вправи з розкладу.")
                    )
                }
            }
        }
    }

    fun onCloseManualEditor() {
        if (_manualEditorState.value.isSaving) return
        _manualEditorState.update { it.copy(isOpen = false, message = null) }
    }

    fun onManualTargetChanged(exerciseId: Int, monthIndex: Int, value: String) {
        if (monthIndex !in 0 until MANUAL_MONTH_COUNT) return
        _manualEditorState.update { state ->
            state.copy(
                exercises = state.exercises.map { exercise ->
                    if (exercise.exerciseId != exerciseId) {
                        exercise
                    } else {
                        exercise.copy(
                            monthTargets = exercise.monthTargets.mapIndexed { index, target ->
                                if (index == monthIndex) value else target
                            }
                        )
                    }
                },
                message = null
            )
        }
    }

    fun onSaveManualPlan() {
        val state = _manualEditorState.value
        val exercises = state.exercises
        if (exercises.isEmpty()) {
            _manualEditorState.update {
                it.copy(message = UiText.DynamicString("Немає вправ для збереження."))
            }
            return
        }

        val plans = exercises.mapNotNull { exercise ->
            val targets = exercise.monthTargets.map { it.normalizedDouble() }
            if (targets.any { it == null || it <= 0.0 }) return@mapNotNull null
            exercise.toManualPlan(targets.filterNotNull())
        }

        if (plans.size != exercises.size) {
            _manualEditorState.update {
                it.copy(message = UiText.DynamicString("Заповни всі 12 місячних цілей для кожної вправи."))
            }
            return
        }

        val plan = AnnualProgressionPlan(
            startDate = state.currentDate,
            adaptationEndsOn = state.currentDate,
            exercises = plans
        )

        viewModelScope.launch(dispatchers.io) {
            _manualEditorState.update { it.copy(isSaving = true, message = null) }
            runCatching { saveAnnualProgressionPlan(plan) }
                .onSuccess {
                    _manualEditorState.update {
                        it.copy(
                            isOpen = false,
                            isSaving = false,
                            message = UiText.DynamicString("Річний графік збережено вручну.")
                        )
                    }
                }
                .onFailure { error ->
                    if (error is CancellationException) throw error
                    _manualEditorState.update {
                        it.copy(
                            isSaving = false,
                            message = UiText.DynamicString("Не вдалося зберегти ручний графік.")
                        )
                    }
                }
        }
    }

    fun onManualMessageShown() {
        _manualEditorState.update { it.copy(message = null) }
    }

    private fun AnnualProgressionManualExerciseUiModel.toManualPlan(
        targets: List<Double>
    ): AnnualProgressionExercisePlan {
        val startWeight = if (trackingMode.usesWeightInput) {
            currentWorkingWeight
                ?.takeIf { it > 0.0 }
                ?: targets.first()
        } else {
            targets.first()
        }
        val monthlyTargets = buildList {
            add(
                AnnualProgressionMonthlyTarget(
                    monthIndex = 0,
                    weight = startWeight,
                    adjustment = AnnualProgressionAdjustment.Baseline
                )
            )
            targets.forEachIndexed { index, target ->
                val previous = if (index == 0) startWeight else targets[index - 1]
                add(
                    AnnualProgressionMonthlyTarget(
                        monthIndex = index + 1,
                        weight = target,
                        adjustment = resolveManualAdjustment(
                            previousWeight = previous,
                            currentWeight = target,
                            inventoryStep = inventoryStep
                        )
                    )
                )
            }
        }

        return AnnualProgressionExercisePlan(
            exerciseId = exerciseId,
            exerciseName = exerciseName,
            currentWeight = startWeight,
            targetWeight = targets.last(),
            inventoryStep = inventoryStep,
            idealMonthlyStep = (targets.last() - startWeight) / MANUAL_MONTH_COUNT,
            monthlyTargets = monthlyTargets,
            status = AnnualProgressionPlanStatus.Ready
        )
    }

    private fun resolveManualAdjustment(
        previousWeight: Double,
        currentWeight: Double,
        inventoryStep: Double
    ): AnnualProgressionAdjustment {
        val change = currentWeight - previousWeight
        return when {
            abs(change) < WEIGHT_EPSILON -> AnnualProgressionAdjustment.Plateau
            change > inventoryStep * FORCED_JUMP_THRESHOLD -> AnnualProgressionAdjustment.ForcedJump
            else -> AnnualProgressionAdjustment.StandardStep
        }
    }

    private fun today(): LocalDate =
        Instant.ofEpochMilli(clock.now())
            .atZone(clock.zoneId())
            .toLocalDate()
}

object AnnualProgressionDetailsUiMapper {
    fun statusLabel(status: AnnualProgressionDetailStatus): String =
        when (status) {
            AnnualProgressionDetailStatus.OnPlan -> "За планом"
            AnnualProgressionDetailStatus.SlightlyBelow -> "Трохи нижче"
            AnnualProgressionDetailStatus.AbovePlan -> "Вище плану"
            AnnualProgressionDetailStatus.NoFact -> "Немає факту"
        }

    fun conclusionText(status: AnnualProgressionDetailStatus): String =
        when (status) {
            AnnualProgressionDetailStatus.OnPlan -> "Факт тримається в робочому коридорі відносно плану."
            AnnualProgressionDetailStatus.SlightlyBelow -> "Є легке відставання. Варто стабілізувати виконання без різких стрибків."
            AnnualProgressionDetailStatus.AbovePlan -> "Факт випереджає план. Важливо не форсувати адаптацію."
            AnnualProgressionDetailStatus.NoFact -> "Поки немає достатньо зафіксованих підходів для висновку."
        }
}
