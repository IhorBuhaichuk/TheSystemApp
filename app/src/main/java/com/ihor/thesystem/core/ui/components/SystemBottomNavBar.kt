package com.ihor.thesystem.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ihor.thesystem.core.navigation.Routes
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.SystemUiTestTags

@Composable
fun SystemBottomNavBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destination = backStackEntry?.destination
    val colors = SystemTheme.colors
    val items = listOf(
        NavBarItem(Routes.Status, NavGlyph.Status, "STATUS", SystemUiTestTags.BOTTOM_NAV_STATUS, destination?.hasRoute<Routes.Status>() == true),
        NavBarItem(Routes.Calendar, NavGlyph.Calendar, "CALENDAR", SystemUiTestTags.BOTTOM_NAV_CALENDAR, destination?.hasRoute<Routes.Calendar>() == true),
        NavBarItem(Routes.Cycle, NavGlyph.System, "SYSTEM", SystemUiTestTags.BOTTOM_NAV_SYSTEM, destination?.hasRoute<Routes.Cycle>() == true),
        NavBarItem(Routes.Statistics, NavGlyph.Statistics, "STATISTICS", SystemUiTestTags.BOTTOM_NAV_STATISTICS, destination?.hasRoute<Routes.Statistics>() == true),
        NavBarItem(Routes.Profile, NavGlyph.Profile, "PROFILE", SystemUiTestTags.BOTTOM_NAV_PROFILE, destination?.hasRoute<Routes.Profile>() == true)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .navigationBarsPadding()
            .padding(horizontal = 6.dp)
            .height(84.dp)
    ) {
        val navShape = systemLargePanelShape()
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(68.dp)
                .techSurface(
                    shape = navShape,
                    active = false,
                    accent = colors.accentPrimary,
                    role = TechSurfaceRole.Panel
                )
        ) {
            NavPanelTexture(modifier = Modifier.matchParentSize())
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(80.dp)
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            items.forEach { item ->
                NavIconButton(
                    modifier = Modifier.weight(1f),
                    item = item,
                    activeColor = colors.accentPrimary,
                    onClick = { navController.navigateTopLevel(item.route) }
                )
            }
        }
    }
}

@Composable
private fun NavPanelTexture(modifier: Modifier = Modifier) {
    val colors = SystemTheme.colors
    Canvas(modifier = modifier) {
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(colors.accentPrimary.copy(alpha = 0.09f), Color.Transparent),
                center = Offset(size.width * 0.50f, size.height * 0.02f),
                radius = size.width * 0.45f
            )
        )
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.035f),
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.20f)
                )
            )
        )
    }
}

@Composable
private fun NavIconButton(
    modifier: Modifier = Modifier,
    item: NavBarItem,
    activeColor: Color,
    onClick: () -> Unit
) {
    val colors = SystemTheme.colors
    val motion = SystemTheme.motion
    val selection by animateFloatAsState(
        targetValue = if (item.isSelected) 1f else 0f,
        animationSpec = tween(durationMillis = motion.quickStateMillis),
        label = "bottom_nav_selection"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (item.isSelected) 1.14f else 1f,
        animationSpec = tween(durationMillis = motion.progressMillis),
        label = "bottom_nav_icon_scale"
    )
    val itemHeight by animateFloatAsState(
        targetValue = if (item.isSelected) 74f else 66f,
        animationSpec = tween(durationMillis = motion.progressMillis),
        label = "bottom_nav_item_height"
    )
    val iconColor = if (item.isSelected) activeColor else Color(0xFFA3A7AE).copy(alpha = 0.78f)
    val labelColor = if (item.isSelected) activeColor else Color(0xFF9B9EA5).copy(alpha = 0.72f)
    val activeShape = systemLargePanelShape()

    Box(
        modifier = modifier
            .height(itemHeight.dp)
            .testTag(item.testTag)
            .padding(start = 1.dp, top = 0.dp, end = 1.dp, bottom = if (item.isSelected) 0.dp else 4.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selection > 0.01f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(start = 7.dp, top = 6.dp, end = 7.dp, bottom = 3.dp)
                    .blur(14.dp)
                    .background(activeColor.copy(alpha = 0.18f * selection), activeShape)
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .padding(start = 7.dp, top = 5.dp, end = 7.dp, bottom = 3.dp)
                    .graphicsLayer { alpha = selection }
                    .techSurface(
                        shape = activeShape,
                        active = true,
                        accent = activeColor,
                        role = TechSurfaceRole.Plate
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = if (item.isSelected) 7.dp else 6.dp, bottom = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(if (item.isSelected) 35.dp else 30.dp),
                contentAlignment = Alignment.Center
            ) {
                if (selection > 0.01f) {
                    Box(
                        modifier = Modifier
                            .size(43.dp)
                            .blur(10.dp)
                            .background(activeColor.copy(alpha = 0.30f * selection), SystemHexagonShape())
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(activeColor.copy(alpha = 0.12f * selection), SystemHexagonShape())
                    )
                }
                SystemNavGlyph(
                    glyph = item.glyph,
                    color = iconColor,
                    glow = selection,
                    modifier = Modifier
                        .size(if (item.isSelected) 26.dp else 22.dp)
                        .graphicsLayer {
                            scaleX = iconScale
                            scaleY = iconScale
                        }
                )
            }

            Text(
                text = item.label,
                color = labelColor,
                fontFamily = FontFamily.SansSerif,
                fontSize = if (item.isSelected) 9.sp else 8.sp,
                lineHeight = 12.sp,
                letterSpacing = 0.5.sp,
                fontWeight = if (item.isSelected) FontWeight.Black else FontWeight.SemiBold,
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private data class NavBarItem(
    val route: Routes,
    val glyph: NavGlyph,
    val label: String,
    val testTag: String,
    val isSelected: Boolean
)

private enum class NavGlyph {
    Status,
    Calendar,
    System,
    Statistics,
    Profile
}

@Composable
private fun SystemNavGlyph(
    glyph: NavGlyph,
    color: Color,
    glow: Float,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val stroke = 2.3.dp.toPx()
        val thinStroke = 1.6.dp.toPx()
        val glowColor = Color(0xFF19D7FF).copy(alpha = 0.22f * glow)
        if (glow > 0.01f) {
            drawCircle(
                color = glowColor,
                radius = size.minDimension * 0.62f,
                center = center
            )
        }
        when (glyph) {
            NavGlyph.Status -> {
                val path = Path().apply {
                    moveTo(size.width * 0.50f, size.height * 0.05f)
                    lineTo(size.width * 0.90f, size.height * 0.88f)
                    lineTo(size.width * 0.10f, size.height * 0.88f)
                    close()
                }
                drawPath(path, color.copy(alpha = 0.18f))
                drawPath(path, color, style = Stroke(width = stroke))
                drawLine(
                    color = color,
                    start = Offset(size.width * 0.34f, size.height * 0.67f),
                    end = Offset(size.width * 0.66f, size.height * 0.67f),
                    strokeWidth = thinStroke
                )
            }
            NavGlyph.Calendar -> {
                val x0 = size.width * 0.14f
                val y0 = size.height * 0.20f
                val x1 = size.width * 0.86f
                val y1 = size.height * 0.84f
                drawRect(
                    color = color.copy(alpha = 0.18f),
                    topLeft = Offset(x0, y0),
                    size = Size(x1 - x0, y1 - y0)
                )
                drawRect(
                    color = color,
                    topLeft = Offset(x0, y0),
                    size = Size(x1 - x0, y1 - y0),
                    style = Stroke(width = stroke)
                )
                drawLine(color, Offset(x0, size.height * 0.38f), Offset(x1, size.height * 0.38f), thinStroke)
                listOf(0.34f, 0.50f, 0.66f).forEach { x ->
                    listOf(0.55f, 0.71f).forEach { y ->
                        drawCircle(
                            color = color,
                            radius = 1.35.dp.toPx(),
                            center = Offset(size.width * x, size.height * y)
                        )
                    }
                }
                drawLine(color, Offset(size.width * 0.31f, size.height * 0.10f), Offset(size.width * 0.31f, size.height * 0.26f), thinStroke)
                drawLine(color, Offset(size.width * 0.69f, size.height * 0.10f), Offset(size.width * 0.69f, size.height * 0.26f), thinStroke)
            }
            NavGlyph.System -> {
                val outer = Path().apply {
                    moveTo(size.width * 0.50f, size.height * 0.03f)
                    lineTo(size.width * 0.88f, size.height * 0.25f)
                    lineTo(size.width * 0.88f, size.height * 0.74f)
                    lineTo(size.width * 0.50f, size.height * 0.97f)
                    lineTo(size.width * 0.12f, size.height * 0.74f)
                    lineTo(size.width * 0.12f, size.height * 0.25f)
                    close()
                }
                val inner = Path().apply {
                    moveTo(size.width * 0.50f, size.height * 0.28f)
                    lineTo(size.width * 0.68f, size.height * 0.39f)
                    lineTo(size.width * 0.68f, size.height * 0.61f)
                    lineTo(size.width * 0.50f, size.height * 0.72f)
                    lineTo(size.width * 0.32f, size.height * 0.61f)
                    lineTo(size.width * 0.32f, size.height * 0.39f)
                    close()
                }
                drawPath(outer, color.copy(alpha = 0.18f))
                drawPath(outer, color, style = Stroke(width = stroke))
                drawPath(inner, color.copy(alpha = 0.12f))
                drawPath(inner, color, style = Stroke(width = thinStroke))
            }
            NavGlyph.Statistics -> {
                val base = size.height * 0.86f
                listOf(0.22f to 0.42f, 0.40f to 0.64f, 0.58f to 0.30f, 0.76f to 0.74f).forEach { (x, h) ->
                    val barW = size.width * 0.10f
                    val top = base - size.height * h
                    drawRect(
                        color = color.copy(alpha = 0.22f),
                        topLeft = Offset(size.width * x - barW / 2f, top),
                        size = Size(barW, base - top)
                    )
                    drawRect(
                        color = color,
                        topLeft = Offset(size.width * x - barW / 2f, top),
                        size = Size(barW, base - top),
                        style = Stroke(width = thinStroke)
                    )
                }
            }
            NavGlyph.Profile -> {
                drawCircle(
                    color = color.copy(alpha = 0.14f),
                    radius = size.minDimension * 0.18f,
                    center = Offset(size.width * 0.50f, size.height * 0.30f)
                )
                drawCircle(
                    color = color,
                    radius = size.minDimension * 0.18f,
                    center = Offset(size.width * 0.50f, size.height * 0.30f),
                    style = Stroke(width = stroke)
                )
                drawArc(
                    color = color,
                    startAngle = 205f,
                    sweepAngle = 130f,
                    useCenter = false,
                    topLeft = Offset(size.width * 0.18f, size.height * 0.52f),
                    size = Size(size.width * 0.64f, size.height * 0.48f),
                    style = Stroke(width = stroke)
                )
            }
        }
    }
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
