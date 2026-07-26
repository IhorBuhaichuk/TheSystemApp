package com.ihor.thesystem.core.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.toRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.components.SystemBottomNavBar
import com.ihor.thesystem.domain.model.AppStartDestination
import com.ihor.thesystem.feature.architect.ui.AnnualProgressionPlanScreen
import com.ihor.thesystem.feature.architect.ui.ArchitectScreen
import com.ihor.thesystem.feature.architect.ui.WorkoutAnalysisScreen
import com.ihor.thesystem.feature.calendar.ui.CalendarSettingsScreen
import com.ihor.thesystem.feature.calendar.ui.CalendarScreen
import com.ihor.thesystem.feature.cycle.ui.CycleScreen
import com.ihor.thesystem.feature.exercise_search.ui.ExercisePickerScreen
import com.ihor.thesystem.feature.onboarding.ui.OnboardingScreen
import com.ihor.thesystem.feature.profile.ui.ProfileScreen
import com.ihor.thesystem.feature.statistics.ui.AnnualProgressionDetailsScreen
import com.ihor.thesystem.feature.statistics.ui.StatisticsScreen
import com.ihor.thesystem.feature.status.ui.StatusScreen
import com.ihor.thesystem.feature.status.viewmodel.WorkoutViewModel
import kotlin.math.abs

@Composable
fun AppNavGraph(
    navController: NavHostController,
    appEntryViewModel: AppEntryViewModel = hiltViewModel()
) {
    val startDestinationState by appEntryViewModel.startDestination.collectAsStateWithLifecycle()
    val appStartDestination = startDestinationState
    if (appStartDestination == null) {
        AppEntryLoading()
        return
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val showBottomNav =
        destination?.hasRoute<Routes.Status>() == true ||
            destination?.hasRoute<Routes.Calendar>() == true ||
            destination?.hasRoute<Routes.Cycle>() == true ||
            destination?.hasRoute<Routes.Statistics>() == true ||
            destination?.hasRoute<Routes.Profile>() == true

    Scaffold(
        containerColor = SystemTheme.colors.background,
        bottomBar = {
            if (showBottomNav) {
                SystemBottomNavBar(navController = navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController    = navController,
            startDestination = appStartDestination.toRoute(),
            modifier         = Modifier
                .padding(paddingValues)
                .topLevelSwipeNavigation(
                    enabled = showBottomNav,
                    destination = destination,
                    navController = navController
                ),
            enterTransition = { topLevelEnterTransition() },
            exitTransition = { topLevelExitTransition() },
            popEnterTransition = { topLevelEnterTransition() },
            popExitTransition = { topLevelExitTransition() }
        ) {
            composable<Routes.Onboarding> {
                OnboardingScreen(
                    onCompleted = {
                        navController.navigate(Routes.Status) {
                            popUpTo<Routes.Onboarding> {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
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
            composable<Routes.Profile> {
                ProfileScreen(navController = navController)
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
                        navController.navigate(Routes.WorkoutAnalysis())
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
                val workoutViewModel = if (route.source == Routes.PICKER_SOURCE_CYCLE) {
                    hiltViewModel<WorkoutViewModel>()
                } else {
                    null
                }
                ExercisePickerScreen(
                    onBack = { navController.popBackStack() },
                    actionLabel = when (route.source) {
                        Routes.PICKER_SOURCE_CYCLE -> "Додати"
                        else -> "Вибрати"
                    },
                    onSelectExercise = { exercise ->
                        when (route.source) {
                            Routes.PICKER_SOURCE_CYCLE -> {
                                workoutViewModel?.onAddExerciseToDay(
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

@Composable
private fun AppEntryLoading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SystemTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = SystemTheme.colors.accentPrimary)
    }
}

private fun AppStartDestination.toRoute(): Routes =
    when (this) {
        AppStartDestination.ONBOARDING -> Routes.Onboarding
        AppStartDestination.STATUS -> Routes.Status
    }

private val topLevelRoutes = listOf(
    Routes.Status,
    Routes.Calendar,
    Routes.Cycle,
    Routes.Statistics,
    Routes.Profile
)

private fun AnimatedContentTransitionScope<NavBackStackEntry>.topLevelEnterTransition(): EnterTransition {
    val initialIndex = initialState.destination.topLevelIndex()
    val targetIndex = targetState.destination.topLevelIndex()
    if (initialIndex != -1 && targetIndex != -1 && initialIndex != targetIndex) {
        return EnterTransition.None
    }
    return fadeIn(animationSpec = tween(durationMillis = 120))
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.topLevelExitTransition(): ExitTransition {
    val initialIndex = initialState.destination.topLevelIndex()
    val targetIndex = targetState.destination.topLevelIndex()
    if (initialIndex != -1 && targetIndex != -1 && initialIndex != targetIndex) {
        return ExitTransition.None
    }
    return fadeOut(animationSpec = tween(durationMillis = 90))
}

private fun Modifier.topLevelSwipeNavigation(
    enabled: Boolean,
    destination: NavDestination?,
    navController: NavHostController
): Modifier {
    if (!enabled) return this
    val currentIndex = destination.topLevelIndex()
    if (currentIndex == -1) return this

    return pointerInput(currentIndex) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            var totalX = 0f
            var totalY = 0f
            var navigated = false
            val threshold = 88.dp.toPx()
            val verticalCancelThreshold = 42.dp.toPx()

            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                val delta = change.positionChange()
                totalX += delta.x
                totalY += delta.y

                if (!navigated &&
                    abs(totalY) > verticalCancelThreshold &&
                    abs(totalY) > abs(totalX) * 1.15f
                ) {
                    break
                }

                if (!navigated &&
                    abs(totalX) > threshold &&
                    abs(totalX) > abs(totalY) * 1.35f
                ) {
                    val nextIndex = if (totalX < 0f) currentIndex + 1 else currentIndex - 1
                    topLevelRoutes.getOrNull(nextIndex)?.let { route ->
                        navigated = true
                        change.consume()
                        navController.navigateTopLevel(route)
                    }
                } else if (navigated) {
                    change.consume()
                }

                if (!change.pressed) break
            }
        }
    }
}

private fun NavDestination?.topLevelIndex(): Int =
    when {
        this?.hasRoute<Routes.Status>() == true -> 0
        this?.hasRoute<Routes.Calendar>() == true -> 1
        this?.hasRoute<Routes.Cycle>() == true -> 2
        this?.hasRoute<Routes.Statistics>() == true -> 3
        this?.hasRoute<Routes.Profile>() == true -> 4
        else -> -1
    }

private fun NavHostController.navigateTopLevel(route: Routes) {
    navigate(route) {
        popUpTo<Routes.Status> {
            inclusive = false
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
