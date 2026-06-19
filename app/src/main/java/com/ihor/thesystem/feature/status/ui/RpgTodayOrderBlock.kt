package com.ihor.thesystem.feature.status.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.components.SystemHexagonShape
import com.ihor.thesystem.core.ui.components.SystemPanel
import com.ihor.thesystem.domain.model.TodayTrainingDecision
import com.ihor.thesystem.domain.model.TodayTrainingDecisionType
import com.ihor.thesystem.feature.status.viewmodel.QuestUiModel

@Composable
internal fun TodayOrderBlock(
    decision: TodayTrainingDecision?,
    fallbackMainQuest: QuestUiModel?,
    onStartWorkout: () -> Unit
) {
    val colors = SystemTheme.colors
    val decisionType = decision?.decisionType
    val accent = colors.accentPrimary
    val readinessProgress = ((decision?.readinessScore ?: 0) / 100f).coerceIn(0f, 1f)
    val actionEnabled = decisionType != null && decisionType != TodayTrainingDecisionType.REST
    val actionText = when (decisionType) {
        TodayTrainingDecisionType.NO_EXCUSE -> "Почати 7 хв"
        TodayTrainingDecisionType.ACTIVE_RECOVERY -> "Почати відновлення"
        TodayTrainingDecisionType.DELOAD -> "Почати deload"
        TodayTrainingDecisionType.REST -> "День без тренування"
        null -> "План формується"
        else -> "Почати тренування"
    }
    val title = decision.todayTitle(fallbackMainQuest).uppercase()
    val subtitle = decision.shortDecisionLabel()
    val durationText = fallbackMainQuest?.estimatedDurationMinutes?.let { "$it хв" }
    val rewardText = fallbackMainQuest?.rewardXp?.let { "+$it XP" }

    SystemPanel(
        modifier = Modifier
            .fillMaxWidth()
            .height(238.dp),
        active = true,
        accent = accent,
        contentPadding = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 18.dp, top = 17.dp, end = 18.dp, bottom = 15.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(138.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        text = "Головний квест".uppercase(),
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = accent,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            lineHeight = 16.sp,
                            letterSpacing = 2.2.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(7.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = colors.textPrimary,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Black,
                            fontSize = 30.sp,
                            lineHeight = 31.sp,
                            letterSpacing = 0.6.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = colors.textSecondary,
                            fontWeight = FontWeight.Medium,
                            fontSize = 17.sp,
                            lineHeight = 20.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (durationText != null && rewardText != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            QuestMetric(
                                value = durationText,
                                label = "Тривалість",
                                accent = colors.textSecondary,
                                modifier = Modifier.widthIn(min = 78.dp)
                            ) {
                                QuestClockBadge(accent = colors.textSecondary)
                            }
                            QuestMetric(
                                value = rewardText,
                                label = "Нагорода",
                                accent = accent,
                                modifier = Modifier.widthIn(min = 82.dp)
                            ) {
                                QuestXpBadge(accent = accent)
                            }
                        }
                    }
                }

                QuestReadinessRing(
                    progress = readinessProgress,
                    accent = accent,
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .size(108.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            QuestPrimaryButton(
                text = actionText,
                enabled = actionEnabled,
                accent = accent,
                onClick = onStartWorkout,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun QuestMetric(
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit
) {
    val colors = SystemTheme.colors
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(29.dp)
                .clip(SystemHexagonShape())
                .background(Color.Black.copy(alpha = 0.24f))
                .border(1.dp, colors.overlayStrong.copy(alpha = 0.70f), SystemHexagonShape()),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    lineHeight = 16.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 9.sp,
                    lineHeight = 10.sp,
                    letterSpacing = 0.6.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun QuestClockBadge(accent: Color) {
    Canvas(modifier = Modifier.size(18.dp)) {
        val stroke = 1.6.dp.toPx()
        drawCircle(
            color = accent.copy(alpha = 0.22f),
            radius = size.minDimension * 0.42f,
            center = center
        )
        drawCircle(
            color = accent.copy(alpha = 0.88f),
            radius = size.minDimension * 0.42f,
            center = center,
            style = Stroke(width = stroke)
        )
        drawLine(
            color = accent.copy(alpha = 0.95f),
            start = center,
            end = Offset(size.width * 0.50f, size.height * 0.25f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = accent.copy(alpha = 0.95f),
            start = center,
            end = Offset(size.width * 0.68f, size.height * 0.54f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun QuestXpBadge(accent: Color) {
    Text(
        text = "XP",
        style = MaterialTheme.typography.labelSmall.copy(
            color = accent.copy(alpha = 0.96f),
            fontWeight = FontWeight.Black,
            fontSize = 9.sp,
            lineHeight = 9.sp
        )
    )
}

@Composable
private fun QuestReadinessRing(
    progress: Float,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    val clamped = progress.coerceIn(0f, 1f)
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 9.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = 0.14f), Color.Transparent),
                    radius = size.minDimension * 0.55f
                ),
                radius = size.minDimension * 0.48f,
                center = center
            )
            drawArc(
                color = colors.overlayMedium.copy(alpha = 0.85f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Butt)
            )
            repeat(28) { index ->
                val segmentProgress = (index + 1) / 28f
                val isActive = segmentProgress <= clamped
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            accent.copy(alpha = if (isActive) 0.78f else 0.10f),
                            Color(0xFF008CFF).copy(alpha = if (isActive) 0.96f else 0.10f),
                            accent.copy(alpha = if (isActive) 0.78f else 0.10f)
                        )
                    ),
                    startAngle = -90f + index * (360f / 28f),
                    sweepAngle = 8.2f,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Butt)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(clamped * 100).toInt()}%",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = colors.textPrimary,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    fontSize = 30.sp,
                    lineHeight = 31.sp,
                    textAlign = TextAlign.Center
                )
            )
            Text(
                text = "Готовність".uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    lineHeight = 10.sp,
                    letterSpacing = 0.7.sp,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun QuestPrimaryButton(
    text: String,
    enabled: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    val shape = QuestButtonShape()
    Box(
        modifier = modifier
            .height(54.dp)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = if (enabled) {
                        listOf(
                            accent.copy(alpha = 0.78f),
                            Color(0xFF087DBD).copy(alpha = 0.78f),
                            colors.backgroundElevated.copy(alpha = 0.92f)
                        )
                    } else {
                        listOf(colors.overlayMedium, colors.surfaceGlassStrong)
                    },
                    start = Offset.Zero,
                    end = Offset.Infinite
                )
            )
            .border(1.dp, if (enabled) accent.copy(alpha = 0.86f) else colors.borderMuted, shape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = if (enabled) 0.34f else 0.07f), Color.Transparent),
                    center = Offset(size.width * 0.25f, size.height * 0.10f),
                    radius = size.width * 0.55f
                )
            )
        }
        Text(
            text = text.uppercase(),
            modifier = Modifier.padding(horizontal = 48.dp),
            style = MaterialTheme.typography.titleMedium.copy(
                color = if (enabled) colors.textPrimary else colors.textMuted,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                lineHeight = 20.sp,
                letterSpacing = 1.4.sp,
                textAlign = TextAlign.Center
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = if (enabled) colors.textPrimary else colors.textMuted,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 14.dp)
                .size(34.dp)
        )
    }
}

private class QuestButtonShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cut = size.height * 0.30f
        val notch = size.height * 0.18f
        val path = Path().apply {
            moveTo(cut, 0f)
            lineTo(size.width - cut, 0f)
            lineTo(size.width, size.height / 2f)
            lineTo(size.width - cut, size.height)
            lineTo(cut, size.height)
            lineTo(0f, size.height / 2f)
            lineTo(notch, size.height * 0.25f)
            close()
        }
        return Outline.Generic(path)
    }
}

private fun TodayTrainingDecision?.todayTitle(fallbackMainQuest: QuestUiModel?): String =
    when (this?.decisionType) {
        TodayTrainingDecisionType.NO_EXCUSE -> "No Excuse Protocol"
        TodayTrainingDecisionType.ACTIVE_RECOVERY -> "Recovery Protocol"
        TodayTrainingDecisionType.DELOAD -> "Deload Session"
        TodayTrainingDecisionType.REST -> "День без тренування"
        null -> fallbackMainQuest?.title ?: "Recovery Protocol"
        else -> workoutName ?: fallbackMainQuest?.title ?: "Recovery Protocol"
    }

private fun TodayTrainingDecision?.shortDecisionLabel(): String =
    when (this?.decisionType) {
        TodayTrainingDecisionType.PROGRESS_ALLOWED -> "Прогрес дозволено"
        TodayTrainingDecisionType.STANDARD_TRAINING -> "Планове тренування"
        TodayTrainingDecisionType.REDUCED_LOAD -> "Зменшити навантаження"
        TodayTrainingDecisionType.ACTIVE_RECOVERY -> "Відновлення"
        TodayTrainingDecisionType.NO_EXCUSE -> "План перераховано"
        TodayTrainingDecisionType.DELOAD -> "Делоад"
        TodayTrainingDecisionType.REST -> "Відпочинок"
        null -> "План синхронізується"
    }

internal fun TodayTrainingDecision?.displayReason(): String =
    when (this?.decisionType) {
        TodayTrainingDecisionType.PROGRESS_ALLOWED ->
            "Готовність висока, борг відновлення низький."
        TodayTrainingDecisionType.STANDARD_TRAINING ->
            "План на сьогодні підходить під поточний стан."
        TodayTrainingDecisionType.REDUCED_LOAD ->
            "Сьогодні краще знизити вагу та обсяг."
        TodayTrainingDecisionType.ACTIVE_RECOVERY ->
            "Організму потрібне відновлення замість силового навантаження."
        TodayTrainingDecisionType.NO_EXCUSE ->
            if (reason.contains("missed", ignoreCase = true)) {
                "Система зафіксувала пропуск. План перераховано. Наступна оптимальна дія: коротке тренування."
            } else {
                "Готовність нижча за планову. Наступна оптимальна дія: коротке тренування."
            }
        TodayTrainingDecisionType.DELOAD ->
            "Навантаження накопичилось, сьогодні працюємо легше."
        TodayTrainingDecisionType.REST ->
            "Сьогодні день без тренування."
        null ->
            "Система готує сьогоднішній план."
    }
