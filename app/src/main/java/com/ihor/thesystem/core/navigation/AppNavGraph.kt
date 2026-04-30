package com.ihor.thesystem.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ihor.thesystem.core.theme.BackgroundDeep
import com.ihor.thesystem.core.ui.components.SystemBottomNavBar
import com.ihor.thesystem.feature.architect.ui.ArchitectScreen
import com.ihor.thesystem.feature.calendar.ui.CalendarSettingsScreen
import com.ihor.thesystem.feature.calendar.ui.CalendarScreen
import com.ihor.thesystem.feature.cycle.ui.CycleScreen
import com.ihor.thesystem.feature.statistics.ui.AnnualProgressionScreen
import com.ihor.thesystem.feature.statistics.ui.StatisticsScreen
import com.ihor.thesystem.feature.status.ui.StatusScreen

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
                CycleScreen()
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
                    }
                )
            }
            composable<Routes.AnnualMatrix> {
                AnnualProgressionScreen(navController = navController)
            }
        }
    }
}
