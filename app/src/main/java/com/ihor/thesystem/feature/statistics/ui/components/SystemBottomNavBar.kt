package com.ihor.thesystem.core.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ihor.thesystem.core.navigation.Routes
import com.ihor.thesystem.core.theme.*

@Composable
fun SystemBottomNavBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Surface(
            color = Color(0xFF0A0A10).copy(alpha = 0.8f),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavIconButton(
                    icon = Icons.Filled.Dashboard,
                    isSelected = destination?.hasRoute<Routes.Status>() == true,
                    activeColor = NeonCyan,
                    onClick = {
                        navController.navigate(Routes.Status) {
                            popUpTo<Routes.Status> { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
                NavIconButton(
                    icon = Icons.Filled.FlashOn,
                    isSelected = destination?.hasRoute<Routes.Mode>() == true,
                    activeColor = NeonGold,
                    onClick = {
                        navController.navigate(Routes.Mode) {
                            popUpTo<Routes.Status> { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
                NavIconButton(
                    icon = Icons.Filled.CalendarToday,
                    isSelected = destination?.hasRoute<Routes.Calendar>() == true,
                    activeColor = GlitchPink,
                    onClick = {
                        navController.navigate(Routes.Calendar) {
                            popUpTo<Routes.Status> { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
                NavIconButton(
                    icon = Icons.Filled.BarChart,
                    isSelected = destination?.hasRoute<Routes.Statistics>() == true,
                    activeColor = NeonGreen,
                    onClick = {
                        navController.navigate(Routes.Statistics) {
                            popUpTo<Routes.Status> { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
                NavIconButton(
                    icon = Icons.Filled.AutoAwesome,
                    isSelected = destination?.hasRoute<Routes.Architect>() == true,
                    activeColor = GlitchPink,
                    onClick = {
                        navController.navigate(Routes.Architect) {
                            popUpTo<Routes.Status> { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun NavIconButton(
    icon: ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(if (isSelected) 1.2f else 1f, label = "scale")
    val alpha by animateFloatAsState(if (isSelected) 1f else 0.4f, label = "alpha")

    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .blur(20.dp)
                    .background(activeColor.copy(alpha = 0.3f), CircleShape)
            )
        }
        
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) activeColor else Color.White,
            modifier = Modifier
                .size(24.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha)
        )
        
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-8).dp)
                    .size(4.dp, 4.dp)
                    .background(activeColor, CircleShape)
            )
        }
    }
}
