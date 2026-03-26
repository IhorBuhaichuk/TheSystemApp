package com.ihor.thesystem.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.ihor.thesystem.core.theme.BackgroundDeep
import com.ihor.thesystem.core.ui.components.SystemBottomNavBar
import com.ihor.thesystem.feature.architect.ui.ArchitectScreen
import com.ihor.thesystem.feature.mode.ui.ModeScreen
import com.ihor.thesystem.feature.statistics.ui.StatisticsScreen
import com.ihor.thesystem.feature.status.ui.StatusScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    Scaffold(
        containerColor = BackgroundDeep,
        bottomBar      = { SystemBottomNavBar(navController = navController) }
    ) { paddingValues ->
        NavHost(
            navController    = navController,
            startDestination = Routes.Status.route,
            modifier         = Modifier.padding(paddingValues)
        ) {
            composable(Routes.Status.route) {
                StatusScreen(navController = navController)
            }
            composable(Routes.Mode.route) {
                ModeScreen(navController = navController)
            }
            composable(Routes.Statistics.route) {
                StatisticsScreen(navController = navController)
            }
            composable(Routes.Architect.route) {
                ArchitectScreen(
                    onAcknowledge = {
                        // Після ознайомлення з директивами повертаємось на попередній екран
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}