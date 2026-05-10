package com.ihor.thesystem.feature.architect.viewmodel

import com.ihor.thesystem.R
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.domain.util.AppClock
import com.ihor.thesystem.core.util.DispatcherProvider
import com.ihor.thesystem.domain.model.AnnualProgressionExerciseSnapshot
import com.ihor.thesystem.domain.model.ExerciseDetails
import com.ihor.thesystem.domain.usecase.GenerateAnnualProgressionPlanUseCase
import com.ihor.thesystem.domain.usecase.GetAnnualProgressionExerciseSnapshotUseCase
import com.ihor.thesystem.domain.usecase.GetTrainingPhaseContextUseCase
import com.ihor.thesystem.domain.usecase.SaveAnnualProgressionPlanUseCase
import com.ihor.thesystem.domain.usecase.TrainingPhaseContext
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class AnnualProgressionPlanViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getExerciseSnapshot: GetAnnualProgressionExerciseSnapshotUseCase = mockk()
    private val saveAnnualProgressionPlan: SaveAnnualProgressionPlanUseCase = mockk()
    private val getTrainingPhaseContext: GetTrainingPhaseContextUseCase = mockk()
    private val fixedDate = LocalDate.of(2026, 5, 2)

    @Test
    fun `initial state uses injected clock instead of wall clock`() {
        val viewModel = viewModel()

        assertEquals(fixedDate, viewModel.uiState.value.startDate)
        assertEquals(fixedDate, viewModel.uiState.value.currentDate)
    }

    @Test
    fun `missing exercise snapshot maps to localized message`() {
        coEvery { getExerciseSnapshot.invoke(42) } returns null

        val viewModel = viewModel()
        viewModel.onExerciseSelected(42)
        mainDispatcherRule.advanceUntilIdle()

        val message = viewModel.uiState.value.message as UiText.StringResource
        assertFalse(viewModel.uiState.value.isLoadingExercise)
        assertEquals(R.string.error_annual_progression_exercise_not_found, message.resId)
    }

    @Test
    fun `save failure clears saving and maps to localized message`() {
        coEvery { getExerciseSnapshot.invoke(7) } returns exerciseSnapshot()
        coEvery { saveAnnualProgressionPlan.invoke(any()) } throws IllegalStateException("write failed")

        val viewModel = viewModel()
        viewModel.onStartDateSelected(LocalDate.of(2026, 4, 1))
        viewModel.onExerciseSelected(7)
        mainDispatcherRule.advanceUntilIdle()
        viewModel.onGeneratePlan()

        assertNotNull(viewModel.uiState.value.generatedPlan)

        viewModel.onSavePlan()
        mainDispatcherRule.advanceUntilIdle()

        val message = viewModel.uiState.value.message as UiText.StringResource
        assertFalse(viewModel.uiState.value.isSaving)
        assertEquals(R.string.error_annual_progression_save_failed, message.resId)
    }

    private fun viewModel(): AnnualProgressionPlanViewModel {
        coEvery { getTrainingPhaseContext.invoke(null) } returns TrainingPhaseContext(
            firstWorkoutDate = fixedDate,
            referenceDate = fixedDate
        )

        return AnnualProgressionPlanViewModel(
            getExerciseSnapshot = getExerciseSnapshot,
            generateAnnualProgressionPlan = GenerateAnnualProgressionPlanUseCase(),
            saveAnnualProgressionPlan = saveAnnualProgressionPlan,
            getTrainingPhaseContext = getTrainingPhaseContext,
            dispatchers = AnnualTestDispatcherProvider(mainDispatcherRule.dispatcher),
            clock = FixedClock(fixedDate)
        ).also {
            mainDispatcherRule.advanceUntilIdle()
        }
    }

    private fun exerciseSnapshot(): AnnualProgressionExerciseSnapshot =
        AnnualProgressionExerciseSnapshot(
            exercise = ExerciseDetails(
                id = 7,
                name = "Bench press",
                nameUk = "Жим лежачи"
            ),
            currentWorkingWeight = 80.0,
            reps = 5,
            lastTrainingTimestamp = null,
            estimatedOneRepMax = 93.0,
            defaultTargetWeight = 100.0,
            inventoryStep = 2.5
        )

    private class FixedClock(
        private val date: LocalDate
    ) : AppClock {
        override fun now(): Long =
            date.atStartOfDay(zoneId()).toInstant().toEpochMilli()

        override fun zoneId(): ZoneId = ZoneId.of("Europe/Kyiv")
    }

    private class AnnualTestDispatcherProvider(
        private val dispatcher: CoroutineDispatcher
    ) : DispatcherProvider {
        override val main: CoroutineDispatcher = dispatcher
        override val io: CoroutineDispatcher = dispatcher
        override val default: CoroutineDispatcher = dispatcher
        override val mainImmediate: CoroutineDispatcher = dispatcher
    }
}
