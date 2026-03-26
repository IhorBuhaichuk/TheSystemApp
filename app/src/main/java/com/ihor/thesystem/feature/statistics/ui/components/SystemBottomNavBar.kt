package com.ihor.thesystem.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ihor.thesystem.core.navigation.Routes
import com.ihor.thesystem.core.theme.*

@Composable
fun SystemBottomNavBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute   = backStackEntry?.destination?.route

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundDeep)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            NavIconButton(
                icon       = Icons.Filled.Home,
                isSelected = currentRoute == Routes.Status.route,
                modifier   = Modifier.weight(1f),
                onClick    = {
                    navController.navigate(Routes.Status.route) {
                        popUpTo(Routes.Status.route) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
            NavIconButton(
                icon       = Icons.Filled.FitnessCenter,
                isSelected = currentRoute == Routes.Mode.route,
                modifier   = Modifier.weight(1f),
                onClick    = {
                    navController.navigate(Routes.Mode.route) {
                        popUpTo(Routes.Status.route) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
            NavIconButton(
                icon       = Icons.Filled.Person,
                isSelected = currentRoute == Routes.Statistics.route,
                modifier   = Modifier.weight(1f),
                onClick    = {
                    navController.navigate(Routes.Statistics.route) {
                        popUpTo(Routes.Status.route) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
            NavIconButton(
                icon       = Icons.Filled.AutoAwesome,
                isSelected = currentRoute == Routes.Architect.route,
                modifier   = Modifier.weight(1f),
                onClick    = {
                    navController.navigate(Routes.Architect.route) {
                        popUpTo(Routes.Status.route) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@Composable
private fun NavIconButton(
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) NeonCyan else NeonCyanDim.copy(alpha = 0.35f)
    val iconTint    = if (isSelected) NeonCyan else TextSecondary
    val bgColor     = if (isSelected) NeonCyan.copy(alpha = 0.10f) else PanelSurface
    val borderWidth = if (isSelected) 1.5.dp else 1.dp
    val shape       = RoundedCornerShape(22.dp)

    Box(
        modifier = modifier
            .height(46.dp)
            .border(borderWidth, borderColor, shape)
            .clip(shape)
            .background(bgColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = iconTint,
            modifier           = Modifier.size(22.dp)
        )
    }
}