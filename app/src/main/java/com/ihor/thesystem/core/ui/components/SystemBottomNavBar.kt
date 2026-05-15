package com.ihor.thesystem.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ihor.thesystem.core.navigation.Routes
import com.ihor.thesystem.core.theme.SystemTheme

@Composable
fun SystemBottomNavBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val colors = SystemTheme.colors
    val shapes = SystemTheme.shapes

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .navigationBarsPadding()
            .padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Surface(
            color = colors.surfaceGlassStrong.copy(alpha = 0.94f),
            shape = RoundedCornerShape(shapes.extraLarge),
            border = BorderStroke(1.dp, colors.borderSubtle),
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
                                listOf(
                                    Color.Transparent,
                                    colors.accentPrimary.copy(alpha = 0.18f),
                                    colors.accentAi.copy(alpha = 0.12f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 7.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavIconButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.Dashboard,
                        label = "Статус",
                        isSelected = destination?.hasRoute<Routes.Status>() == true,
                        activeColor = colors.accentPrimary,
                        onClick = {
                            navController.navigate(Routes.Status) {
                                popUpTo<Routes.Status> { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    )
                    NavIconButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.FitnessCenter,
                        label = "Цикл",
                        isSelected = destination?.hasRoute<Routes.Cycle>() == true,
                        activeColor = colors.accentPrimary,
                        onClick = {
                            navController.navigate(Routes.Cycle) {
                                popUpTo<Routes.Status> { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    )
                    NavIconButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.BarChart,
                        label = "Аналітика",
                        isSelected = destination?.hasRoute<Routes.Statistics>() == true,
                        activeColor = colors.accentPrimary,
                        onClick = {
                            navController.navigate(Routes.Statistics) {
                                popUpTo<Routes.Status> { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    )
                    NavIconButton(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Filled.AutoAwesome,
                        label = "ШІ",
                        isSelected = destination?.hasRoute<Routes.Architect>() == true,
                        activeColor = colors.accentAi,
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
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    val colors = SystemTheme.colors
    val shapes = SystemTheme.shapes
    val motion = SystemTheme.motion
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.08f else 1f,
        animationSpec = tween(motion.quickStateMillis),
        label = "nav_item_scale"
    )

    Column(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(shapes.large))
            .background(if (isSelected) activeColor.copy(alpha = 0.055f) else Color.Transparent)
            .border(
                width = 1.dp,
                color = if (isSelected) activeColor.copy(alpha = 0.16f) else Color.Transparent,
                shape = RoundedCornerShape(shapes.large)
            )
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
                        .background(activeColor.copy(alpha = 0.13f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .blur(12.dp)
                        .background(activeColor.copy(alpha = 0.18f), CircleShape)
                )
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) activeColor else colors.textSecondary.copy(alpha = 0.76f),
                modifier = Modifier
                    .size(23.dp)
                    .graphicsLayer(scaleX = scale, scaleY = scale)
            )
        }

        Text(
            text = label,
            color = if (isSelected) activeColor else colors.textSecondary.copy(alpha = 0.72f),
            fontSize = 9.sp,
            lineHeight = 11.sp,
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            overflow = TextOverflow.Ellipsis
        )

        if (isSelected) {
            Box(
                modifier = Modifier
                    .padding(top = 3.dp)
                    .size(4.dp)
                    .background(activeColor, CircleShape)
            )
        } else {
            Spacer(modifier = Modifier.padding(top = 3.dp).size(4.dp))
        }
    }
}
