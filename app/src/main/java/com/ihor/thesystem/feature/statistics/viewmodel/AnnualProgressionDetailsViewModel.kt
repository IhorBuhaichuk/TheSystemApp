package com.ihor.thesystem.feature.statistics.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.domain.model.AnnualProgressionDetailStatus
import com.ihor.thesystem.domain.model.AnnualProgressionDetailsData
import com.ihor.thesystem.domain.model.AnnualProgressionExerciseDetails
import com.ihor.thesystem.domain.usecase.GetAnnualProgressionDetailsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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

@HiltViewModel
class AnnualProgressionDetailsViewModel @Inject constructor(
    getAnnualProgressionDetails: GetAnnualProgressionDetailsUseCase
) : ViewModel() {

    private val selectedExerciseId = MutableStateFlow<Int?>(null)

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
