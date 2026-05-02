package com.ihor.thesystem.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.toRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ihor.thesystem.core.theme.BackgroundDeep
import com.ihor.thesystem.core.ui.components.SystemBottomNavBar
import com.ihor.thesystem.feature.architect.ui.AnnualProgressionPlanScreen
import com.ihor.thesystem.feature.architect.ui.ArchitectScreen
import com.ihor.thesystem.feature.architect.ui.WorkoutAnalysisScreen
import com.ihor.thesystem.feature.calendar.ui.CalendarSettingsScreen
import com.ihor.thesystem.feature.calendar.ui.CalendarScreen
import com.ihor.thesystem.feature.cycle.ui.CycleScreen
import com.ihor.thesystem.feature.exercise_search.ui.ExercisePickerScreen
import com.ihor.thesystem.feature.statistics.ui.AnnualProgressionDetailsScreen
import com.ihor.thesystem.feature.statistics.ui.StatisticsScreen
import com.ihor.thesystem.feature.status.ui.StatusScreen
import com.ihor.thesystem.feature.status.viewmodel.WorkoutViewModel

@Composable
fun AppNavGraph(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val showBottomNav =
        destination?.hasRoute<Routes.Status>() == true ||
            destination?.hasRoute<Routes.Cycle>() == true ||
            destination?.hasRoute<Routes.Statistics>() == true ||
            destination?.hasRoute<Routes.Architect>() == true

    Scaffold(
        containerColor = BackgroundDeep,
        bottomBar = {
            if (showBottomNav) {
                SystemBottomNavBar(navController = navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController    = navController,
            startDestination = Routes.Status,
            modifier         = Modifier.padding(paddingValues)
        ) {
            composable<Routes.Status> {
                StatusScreen(navController = navController)
            }
            composable<Routes.Cycle> {
                CycleScreen(navController = navController)
            }
            composable<Routes.Calendar> {
                CalendarScreen(navController = navController)
            }
            composable<Routes.CalendarSettings> {
                CalendarSettingsScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable<Routes.Statistics> {
                StatisticsScreen(navController = navController)
            }
            composable<Routes.Architect> {
                ArchitectScreen(
                    onAcknowledge = {
                        navController.popBackStack()
                    },
                    onOpenAnnualProgression = {
                        navController.navigate(Routes.AnnualProgressionPlan)
                    },
                    onOpenWorkoutAnalysis = {
                        navController.navigate(Routes.WorkoutAnalysis)
                    }
                )
            }
            composable<Routes.AnnualProgressionDetails> {
                AnnualProgressionDetailsScreen(
                    onBack = { navController.popBackStack() },
                    onCreateInAi = {
                        navController.navigate(Routes.AnnualProgressionPlan)
                    }
                )
            }
            composable<Routes.AnnualProgressionPlan> { backStackEntry ->
                val selectedExerciseIdFlow = backStackEntry.savedStateHandle
                    .getStateFlow<Int?>(Routes.PICKER_RESULT_EXERCISE_ID, null)
                val selectedExerciseId by selectedExerciseIdFlow.collectAsStateWithLifecycle()
                val viewModel: com.ihor.thesystem.feature.architect.viewmodel.AnnualProgressionPlanViewModel =
                    hiltViewModel()

                androidx.compose.runtime.LaunchedEffect(selectedExerciseId) {
                    val exerciseId = selectedExerciseId ?: return@LaunchedEffect
                    viewModel.onExerciseSelected(exerciseId)
                    backStackEntry.savedStateHandle.remove<Int>(Routes.PICKER_RESULT_EXERCISE_ID)
                }

                AnnualProgressionPlanScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onOpenExercisePicker = {
                        navController.navigate(
                            Routes.ExercisePicker(source = Routes.PICKER_SOURCE_ANNUAL)
                        )
                    }
                )
            }
            composable<Routes.WorkoutAnalysis> {
                WorkoutAnalysisScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable<Routes.ExercisePicker> { backStackEntry ->
                val route = backStackEntry.toRoute<Routes.ExercisePicker>()
                val workoutViewModel: WorkoutViewModel = hiltViewModel()
                ExercisePickerScreen(
                    onBack = { navController.popBackStack() },
                    actionLabel = when (route.source) {
                        Routes.PICKER_SOURCE_CYCLE -> "Додати"
                        else -> "Вибрати"
                    },
                    onSelectExercise = { exercise ->
                        when (route.source) {
                            Routes.PICKER_SOURCE_CYCLE -> {
                                workoutViewModel.onAddExerciseToDay(
                                    exerciseId = exercise.id,
                                    cycleDay = route.cycleDay.takeIf { it > 0 } ?: 1
                                )
                                navController.popBackStack()
                            }
                            Routes.PICKER_SOURCE_ANNUAL -> {
                                navController.previousBackStackEntry
                                    ?.savedStateHandle
                                    ?.set(Routes.PICKER_RESULT_EXERCISE_ID, exercise.id)
                                navController.popBackStack()
                            }
                            else -> navController.popBackStack()
                        }
                    }
                )
            }
        }
    }
}
