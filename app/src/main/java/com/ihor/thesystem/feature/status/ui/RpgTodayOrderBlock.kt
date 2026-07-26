package com.ihor.thesystem.feature.status.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.SystemDisplayFamily
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.SystemUiTestTags
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemButtonStyle
import com.ihor.thesystem.core.ui.components.SystemHexagonShape
import com.ihor.thesystem.core.ui.components.TechSurfaceRole
import com.ihor.thesystem.core.ui.components.systemEnterMotion
import com.ihor.thesystem.core.ui.components.techSurface
import com.ihor.thesystem.core.ui.toSystemSentenceCase
import com.ihor.thesystem.feature.status.viewmodel.TodayOrderAccent
import com.ihor.thesystem.feature.status.viewmodel.TodayOrderUiModel
import kotlin.math.roundToInt

@Composable
internal fun TodayOrderBlock(
    order: TodayOrderUiModel,
    onStartWorkout: () -> Unit
) {
    val colors = SystemTheme.colors
    val accent = order.accent.toColor()
    val actionAccent = if (order.accent == TodayOrderAccent.AI) colors.accentPrimary else accent
    val heroShape = RoundedCornerShape(
        topStart = SystemTheme.shapes.extraLarge,
        topEnd = SystemTheme.shapes.large,
        bottomEnd = SystemTheme.shapes.extraLarge,
        bottomStart = SystemTheme.shapes.large
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 356.dp)
            .testTag(SystemUiTestTags.TODAY_ORDER)
            .techSurface(
                shape = heroShape,
                active = false,
                accent = actionAccent,
                role = TechSurfaceRole.Content
            )
            .systemEnterMotion(
                initialScale = 0.99f,
                initialOffset = 8.dp
            )
    ) {
        val compact = maxWidth < 360.dp || LocalDensity.current.fontScale >= 1.2f
        val horizontalPadding = if (compact) 16.dp else 20.dp

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = horizontalPadding,
                    top = if (compact) 19.dp else 20.dp,
                    end = horizontalPadding,
                    bottom = 18.dp
                )
        ) {
            TodayOrderHeader(
                order = order,
                accent = actionAccent,
                complementaryAccent = if (order.accent == TodayOrderAccent.AI) {
                    colors.accentAi
                } else {
                    null
                },
                compact = compact
            )

            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.borderSubtle.copy(alpha = 0.26f))
            )
            Spacer(modifier = Modifier.height(14.dp))

            TodayOrderFacts(
                order = order,
                accent = accent,
                compact = compact
            )

            Spacer(modifier = Modifier.height(16.dp))
            SystemButton(
                text = order.primaryActionLabel.toSystemSentenceCase(),
                onClick = onStartWorkout,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(SystemUiTestTags.TODAY_ORDER_CTA),
                accent = actionAccent,
                enabled = order.actionEnabled,
                style = SystemButtonStyle.Filled,
                trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight
            )
        }
    }
}

@Composable
private fun TodayOrderHeader(
    order: TodayOrderUiModel,
    accent: Color,
    complementaryAccent: Color?,
    compact: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TodayOrderCopy(
            order = order,
            accent = accent,
            compact = compact,
            modifier = Modifier.weight(1f)
        )
        QuestReadinessRing(
            progress = order.readinessProgress,
            accent = accent,
            complementaryAccent = complementaryAccent,
            compact = compact,
            modifier = Modifier.size(if (compact) 100.dp else 132.dp)
        )
    }
}

@Composable
private fun TodayOrderCopy(
    order: TodayOrderUiModel,
    accent: Color,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    Column(modifier = modifier) {
        Text(
            text = "Сьогодні · ${order.dayTypeLabel.toSystemSentenceCase()}",
            style = MaterialTheme.typography.labelLarge.copy(
                color = accent,
                fontFamily = SystemDisplayFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = if (compact) 12.sp else 13.sp,
                lineHeight = if (compact) 15.sp else 16.sp,
                letterSpacing = 0.2.sp
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(9.dp))
        Text(
            text = order.title.toSystemSentenceCase(),
            style = MaterialTheme.typography.headlineMedium.copy(
                color = colors.textPrimary,
                fontFamily = SystemDisplayFamily,
                fontWeight = FontWeight.Bold,
                fontSize = if (compact) 26.sp else 32.sp,
                lineHeight = if (compact) 29.sp else 35.sp,
                letterSpacing = (-0.3).sp
            ),
            maxLines = if (compact) 3 else 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = order.reason,
            style = MaterialTheme.typography.bodyLarge.copy(
                color = colors.textSecondary,
                fontWeight = FontWeight.Normal,
                fontSize = if (compact) 13.sp else 14.sp,
                lineHeight = if (compact) 18.sp else 20.sp
            ),
            maxLines = if (compact) 6 else 5,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TodayOrderFacts(
    order: TodayOrderUiModel,
    accent: Color,
    compact: Boolean
) {
    val colors = SystemTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (compact) 58.dp else 64.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        QuestMetric(
            value = order.durationText,
            label = order.durationLabel,
            accent = colors.textSecondary,
            compact = compact,
            modifier = Modifier.weight(1f)
        ) {
            QuestClockBadge(accent = colors.textSecondary)
        }
        Box(
            modifier = Modifier
                .padding(horizontal = if (compact) 8.dp else 13.dp)
                .width(1.dp)
                .height(if (compact) 44.dp else 50.dp)
                .background(colors.borderSubtle.copy(alpha = 0.28f))
        )
        QuestMetric(
            value = order.outcomeText,
            label = order.outcomeLabel,
            accent = accent,
            compact = compact,
            modifier = Modifier.weight(1f)
        ) {
            QuestXpBadge(accent = accent)
        }
    }
}

@Composable
private fun TodayOrderAccent.toColor(): Color {
    val colors = SystemTheme.colors
    return when (this) {
        TodayOrderAccent.PRIMARY -> colors.accentPrimary
        TodayOrderAccent.SUCCESS -> colors.accentSuccess
        TodayOrderAccent.WARNING -> colors.accentWarning
        TodayOrderAccent.ERROR -> colors.accentError
        TodayOrderAccent.AI -> colors.accentAi
    }
}

@Composable
private fun QuestMetric(
    value: String,
    label: String,
    accent: Color,
    compact: Boolean,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit
) {
    val colors = SystemTheme.colors
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(if (compact) 9.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 32.dp else 38.dp)
                .clip(SystemHexagonShape())
                .background(colors.background.copy(alpha = 0.38f))
                .border(
                    width = 1.dp,
                    color = colors.borderSubtle.copy(alpha = 0.82f),
                    shape = SystemHexagonShape()
                ),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = if (compact) 13.sp else 15.sp,
                    lineHeight = if (compact) 16.sp else 18.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label.toSystemSentenceCase(),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Normal,
                    fontSize = if (compact) 10.sp else 11.sp,
                    lineHeight = if (compact) 12.sp else 14.sp,
                    letterSpacing = 0.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun QuestClockBadge(accent: Color) {
    Canvas(modifier = Modifier.size(19.dp)) {
        val stroke = 1.6.dp.toPx()
        drawCircle(
            color = accent.copy(alpha = 0.12f),
            radius = size.minDimension * 0.43f,
            center = center
        )
        drawCircle(
            color = accent.copy(alpha = 0.90f),
            radius = size.minDimension * 0.43f,
            center = center,
            style = Stroke(width = stroke)
        )
        drawLine(
            color = accent.copy(alpha = 0.95f),
            start = center,
            end = Offset(size.width * 0.50f, size.height * 0.27f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = accent.copy(alpha = 0.95f),
            start = center,
            end = Offset(size.width * 0.69f, size.height * 0.55f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun QuestXpBadge(accent: Color) {
    Icon(
        imageVector = Icons.Filled.Star,
        contentDescription = null,
        tint = accent,
        modifier = Modifier.size(18.dp)
    )
}

@Composable
private fun QuestReadinessRing(
    progress: Float,
    accent: Color,
    complementaryAccent: Color?,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    val motion = SystemTheme.motion
    val clamped = progress.coerceIn(0f, 1f)
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(clamped) {
        animatedProgress.animateTo(
            targetValue = clamped,
            animationSpec = tween(
                durationMillis = motion.progressMillis,
                easing = EaseOutCubic
            )
        )
    }

    Box(
        modifier = modifier.semantics {
            progressBarRangeInfo = ProgressBarRangeInfo(
                current = clamped,
                range = 0f..1f
            )
        },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = if (compact) 8.dp.toPx() else 10.dp.toPx()
            val inset = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val drawnProgress = animatedProgress.value.coerceIn(0f, 1f)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accent.copy(alpha = 0.055f),
                        colors.background.copy(alpha = 0.62f)
                    ),
                    center = Offset(size.width * 0.38f, size.height * 0.30f),
                    radius = size.minDimension * 0.48f
                ),
                radius = size.minDimension * 0.36f,
                center = center
            )
            drawArc(
                color = colors.borderSubtle.copy(alpha = 0.55f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            if (drawnProgress > 0f) {
                val progressBrush = if (complementaryAccent != null) {
                    Brush.sweepGradient(
                        colors = listOf(
                            colors.accentPrimary,
                            colors.accentPrimarySoft,
                            colors.accentPrimary
                        )
                    )
                } else {
                    Brush.sweepGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.76f),
                            accent,
                            accent.copy(alpha = 0.76f)
                        )
                    )
                }
                drawArc(
                    brush = progressBrush,
                    startAngle = -90f,
                    sweepAngle = 360f * drawnProgress,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
                if (complementaryAccent != null) {
                    val progressEndAngle = -90f + (360f * drawnProgress)
                    val complementaryStartAngle = 102f
                    val complementarySweep =
                        (progressEndAngle - complementaryStartAngle).coerceIn(0f, 58f)
                    if (complementarySweep > 0f) {
                        drawArc(
                            brush = Brush.linearGradient(
                                colors = listOf(colors.accentPrimary, complementaryAccent),
                                start = Offset(size.width * 0.52f, size.height),
                                end = Offset(0f, size.height * 0.50f)
                            ),
                            startAngle = complementaryStartAngle,
                            sweepAngle = complementarySweep,
                            useCenter = false,
                            topLeft = Offset(inset, inset),
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Round)
                        )
                    }
                }
            }
            drawCircle(
                color = colors.borderSubtle.copy(alpha = 0.54f),
                radius = size.minDimension * 0.36f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(animatedProgress.value * 100f).roundToInt()}%",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = colors.textPrimary,
                    fontFamily = SystemDisplayFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = if (compact) 28.sp else 39.sp,
                    lineHeight = if (compact) 31.sp else 42.sp,
                    letterSpacing = (-0.6).sp,
                    textAlign = TextAlign.Center
                )
            )
            Text(
                text = "Готовність",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Normal,
                    fontSize = if (compact) 9.sp else 11.sp,
                    lineHeight = if (compact) 11.sp else 14.sp,
                    letterSpacing = 0.sp,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
