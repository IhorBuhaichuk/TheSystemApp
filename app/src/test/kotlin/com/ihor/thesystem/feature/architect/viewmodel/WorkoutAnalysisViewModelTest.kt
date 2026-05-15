package com.ihor.thesystem.feature.architect.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.ihor.thesystem.R
import com.ihor.thesystem.core.ui.UiState
import com.ihor.thesystem.core.ui.UiText
import com.ihor.thesystem.core.util.DispatcherProvider
import com.ihor.thesystem.domain.model.MotivationComponentScores
import com.ihor.thesystem.domain.model.MotivationLevel
import com.ihor.thesystem.domain.model.MotivationLevelResult
import com.ihor.thesystem.domain.model.WorkoutAnalysisData
import com.ihor.thesystem.domain.model.WorkoutExecutionAnalysis
import com.ihor.thesystem.domain.usecase.GetWorkoutAnalysisUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class WorkoutAnalysisViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getWorkoutAnalysis: GetWorkoutAnalysisUseCase = mockk()

    @Test
    fun `loads workout analysis into content state`() {
        val analysis = workoutAnalysis()
        coEvery { getWorkoutAnalysis.invoke() } returns analysis

        val viewModel = viewModel()
        mainDispatcherRule.advanceUntilIdle()

        assertEquals(UiState.Content(analysis), viewModel.uiState.value)
    }

    @Test
    fun `keeps empty analysis as content without treating it as failure`() {
        coEvery { getWorkoutAnalysis.invoke() } returns null

        val viewModel = viewModel()
        mainDispatcherRule.advanceUntilIdle()

        assertEquals(UiState.Content(null), viewModel.uiState.value)
    }

    @Test
    fun `maps load failure to localized error state`() {
        coEvery { getWorkoutAnalysis.invoke() } throws IllegalStateException("database unavailable")

        val viewModel = viewModel()
        mainDispatcherRule.advanceUntilIdle()

        val state = viewModel.uiState.value as UiState.Error
        val message = state.message as UiText.StringResource
        assertEquals(R.string.error_workout_analysis_failed, message.resId)
    }

    @Test
    fun `loads routed workout analysis when session id is present`() {
        val analysis = workoutAnalysis()
        coEvery { getWorkoutAnalysis.invoke(42L) } returns analysis

        val viewModel = viewModel(SavedStateHandle(mapOf("sessionId" to 42L)))
        mainDispatcherRule.advanceUntilIdle()

        assertEquals(UiState.Content(analysis), viewModel.uiState.value)
        coVerify(exactly = 1) { getWorkoutAnalysis.invoke(42L) }
        coVerify(exactly = 0) { getWorkoutAnalysis.invoke() }
    }

    private fun viewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()): WorkoutAnalysisViewModel =
        WorkoutAnalysisViewModel(
            getWorkoutAnalysis = getWorkoutAnalysis,
            dispatchers = TestDispatcherProvider(mainDispatcherRule.dispatcher),
            savedStateHandle = savedStateHandle
        )

    private fun workoutAnalysis(): WorkoutAnalysisData =
        WorkoutAnalysisData(
            sessionTimestamp = 1_700_000_000_000,
            workoutName = "Push Day",
            execution = WorkoutExecutionAnalysis(
                completedSets = 3,
                plannedSets = 3,
                completedExercises = 1,
                skippedExercises = 0
            ),
            exerciseProgress = emptyList(),
            annualProgress = emptyList(),
            recommendations = emptyList(),
            motivationLevel = MotivationLevelResult(
                finalScore = 75,
                level = MotivationLevel.ADVANCED,
                title = "Advanced",
                description = "Stable progress",
                componentScores = MotivationComponentScores(
                    personalProgressScore = 80,
                    planProgressScore = 70,
                    consistencyScore = 75,
                    strengthBenchmarkScore = 75
                )
            ),
            aiFeedback = null
        )
}

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
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

private class TestDispatcherProvider(
    private val dispatcher: CoroutineDispatcher
) : DispatcherProvider {
    override val main: CoroutineDispatcher = dispatcher
    override val io: CoroutineDispatcher = dispatcher
    override val default: CoroutineDispatcher = dispatcher
    override val mainImmediate: CoroutineDispatcher = dispatcher
}
