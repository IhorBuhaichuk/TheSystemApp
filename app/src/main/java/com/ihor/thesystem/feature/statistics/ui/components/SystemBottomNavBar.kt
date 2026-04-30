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
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Surface(
            color = Color(0xFF050A13).copy(alpha = 0.94f),
            shape = RoundedCornerShape(26.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.13f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(78.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth(0.66f)
                        .height(26.dp)
                        .blur(18.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, Primary.copy(alpha = 0.22f), Color.Transparent)
                            )
                        )
                )

                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 7.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavIconButton(
                        icon = Icons.Filled.Dashboard,
                        label = "Статус",
                        isSelected = destination?.hasRoute<Routes.Status>() == true,
                        activeColor = Primary,
                        onClick = {
                            navController.navigate(Routes.Status) {
                                popUpTo<Routes.Status> { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    )
                    NavIconButton(
                        icon = Icons.Filled.FitnessCenter,
                        label = "Цикл",
                        isSelected = destination?.hasRoute<Routes.Cycle>() == true,
                        activeColor = Primary,
                        onClick = {
                            navController.navigate(Routes.Cycle) {
                                popUpTo<Routes.Status> { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    )
                    NavIconButton(
                        icon = Icons.Filled.BarChart,
                        label = "Аналітика",
                        isSelected = destination?.hasRoute<Routes.Statistics>() == true,
                        activeColor = Primary,
                        onClick = {
                            navController.navigate(Routes.Statistics) {
                                popUpTo<Routes.Status> { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    )
                    NavIconButton(
                        icon = Icons.Filled.AutoAwesome,
                        label = "ШІ",
                        isSelected = destination?.hasRoute<Routes.Architect>() == true,
                        activeColor = AccentAi,
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
}

@Composable
private fun NavIconButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(if (isSelected) 1.2f else 1f, label = "scale")

    Column(
        modifier = Modifier
            .width(78.dp)
            .height(64.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(38.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(activeColor.copy(alpha = 0.18f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .blur(12.dp)
                        .background(activeColor.copy(alpha = 0.22f), CircleShape)
                )
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) activeColor else OnSurfaceVariant.copy(alpha = 0.76f),
                modifier = Modifier
                    .size(23.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
            )
        }

        Text(
            text = label,
            color = if (isSelected) activeColor else OnSurfaceVariant.copy(alpha = 0.72f),
            fontSize = 9.sp,
            lineHeight = 11.sp,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
            maxLines = 1,
            textAlign = TextAlign.Center
        )

        if (isSelected) {
            Box(
                modifier = Modifier
                    .padding(top = 3.dp)
                    .size(4.dp)
                    .background(activeColor, CircleShape)
            )
        }
    }
}
