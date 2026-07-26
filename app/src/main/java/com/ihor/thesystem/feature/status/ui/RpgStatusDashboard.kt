package com.ihor.thesystem.feature.status.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.SystemCardPadding
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemScreenPadding
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.theme.SystemDisplayFamily
import com.ihor.thesystem.core.ui.SystemUiTestTags
import com.ihor.thesystem.core.ui.toSystemSentenceCase
import com.ihor.thesystem.core.ui.components.DarkGlassCard
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemHoodBadge
import com.ihor.thesystem.core.ui.components.SystemHexagonShape
import com.ihor.thesystem.core.ui.components.SystemProgressBar
import com.ihor.thesystem.core.ui.components.SystemSectionHeader
import com.ihor.thesystem.core.ui.components.SystemWeekDayModel
import com.ihor.thesystem.core.ui.components.SystemWeekDayStatus
import com.ihor.thesystem.core.ui.components.SystemWeekDayVisualType
import com.ihor.thesystem.core.ui.components.TechSurfaceRole
import com.ihor.thesystem.core.ui.components.systemLargePanelShape
import com.ihor.thesystem.core.ui.components.systemClickable
import com.ihor.thesystem.core.ui.components.systemPlateShape
import com.ihor.thesystem.core.ui.components.techSurface
import com.ihor.thesystem.domain.model.BossFight
import com.ihor.thesystem.domain.model.BossFightStatus
import com.ihor.thesystem.domain.model.BossFightTargetMetric
import com.ihor.thesystem.feature.status.viewmodel.QuestUiModel
import com.ihor.thesystem.feature.status.viewmodel.StatusUiData
import com.ihor.thesystem.feature.status.viewmodel.StatusWeekDayStatus
import com.ihor.thesystem.feature.status.viewmodel.StatusWeekDayUiModel
import com.ihor.thesystem.feature.status.viewmodel.StatusWeekDayVisualType
import com.ihor.thesystem.feature.status.viewmodel.TaskUiModel
import com.ihor.thesystem.feature.status.viewmodel.TodayOrderUiModel
import com.ihor.thesystem.feature.status.viewmodel.TodoUiModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private enum class StatusDashboardState {
    ACTIONS,
    STATUS
}

@Composable
fun RpgStatusDashboard(
    data: StatusUiData,
    onAvatarSelected: (Uri) -> Unit,
    onEditNameTap: () -> Unit,
    onStartWorkout: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenWorkoutSettings: () -> Unit,
    onTaskToggled: (TodoUiModel) -> Unit,
    onAddTask: (Int) -> Unit,
    onAddMicrotask: (TodoUiModel) -> Unit,
    onTodosReordered: (List<Int>) -> Unit,
    onRemoveTask: (Int) -> Unit
) {
    val colors = SystemTheme.colors
    val pinnedWeekHeight = 116.dp
    val transitionOffsetPx = with(LocalDensity.current) { 24.dp.roundToPx() }
    var dashboardState by remember { mutableStateOf(StatusDashboardState.ACTIONS) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag(SystemUiTestTags.STATUS_DASHBOARD)
            .statusDashboardSwipe(
                state = dashboardState,
                onStateChange = { dashboardState = it }
            )
    ) {
        AnimatedContent(
            targetState = dashboardState,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = pinnedWeekHeight + 8.dp, bottom = 20.dp),
            transitionSpec = {
                val enteringStatus = targetState == StatusDashboardState.STATUS
                (
                    fadeIn(animationSpec = tween(durationMillis = 160)) +
                        slideInHorizontally(
                            animationSpec = tween(durationMillis = 280),
                            initialOffsetX = { if (enteringStatus) transitionOffsetPx else -transitionOffsetPx }
                        )
                    ).togetherWith(
                    fadeOut(animationSpec = tween(durationMillis = 140)) +
                        slideOutHorizontally(
                            animationSpec = tween(durationMillis = 280),
                            targetOffsetX = { if (enteringStatus) -transitionOffsetPx else transitionOffsetPx }
                        )
                ).using(SizeTransform(clip = false))
            },
            label = "status_dashboard_state"
        ) { state ->
            when (state) {
                StatusDashboardState.ACTIONS -> StatusActionsContent(
                    data = data,
                    onStartWorkout = onStartWorkout,
                    onTaskToggled = onTaskToggled,
                    onAddTask = onAddTask,
                    onAddMicrotask = onAddMicrotask,
                    onTodosReordered = onTodosReordered,
                    onRemoveTask = onRemoveTask
                )

                StatusDashboardState.STATUS -> StatusInfoContent(
                    data = data,
                    onAvatarSelected = onAvatarSelected,
                    onEditNameTap = onEditNameTap,
                    onOpenCalendar = onOpenCalendar
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(pinnedWeekHeight)
                .padding(horizontal = SystemScreenPadding)
                .padding(top = 12.dp, bottom = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            WeekPreviewBlock(
                days = data.weekPreview,
                onOpenCalendar = onOpenCalendar,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StatusActionsContent(
    data: StatusUiData,
    onStartWorkout: () -> Unit,
    onTaskToggled: (TodoUiModel) -> Unit,
    onAddTask: (Int) -> Unit,
    onAddMicrotask: (TodoUiModel) -> Unit,
    onTodosReordered: (List<Int>) -> Unit,
    onRemoveTask: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag(SystemUiTestTags.STATUS_ACTIONS_CONTENT)
            .padding(horizontal = SystemScreenPadding),
        verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)
    ) {
        TodayOrderBlock(
            order = data.todayOrder,
            onStartWorkout = onStartWorkout
        )
        TodoBlock(
            todos = data.todos,
            onTaskToggled = onTaskToggled,
            onAddTask = onAddTask,
            onAddMicrotask = onAddMicrotask,
            onTodosReordered = onTodosReordered,
            onRemoveTask = onRemoveTask
        )
        ActionsSwipeHint()
    }
}

@Composable
private fun StatusInfoContent(
    data: StatusUiData,
    onAvatarSelected: (Uri) -> Unit,
    onEditNameTap: () -> Unit,
    onOpenCalendar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag(SystemUiTestTags.STATUS_INFO_CONTENT)
            .padding(horizontal = SystemScreenPadding),
        verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)
    ) {
        StatusHeroPanel(
            data = data,
            onAvatarSelected = onAvatarSelected,
            onEditNameTap = onEditNameTap,
            onOpenCalendar = onOpenCalendar
        )
        TodayProgressBlock(data = data)
        RecommendationPanel(order = data.todayOrder)
        StatusSwipeHint()
    }
}

private fun Modifier.statusDashboardSwipe(
    state: StatusDashboardState,
    onStateChange: (StatusDashboardState) -> Unit
): Modifier =
    pointerInput(state) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            var totalX = 0f
            var totalY = 0f
            var switched = false
            val threshold = 72.dp.toPx()
            val maxGestureMillis = 650L

            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                val delta = change.positionChange()
                totalX += delta.x
                totalY += delta.y

                if (!switched && change.uptimeMillis - down.uptimeMillis <= maxGestureMillis) {
                    state.resolveSwipeTarget(
                        totalX = totalX,
                        totalY = totalY,
                        threshold = threshold
                    )?.let { targetState ->
                        switched = true
                        change.consume()
                        onStateChange(targetState)
                    }
                }

                if (switched) {
                    change.consume()
                }
                if (!change.pressed) break
            }
        }
    }

private fun StatusDashboardState.resolveSwipeTarget(
    totalX: Float,
    totalY: Float,
    threshold: Float
): StatusDashboardState? {
    if (abs(totalX) < threshold || abs(totalX) < abs(totalY) * 1.60f) {
        return null
    }
    return when {
        this == StatusDashboardState.ACTIONS && totalX < 0f -> StatusDashboardState.STATUS
        this == StatusDashboardState.STATUS && totalX > 0f -> StatusDashboardState.ACTIONS
        else -> null
    }
}

@Composable
private fun StatusHeroPanel(
    data: StatusUiData,
    onAvatarSelected: (Uri) -> Unit,
    onEditNameTap: () -> Unit,
    onOpenCalendar: () -> Unit
) {
    val colors = SystemTheme.colors
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let(onAvatarSelected) }
    )
    val progress = remember(data.xpTotal, data.xpMax) {
        if (data.xpMax > 0) (data.xpTotal.toFloat() / data.xpMax).coerceIn(0f, 1f) else 0f
    }
    val shape = systemLargePanelShape()
    val displayName = data.playerName.asPlayerDisplayName()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(172.dp)
            .techSurface(
                shape = shape,
                active = true,
                accent = colors.accentPrimary,
                role = TechSurfaceRole.Panel
            )
            .padding(start = 18.dp, top = 24.dp, end = 22.dp, bottom = 13.dp)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(colors.accentPrimary.copy(alpha = 0.14f), Color.Transparent),
                    center = Offset(size.width * 0.12f, size.height * 0.54f),
                    radius = size.width * 0.34f
                ),
                radius = size.width * 0.34f,
                center = Offset(size.width * 0.12f, size.height * 0.54f)
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 5.dp)
                .size(92.dp)
        ) {
            SystemHoodBadge(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 121.dp, top = 20.dp)
                .width(96.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text(
                text = displayName,
                modifier = Modifier.systemClickable(onClick = onEditNameTap),
                style = MaterialTheme.typography.displayLarge.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 31.sp,
                    lineHeight = 34.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        HeroXpPanel(
            xpText = "${data.xpTotal} / ${data.xpMax} досвіду",
            percentText = "${(progress * 100).roundToInt()}%",
            progress = progress,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 116.dp)
                .fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HeroRankPlate(label = "Ранг", value = data.globalRank.name, onClick = onOpenCalendar)
            HeroRankPlate(label = "Рівень", value = data.level.toString(), onClick = onOpenCalendar)
        }
    }
}

private fun String.asPlayerDisplayName(): String {
    val locale = Locale.forLanguageTag("uk-UA")
    val cleaned = trim().ifEmpty { "Ігор" }
    val isAllCaps = cleaned.any { it.isLetter() } && cleaned == cleaned.uppercase(locale)
    return if (isAllCaps) {
        cleaned.lowercase(locale).replaceFirstChar { first ->
            if (first.isLowerCase()) first.titlecase(locale) else first.toString()
        }
    } else {
        cleaned
    }
}

@Composable
private fun HeroHeaderBackdrop(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    Canvas(modifier = modifier) {
        val cut = 11.dp.toPx()
        val panel = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height)
            lineTo(size.width * 0.34f, size.height)
            lineTo(size.width * 0.29f, size.height - cut)
            lineTo(0f, size.height - cut)
            close()
        }
        drawPath(
            path = panel,
            brush = Brush.linearGradient(
                listOf(Color(0xFF080D12), Color(0xFF05080D), Color(0xFF010204)),
                start = Offset.Zero,
                end = Offset(size.width, size.height)
            )
        )
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(colors.accentPrimary.copy(alpha = 0.18f), Color.Transparent),
                center = Offset(size.width * 0.12f, size.height * 0.25f),
                radius = size.width * 0.34f
            )
        )
        drawPath(
            path = panel,
            color = Color.White.copy(alpha = 0.12f),
            style = Stroke(width = 1.dp.toPx())
        )
        drawCircle(
            color = colors.accentPrimary.copy(alpha = 0.08f + progress * 0.08f),
            radius = 3.dp.toPx(),
            center = Offset(size.width * 0.91f, size.height * 0.23f)
        )
    }
}

@Composable
private fun HeroRankPlate(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .width(56.dp)
            .height(50.dp)
            .techSurface(
                shape = shape,
                active = false,
                accent = colors.accentPrimary,
                role = TechSurfaceRole.Plate
            )
            .systemClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label.toSystemSentenceCase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = colors.textSecondary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    lineHeight = 13.sp
                ),
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 23.sp,
                    lineHeight = 25.sp
                ),
                maxLines = 1
            )
        }
    }
}

private class HeroSlantShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val slant = size.width * 0.18f
        val path = Path().apply {
            moveTo(slant, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width - slant, size.height)
            lineTo(0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
private fun HeroXpPanel(
    xpText: String,
    percentText: String,
    progress: Float,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = modifier
            .height(54.dp)
            .techSurface(
                shape = shape,
                active = false,
                accent = colors.accentPrimary,
                role = TechSurfaceRole.Plate
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 17.dp, top = 9.dp, end = 16.dp, bottom = 9.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = xpText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = colors.accentPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        lineHeight = 18.sp
                    )
                )
                Text(
                    text = percentText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = colors.textSecondary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        lineHeight = 17.sp
                    )
                )
            }
            SystemProgressBar(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
            )
        }
    }
}

@Composable
private fun RecommendationPanel(
    order: TodayOrderUiModel,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    val purple = Color(0xFFB76CFF)
    val shape = systemLargePanelShape()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(116.dp)
            .techSurface(
                shape = shape,
                active = true,
                accent = purple,
                role = TechSurfaceRole.Panel
            )
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(purple.copy(alpha = 0.20f), Color.Transparent),
                    center = Offset(size.width * 0.14f, size.height * 0.55f),
                    radius = size.width * 0.24f
                ),
                radius = size.width * 0.24f,
                center = Offset(size.width * 0.14f, size.height * 0.55f)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 26.dp, top = 23.dp, end = 24.dp, bottom = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RecommendationStarBadge(accent = purple, modifier = Modifier.size(64.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Рекомендація системи",
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = purple,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        lineHeight = 16.sp,
                        letterSpacing = 1.7.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = order.reason,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = colors.textPrimary.copy(alpha = 0.92f),
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        lineHeight = 22.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun RecommendationStarBadge(
    accent: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val hex = Path().apply {
            moveTo(size.width * 0.50f, size.height * 0.04f)
            lineTo(size.width * 0.90f, size.height * 0.27f)
            lineTo(size.width * 0.90f, size.height * 0.73f)
            lineTo(size.width * 0.50f, size.height * 0.96f)
            lineTo(size.width * 0.10f, size.height * 0.73f)
            lineTo(size.width * 0.10f, size.height * 0.27f)
            close()
        }
        drawPath(hex, color = accent.copy(alpha = 0.12f))
        drawPath(hex, color = accent.copy(alpha = 0.84f), style = Stroke(width = 1.5.dp.toPx()))
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accent.copy(alpha = 0.40f), Color.Transparent),
                radius = size.minDimension * 0.36f
            ),
            radius = size.minDimension * 0.36f,
            center = center
        )
        val star = Path().apply {
            val c = center
            val r1 = size.minDimension * 0.24f
            val r2 = size.minDimension * 0.10f
            repeat(10) { i ->
                val angle = Math.toRadians((i * 36f - 90f).toDouble())
                val r = if (i % 2 == 0) r1 else r2
                val x = c.x + kotlin.math.cos(angle).toFloat() * r
                val y = c.y + kotlin.math.sin(angle).toFloat() * r
                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
            close()
        }
        drawPath(star, color = accent.copy(alpha = 0.95f))
    }
}

@Composable
private fun TodayProgressBlock(data: StatusUiData) {
    val colors = SystemTheme.colors
    val flatTodos = remember(data.todos) { data.todos.flatMap { listOf(it) + it.microtasks } }
    val completedTodos = flatTodos.count { it.isCompleted }

    val shape = systemLargePanelShape()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(142.dp)
            .techSurface(
                shape = shape,
                active = false,
                accent = colors.accentPrimary,
                role = TechSurfaceRole.Panel
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, top = 22.dp, end = 24.dp, bottom = 18.dp)
        ) {
            Text(
                text = "Сьогоднішній прогрес",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = colors.accentPrimary,
                    fontFamily = SystemDisplayFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 2.7.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                ProgressMetric(
                    label = "Досвід сьогодні",
                    value = "0",
                    accent = colors.accentPrimary,
                    icon = { ProgressXpBadge() },
                    modifier = Modifier.weight(1f)
                )
                ProgressDivider()
                ProgressMetric(
                    label = "Серія",
                    value = data.currentStreak.toString(),
                    accent = colors.accentPrimary,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Whatshot,
                            contentDescription = null,
                            tint = colors.accentPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
                ProgressDivider()
                ProgressMetric(
                    label = "Квести",
                    value = "$completedTodos/${flatTodos.size.coerceAtLeast(2)}",
                    accent = colors.textSecondary,
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ProgressMetric(
    label: String,
    value: String,
    accent: Color,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    val colors = SystemTheme.colors
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Text(
                text = label.toSystemSentenceCase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    lineHeight = 11.sp,
                    letterSpacing = 0.5.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(
                color = accent,
                fontFamily = SystemDisplayFamily,
                fontWeight = FontWeight.Black,
                fontSize = 30.sp,
                lineHeight = 31.sp,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (subtitle != null) {
            Text(
                text = subtitle.toSystemSentenceCase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                    lineHeight = 10.sp,
                    letterSpacing = 0.4.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ProgressXpBadge() {
    val colors = SystemTheme.colors
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(SystemHexagonShape())
            .background(Color.Black.copy(alpha = 0.20f))
            .border(1.dp, colors.accentPrimary.copy(alpha = 0.78f), SystemHexagonShape()),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = colors.textPrimary,
            modifier = Modifier.size(14.dp)
        )
    }
}

@Composable
private fun ProgressDivider() {
    val colors = SystemTheme.colors
    Box(
        modifier = Modifier
            .padding(top = 5.dp)
            .width(1.dp)
            .height(56.dp)
            .background(colors.overlayStrong.copy(alpha = 0.55f))
    )
}

@Composable
private fun BossFightBlock(
    bossFight: BossFight,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    val accent = colors.accentWarning
    val isActive = bossFight.status == BossFightStatus.ACTIVE

    DarkGlassCard(modifier = modifier, contentPadding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SystemSectionHeader(
                title = "Контрольний норматив",
                subtitle = "Ранг ${bossFight.rankFrom.name} -> ${bossFight.rankTo.name}"
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SystemItemSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FocusIcon(
                    icon = Icons.Filled.FitnessCenter,
                    tint = accent
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        text = bossFight.title.removePrefix("Контрольний норматив: ").ifBlank { bossFight.title },
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = bossFight.rulesText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = colors.textSecondary,
                            fontWeight = FontWeight.Medium
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SystemStateBadge(
                    text = bossFight.targetLabel(),
                    accent = accent
                )
                SystemStateBadge(
                    text = "Нагорода: ${bossFight.rankTo.name}",
                    accent = colors.accentSuccess
                )
            }
            SystemButton(
                text = if (isActive) "Почати" else "Відкрити норматив",
                icon = Icons.Filled.FitnessCenter,
                onClick = onOpen,
                enabled = isActive,
                accent = accent,
                glow = isActive,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun BossFight.targetLabel(): String =
    when (targetMetric) {
        BossFightTargetMetric.WEIGHT -> "Ціль: ${targetValue.formatCompactTarget()} кг"
        BossFightTargetMetric.REPS -> "Ціль: ${targetValue.roundToInt()} повт."
        BossFightTargetMetric.TIME_SECONDS -> "Ціль: ${targetValue.roundToInt().formatTimeTarget()}"
        BossFightTargetMetric.DISTANCE_METERS -> "Ціль: ${targetValue.formatCompactTarget()} м"
    }

private fun Double.formatCompactTarget(): String =
    if (this % 1.0 == 0.0) {
        toInt().toString()
    } else {
        String.format(Locale.US, "%.1f", this)
    }

private fun Int.formatTimeTarget(): String =
    if (this >= 60 && this % 60 == 0) {
        "${this / 60} хв"
    } else {
        "$this сек"
    }

@Composable
private fun StatusHeader(
    weekPreview: List<StatusWeekDayUiModel>,
    onOpenCalendar: () -> Unit
) {
    val colors = SystemTheme.colors
    val today = LocalDate.now()
    val dateText = today.format(DateTimeFormatter.ofPattern("dd.MM.yy"))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(124.dp)
                .height(88.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = dateText,
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = colors.textPrimary.copy(alpha = 0.96f),
                    fontFamily = SystemDisplayFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 25.sp,
                    lineHeight = 26.sp,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1,
                overflow = TextOverflow.Clip,
                modifier = Modifier.fillMaxWidth()
            )
        }
        WeekPreviewBlock(
            days = weekPreview,
            onOpenCalendar = onOpenCalendar,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SystemLevelBlock(data: StatusUiData) {
    val colors = SystemTheme.colors
    val progress = remember(data.xpTotal, data.xpMax) {
        if (data.xpMax > 0) (data.xpTotal.toFloat() / data.xpMax).coerceIn(0f, 1f) else 0f
    }
    val xpRemaining = (data.xpMax - data.xpTotal).coerceAtLeast(0)
    val isRecoveryDay = data.mainQuest == null
    val dayStatusText = remember(data.weekPreview, data.mainQuest) {
        if (data.mainQuest == null) {
            "Відновлення"
        } else {
            data.weekPreview
                .firstOrNull { it.isToday }
                ?.status
                ?.toCompactDayStatus()
                ?: if (data.mainQuest.isCompleted) "Виконано" else "Активний"
        }
    }
    val priorityText = data.mainQuest?.title ?: "Без тренування"

    DarkGlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Система · Рівень ${data.level}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SystemStateBadge(
                        text = dayStatusText,
                        accent = if (isRecoveryDay) colors.accentSuccess else colors.accentPrimary
                    )
                    Text(
                        text = "${(progress * 100).roundToInt()}%",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = colors.accentPrimary,
                            fontWeight = FontWeight.Black
                        )
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${data.xpTotal} / ${data.xpMax} досвіду",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = colors.textSecondary,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "До рівня ${data.level + 1}: $xpRemaining досвіду",
                    style = MaterialTheme.typography.bodySmall.copy(color = colors.textMuted),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            SystemProgressBar(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompactStatusItem(
                    icon = Icons.Filled.Whatshot,
                    text = "${data.currentStreak} днів",
                    accent = colors.accentWarning,
                    modifier = Modifier.weight(1f)
                )
                CompactStatusItem(
                    icon = Icons.Filled.FitnessCenter,
                    text = priorityText,
                    accent = colors.accentPrimary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
internal fun SystemStateBadge(
    text: String,
    accent: Color
) {
    val colors = SystemTheme.colors
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(SystemTheme.shapes.pill))
            .background(accent.copy(alpha = 0.09f))
            .border(1.dp, accent.copy(alpha = 0.18f), RoundedCornerShape(SystemTheme.shapes.pill))
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(RoundedCornerShape(SystemTheme.shapes.pill))
                .background(accent.copy(alpha = 0.88f))
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                color = colors.textPrimary.copy(alpha = 0.86f),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                lineHeight = 11.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CompactStatusItem(
    icon: ImageVector,
    text: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(SystemTheme.shapes.small))
                .background(accent.copy(alpha = 0.09f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(14.dp)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                color = colors.textSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                lineHeight = 12.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun StatusWeekDayStatus.toCompactDayStatus(): String =
    when (this) {
        StatusWeekDayStatus.COMPLETED -> "Виконано"
        StatusWeekDayStatus.PARTIAL -> "Частково"
        StatusWeekDayStatus.MISSED -> "Пропущено"
        StatusWeekDayStatus.PLANNED -> "Активний"
        StatusWeekDayStatus.NO_DATA -> "Немає даних"
    }

@Composable
private fun MainFocusBlock(
    mainQuest: QuestUiModel,
    onStartWorkout: () -> Unit,
    onOpenWorkoutSettings: () -> Unit
) {
    val colors = SystemTheme.colors
    DarkGlassCard(modifier = Modifier.fillMaxWidth(), active = true, contentPadding = SystemCardPadding) {
        Column(verticalArrangement = Arrangement.spacedBy(SystemCardPadding)) {
            SystemSectionHeader(
                title = "Головний фокус",
                subtitle = "Поточна дія дня",
                trailing = {
                    IconButton(onClick = onOpenWorkoutSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = null, tint = colors.textSecondary)
                    }
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SystemItemSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FocusIcon(
                    icon = Icons.Filled.FitnessCenter,
                    tint = colors.accentPrimary
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = mainQuest.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Black
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = mainQuest.subtitle.asString(),
                        style = MaterialTheme.typography.bodyMedium.copy(color = colors.textSecondary),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (mainQuest.tasks.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    mainQuest.tasks.take(3).forEach { task ->
                        FocusTaskLine(task)
                    }
                }
            }

            SystemButton(
                text = if (mainQuest.isCompleted) "Тренування завершено" else mainQuest.protocolActionText(),
                icon = Icons.Filled.FitnessCenter,
                onClick = onStartWorkout,
                enabled = !mainQuest.isCompleted,
                glow = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun QuestUiModel.protocolActionText(): String =
    when (title.uppercase()) {
        "NO EXCUSE PROTOCOL" -> "Почати 7 хв"
        "RECOVERY PROTOCOL" -> "Почати відновлення"
        "DELOAD SESSION" -> "Почати легке тренування"
        else -> "Почати тренування"
    }

@Composable
private fun WeekPreviewBlock(
    days: List<StatusWeekDayUiModel>,
    onOpenCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    val cardShape = systemLargePanelShape()
    if (days.isEmpty()) {
        Box(
            modifier = modifier
                .height(96.dp)
                .techSurface(
                    shape = cardShape,
                    active = false,
                    accent = colors.accentPrimary,
                    role = TechSurfaceRole.Panel
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Готуємо календарний цикл.",
                style = MaterialTheme.typography.bodySmall.copy(color = colors.textMuted)
            )
        }
    } else {
        val models = days.map { it.toSystemWeekDayModel() }
        Column(
            modifier = modifier
                .fillMaxWidth()
                .height(96.dp)
                .techSurface(
                    shape = cardShape,
                    active = false,
                    accent = colors.accentPrimary,
                    role = TechSurfaceRole.Panel
                )
                .systemClickable(onClick = onOpenCalendar)
                .padding(start = 13.dp, top = 13.dp, end = 13.dp, bottom = 11.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                models.forEach { day ->
                    Text(
                        text = day.date.ukrainianWeekLabelReadable(),
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = when {
                                day.isToday -> colors.accentPrimary
                                day.date.dayOfWeek == java.time.DayOfWeek.SATURDAY -> colors.accentWarning
                                else -> colors.textMuted
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            lineHeight = 14.sp,
                            textAlign = TextAlign.Center
                        ),
                        maxLines = 1
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val nodeCount = models.size.coerceAtLeast(1)
                    val nodeY = size.height * 0.50f
                    val nodeRadius = 20.dp.toPx()
                    fun nodeX(index: Int): Float =
                        size.width * ((index + 0.5f) / nodeCount.toFloat())

                    if (nodeCount > 1) {
                        repeat(nodeCount - 1) { index ->
                            val startX = nodeX(index) + nodeRadius
                            val endX = nodeX(index + 1) - nodeRadius
                            drawLine(
                                color = colors.borderSubtle.copy(alpha = 0.42f),
                                start = Offset(startX, nodeY),
                                end = Offset(endX, nodeY),
                                strokeWidth = 1.2.dp.toPx(),
                                cap = StrokeCap.Square
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    models.forEach { day ->
                        val dayAccent = if (day.isToday) colors.accentPrimary else colors.statusNeutral
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            val dayShape = RoundedCornerShape(12.dp)
                            Box(
                                modifier = Modifier
                                    .size(if (day.isToday) 42.dp else 40.dp)
                                    .techSurface(
                                        shape = dayShape,
                                        active = day.isToday,
                                        accent = dayAccent,
                                        role = TechSurfaceRole.Plate
                                    )
                                    .border(
                                        width = if (day.isToday) 1.4.dp else 0.7.dp,
                                        color = dayAccent.copy(alpha = if (day.isToday) 0.90f else 0.28f),
                                        shape = dayShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = day.dayNumber,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        color = colors.textPrimary,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 19.sp,
                                        lineHeight = 21.sp,
                                        textAlign = TextAlign.Center
                                    ),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionsSwipeHint() {
    val colors = SystemTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 3.dp, bottom = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Canvas(modifier = Modifier.size(width = 15.dp, height = 24.dp)) {
            val stroke = 2.1.dp.toPx()
            drawLine(
                color = colors.textMuted.copy(alpha = 0.72f),
                start = Offset(size.width * 0.74f, size.height * 0.18f),
                end = Offset(size.width * 0.28f, size.height * 0.50f),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
            drawLine(
                color = colors.textMuted.copy(alpha = 0.72f),
                start = Offset(size.width * 0.28f, size.height * 0.50f),
                end = Offset(size.width * 0.74f, size.height * 0.82f),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
        Text(
            text = "Проведіть вліво — показати прогрес",
            style = MaterialTheme.typography.bodySmall.copy(
                color = colors.textMuted.copy(alpha = 0.88f),
                fontSize = 14.sp,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StatusSwipeHint() {
    val colors = SystemTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Canvas(modifier = Modifier.size(width = 15.dp, height = 24.dp)) {
            val stroke = 2.1.dp.toPx()
            drawLine(
                color = colors.accentPrimary.copy(alpha = 0.88f),
                start = Offset(size.width * 0.26f, size.height * 0.18f),
                end = Offset(size.width * 0.72f, size.height * 0.50f),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
            drawLine(
                color = colors.accentPrimary.copy(alpha = 0.88f),
                start = Offset(size.width * 0.72f, size.height * 0.50f),
                end = Offset(size.width * 0.26f, size.height * 0.82f),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
        Text(
            text = "Проведіть вправо — повернутись до завдань",
            style = MaterialTheme.typography.bodySmall.copy(
                color = colors.textMuted.copy(alpha = 0.88f),
                fontSize = 14.sp,
                lineHeight = 16.sp,
                textAlign = TextAlign.Center
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun LocalDate.ukrainianWeekLabelReadable(): String =
    when (dayOfWeek) {
        java.time.DayOfWeek.MONDAY -> "Пн"
        java.time.DayOfWeek.TUESDAY -> "Вт"
        java.time.DayOfWeek.WEDNESDAY -> "Ср"
        java.time.DayOfWeek.THURSDAY -> "Чт"
        java.time.DayOfWeek.FRIDAY -> "Пт"
        java.time.DayOfWeek.SATURDAY -> "Сб"
        java.time.DayOfWeek.SUNDAY -> "Нд"
    }

@Composable
internal fun FocusIcon(icon: ImageVector, tint: Color) {
    Box(
        modifier = Modifier
            .height(52.dp)
            .width(52.dp)
            .background(
                Brush.radialGradient(listOf(tint.copy(alpha = 0.22f), Color.Transparent))
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint)
    }
}

@Composable
private fun FocusTaskLine(task: TaskUiModel) {
    val colors = SystemTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .height(5.dp)
                .width(5.dp)
                .background(if (task.isCompleted) colors.accentSuccess else colors.accentPrimarySoft)
        )
        Text(
            text = task.nameUk ?: task.name,
            style = MaterialTheme.typography.bodySmall.copy(
                color = if (task.isCompleted) colors.textSecondary.copy(alpha = 0.58f) else colors.textSecondary,
                fontWeight = FontWeight.SemiBold
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun StatusWeekDayUiModel.toSystemWeekDayModel(): SystemWeekDayModel =
    SystemWeekDayModel(
        date = date,
        label = weekDayLabel,
        dayNumber = dayNumber,
        visualType = when (visualType) {
            StatusWeekDayVisualType.WORK -> SystemWeekDayVisualType.Work
            StatusWeekDayVisualType.TRAINING -> SystemWeekDayVisualType.Training
            StatusWeekDayVisualType.MIXED -> SystemWeekDayVisualType.Mixed
            StatusWeekDayVisualType.REST -> SystemWeekDayVisualType.Rest
        },
        status = when (status) {
            StatusWeekDayStatus.COMPLETED -> SystemWeekDayStatus.Completed
            StatusWeekDayStatus.PARTIAL -> SystemWeekDayStatus.Partial
            StatusWeekDayStatus.MISSED -> SystemWeekDayStatus.Missed
            StatusWeekDayStatus.PLANNED -> SystemWeekDayStatus.Planned
            StatusWeekDayStatus.NO_DATA -> SystemWeekDayStatus.NoData
        },
        isToday = isToday
    )
