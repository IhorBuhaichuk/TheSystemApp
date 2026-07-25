package com.ihor.thesystem.feature.status.viewmodel

import androidx.compose.runtime.Immutable
import com.ihor.thesystem.domain.model.ActiveSetInput
import com.ihor.thesystem.domain.model.EquipmentProfile
import com.ihor.thesystem.domain.model.ExerciseTrackingMode
import com.ihor.thesystem.presentation.common.model.MatrixEntryUiModel
import com.ihor.thesystem.domain.model.ExerciseDetails
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class WorkoutScheduleSettingsUiState(
    val selectedDay: Int = 1,
    val totalCycleDays: Int = 4,
    val workoutNameDraft: String = "",
    val exercisesForSelectedDay: ImmutableList<ExerciseDetails> = persistentListOf(),
    val allExercises: ImmutableList<ExerciseDetails> = persistentListOf(),
    val equipmentProfile: EquipmentProfile = EquipmentProfile(),
    val dumbbellMaxKgDraft: String = "",
    val healthConnect: HealthConnectUiState = HealthConnectUiState(),
    val backup: BackupUiState = BackupUiState(),
    val isLoading: Boolean = false
)

@Immutable
data class HealthConnectUiState(
    val isAvailable: Boolean = false,
    val hasReadinessPermission: Boolean = false,
    val isLoading: Boolean = false
)

@Immutable
data class BackupUiState(
    val lastExportedAtMillis: Long? = null,
    val lastImportedAtMillis: Long? = null,
    val pendingImport: BackupImportPreviewUiState? = null,
    val isBusy: Boolean = false
)

@Immutable
data class BackupImportPreviewUiState(
    val exportedAtMillis: Long,
    val tableCount: Int,
    val rowCount: Int
)

@Immutable
data class ActiveDayUiModel(
    val dayNumber: Int,
    val dailyTasks: ImmutableList<com.ihor.thesystem.domain.model.Quest>,
    val workoutName: String?,
    val exercises: ImmutableList<ExerciseWorkoutUiModel>,
    val adjustmentReason: String? = null,
    val matrixEntries: ImmutableList<MatrixEntryUiModel> = persistentListOf(),
    val loggingSummary: WorkoutLoggingSummaryUiModel = WorkoutLoggingSummaryUiModel()
)

@Immutable
data class ExerciseWorkoutUiModel(
    val exerciseId: Int,
    val name: String,
    val nameUk: String? = null,
    val recommendedWeight: Double? = null,
    val recommendedReps: Int? = null,
    val recommendedSets: Int? = null,
    val recommendation: String? = null,
    val gifUrl: String? = null,
    val externalId: String? = null,
    val trackingMode: ExerciseTrackingMode = ExerciseTrackingMode.WEIGHT_REPS,
    val isCoreSystemExercise: Boolean = false,
    val movementPattern: String? = null,
    val techniqueTips: ImmutableList<String> = persistentListOf(),
    val commonMistakes: ImmutableList<String> = persistentListOf(),
    val substitutionExternalIds: ImmutableList<String> = persistentListOf(),
    val techniqueCheckEmphasized: Boolean = false,
    val sets: ImmutableList<ActiveSetInput> = persistentListOf()
)

@Immutable
data class WorkoutLoggingSummaryUiModel(
    val completedSets: Int = 0,
    val totalSets: Int = 0,
    val completedExercises: Int = 0,
    val totalExercises: Int = 0,
    val remainingSets: Int = 0,
    val canFinish: Boolean = false,
    val progressText: String = "0/0 підх.",
    val exerciseText: String = "0 вправ",
    val helperText: String = "Заповни перший підхід, щоб система могла зберегти тренування.",
    val finishCtaText: String = "Завершити тренування"
)

@Immutable
data class CycleDayUiModel(
    val dayNumber: Int,
    val type: DayType,
    val isActive: Boolean,
    val isSelected: Boolean = false,
    val workoutName: String? = null
)

enum class DayType { REST, WORKOUT }
