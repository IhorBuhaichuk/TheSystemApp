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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalDensity
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
import com.ihor.thesystem.core.ui.components.DarkGlassCard
import com.ihor.thesystem.core.ui.components.SystemAvatarBadge
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemHexIcon
import com.ihor.thesystem.core.ui.components.SystemHexagonShape
import com.ihor.thesystem.core.ui.components.SystemPanel
import com.ihor.thesystem.core.ui.components.SystemProgressBar
import com.ihor.thesystem.core.ui.components.SystemSectionHeader
import com.ihor.thesystem.core.ui.components.SystemWeekCalendarPreview
import com.ihor.thesystem.core.ui.components.SystemWeekDayModel
import com.ihor.thesystem.core.ui.components.SystemWeekDayStatus
import com.ihor.thesystem.core.ui.components.SystemWeekDayVisualType
import com.ihor.thesystem.domain.model.BossFight
import com.ihor.thesystem.domain.model.BossFightStatus
import com.ihor.thesystem.domain.model.BossFightTargetMetric
import com.ihor.thesystem.domain.model.TodayTrainingDecision
import com.ihor.thesystem.feature.status.viewmodel.QuestUiModel
import com.ihor.thesystem.feature.status.viewmodel.StatusUiData
import com.ihor.thesystem.feature.status.viewmodel.StatusWeekDayStatus
import com.ihor.thesystem.feature.status.viewmodel.StatusWeekDayUiModel
import com.ihor.thesystem.feature.status.viewmodel.StatusWeekDayVisualType
import com.ihor.thesystem.feature.status.viewmodel.TaskUiModel
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
    val pinnedWeekHeight = 92.dp
    val transitionOffsetPx = with(LocalDensity.current) { 24.dp.roundToPx() }
    var dashboardState by rememberSaveable { mutableStateOf(StatusDashboardState.ACTIONS) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        AnimatedContent(
            targetState = dashboardState,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = pinnedWeekHeight + 16.dp, bottom = 24.dp)
                .statusDashboardSwipe(
                    state = dashboardState,
                    onStateChange = { dashboardState = it }
                ),
            transitionSpec = {
                val enteringStatus = targetState == StatusDashboardState.STATUS
                (
                    fadeIn(animationSpec = tween(durationMillis = 160)) +
                        slideInVertically(
                            animationSpec = tween(durationMillis = 280),
                            initialOffsetY = { if (enteringStatus) transitionOffsetPx else -transitionOffsetPx }
                        )
                    ).togetherWith(
                    fadeOut(animationSpec = tween(durationMillis = 140)) +
                        slideOutVertically(
                            animationSpec = tween(durationMillis = 280),
                            targetOffsetY = { if (enteringStatus) -transitionOffsetPx else transitionOffsetPx }
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
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF010204),
                            colors.background.copy(alpha = 0.98f),
                            Color(0xFF010204)
                        )
                    )
                )
                .padding(horizontal = SystemScreenPadding)
                .padding(top = 8.dp, bottom = 10.dp),
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
            .padding(horizontal = SystemScreenPadding),
        verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)
    ) {
        TodayOrderBlock(
            decision = data.todayDecision,
            fallbackMainQuest = data.mainQuest,
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
            .padding(horizontal = SystemScreenPadding),
        verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)
    ) {
        StatusHeroPanel(
            data = data,
            onAvatarSelected = onAvatarSelected,
            onEditNameTap = onEditNameTap,
            onOpenCalendar = onOpenCalendar
        )
        RecommendationPanel(decision = data.todayDecision)
        TodayProgressBlock(data = data)
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
            val threshold = 108.dp.toPx()
            val maxGestureMillis = 650L

            while (true) {
                val event = awaitPointerEvent()
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
    if (abs(totalY) < threshold || abs(totalY) < abs(totalX) * 1.35f) {
        return null
    }
    return when {
        this == StatusDashboardState.ACTIONS && totalY < 0f -> StatusDashboardState.STATUS
        this == StatusDashboardState.STATUS && totalY > 0f -> StatusDashboardState.ACTIONS
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

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(176.dp)
    ) {
        HeroHeaderBackdrop(
            modifier = Modifier.matchParentSize(),
            progress = progress
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 18.dp, top = 17.dp)
                .size(112.dp)
        ) {
            SystemAvatarBadge(
                avatarUri = data.avatarUri,
                modifier = Modifier.fillMaxSize(),
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 150.dp, top = 35.dp, end = 90.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = data.playerName.uppercase(),
                modifier = Modifier.clickable(onClick = onEditNameTap),
                style = MaterialTheme.typography.displayLarge.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 39.sp,
                    lineHeight = 39.sp
                )
            )
        }

        HeroXpPanel(
            xpText = "${data.xpTotal} / ${data.xpMax} XP",
            percentText = "${(progress * 100).roundToInt()}%",
            progress = progress,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 132.dp, top = 108.dp, end = 14.dp)
                .fillMaxWidth()
        )

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 11.dp, top = 19.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            HeroRankPlate(label = "Ранг", value = data.globalRank.name, onClick = onOpenCalendar)
            HeroRankPlate(label = "Рівень", value = data.level.toString(), onClick = onOpenCalendar)
        }
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
    Box(
        modifier = Modifier
            .width(72.dp)
            .height(45.dp)
            .clip(HeroSlantShape())
            .background(Color(0x66101620))
            .border(1.dp, Color.White.copy(alpha = 0.13f), HeroSlantShape())
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy((-2).dp)) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = colors.textSecondary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    lineHeight = 12.sp
                ),
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 25.sp,
                    lineHeight = 27.sp
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
    Box(modifier = modifier.height(50.dp)) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val cut = 12.dp.toPx()
            val path = Path().apply {
                moveTo(cut, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                lineTo(size.width * 0.08f, 0f)
                close()
            }
            drawPath(
                path = path,
                color = Color(0xAA070B10)
            )
            drawPath(
                path = path,
                color = colors.accentPrimary.copy(alpha = 0.28f),
                style = Stroke(width = 1.dp.toPx())
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 35.dp, top = 8.dp, end = 7.dp),
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
    decision: TodayTrainingDecision?,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    SystemPanel(
        modifier = modifier
            .fillMaxWidth()
            .height(86.dp),
        accent = colors.accentPrimary,
        contentPadding = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 12.dp, top = 10.dp, end = 16.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SystemHexIcon(
                icon = Icons.Filled.Star,
                accent = colors.accentPrimary,
                modifier = Modifier.size(58.dp)
            )
            Text(
                text = "Рекомендація: ${decision.displayReason()}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    lineHeight = 19.sp
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TodayProgressBlock(data: StatusUiData) {
    val colors = SystemTheme.colors
    val flatTodos = remember(data.todos) { data.todos.flatMap { listOf(it) + it.microtasks } }
    val completedTodos = flatTodos.count { it.isCompleted }

    SystemPanel(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp),
        contentPadding = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 15.dp, top = 13.dp, end = 15.dp, bottom = 12.dp)
        ) {
            Text(
                text = "Сьогоднішній прогрес".uppercase(),
                style = MaterialTheme.typography.labelLarge.copy(
                    color = colors.accentPrimary,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    lineHeight = 17.sp,
                    letterSpacing = 2.3.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(13.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                ProgressMetric(
                    label = "XP сьогодні",
                    value = data.xpThisWeek.toString(),
                    accent = colors.accentPrimary,
                    icon = { ProgressXpBadge() },
                    modifier = Modifier.weight(1f)
                )
                ProgressDivider()
                ProgressMetric(
                    label = "Серія",
                    value = data.currentStreak.toString(),
                    subtitle = "днів",
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
                    value = "$completedTodos/${flatTodos.size.coerceAtLeast(1)}",
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
                text = label.uppercase(),
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
                fontFamily = FontFamily.SansSerif,
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
                text = subtitle.uppercase(),
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
        Text(
            text = "XP",
            style = MaterialTheme.typography.labelSmall.copy(
                color = colors.textPrimary,
                fontWeight = FontWeight.Black,
                fontSize = 9.sp,
                lineHeight = 9.sp,
                textAlign = TextAlign.Center
            )
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
                    fontFamily = FontFamily.SansSerif,
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
                    text = "${data.xpTotal} / ${data.xpMax} XP",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = colors.textSecondary,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "До рівня ${data.level + 1}: $xpRemaining XP",
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
        "DELOAD SESSION" -> "Почати deload"
        else -> "Почати тренування"
    }

@Composable
private fun WeekPreviewBlock(
    days: List<StatusWeekDayUiModel>,
    onOpenCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    if (days.isEmpty()) {
        Box(modifier = modifier.height(58.dp), contentAlignment = Alignment.Center) {
            Text(
                text = "Календарний цикл синхронізується.",
                style = MaterialTheme.typography.bodySmall.copy(color = colors.textMuted)
            )
        }
    } else {
        SystemWeekCalendarPreview(
            days = days.map { it.toSystemWeekDayModel() },
            onOpenCalendar = onOpenCalendar,
            modifier = modifier
        )
    }
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
