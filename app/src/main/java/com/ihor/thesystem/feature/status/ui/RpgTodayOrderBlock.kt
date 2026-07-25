package com.ihor.thesystem.feature.status.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.SystemUiTestTags
import com.ihor.thesystem.core.ui.components.SystemHexagonShape
import com.ihor.thesystem.core.ui.components.TechSurfaceRole
import com.ihor.thesystem.core.ui.components.systemLargePanelShape
import com.ihor.thesystem.core.ui.components.techSurface
import com.ihor.thesystem.feature.status.viewmodel.TodayOrderAccent
import com.ihor.thesystem.feature.status.viewmodel.TodayOrderUiModel

@Composable
internal fun TodayOrderBlock(
    order: TodayOrderUiModel,
    onStartWorkout: () -> Unit
) {
    val colors = SystemTheme.colors
    val accent = order.accent.toColor()
    val title = order.title.uppercase()
    val cardShape = systemLargePanelShape()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 334.dp)
            .testTag(SystemUiTestTags.TODAY_ORDER)
            .techSurface(
                shape = cardShape,
                active = true,
                accent = accent,
                role = TechSurfaceRole.Panel
            )
    ) {
        val compact = maxWidth < 360.dp
        val horizontalPadding = if (compact) 16.dp else 26.dp

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = 0.16f), Color.Transparent),
                    center = Offset(size.width * 0.72f, size.height * 0.16f),
                    radius = size.width * 0.42f
                ),
                radius = size.width * 0.42f,
                center = Offset(size.width * 0.72f, size.height * 0.16f)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = horizontalPadding,
                    top = if (compact) 24.dp else 30.dp,
                    end = horizontalPadding,
                    bottom = 22.dp
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = if (compact) 184.dp else 146.dp),
                horizontalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 18.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Top
                ) {
                    Text(
                        text = "Today order / ${order.dayTypeLabel}".uppercase(),
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = accent,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = if (compact) 12.sp else 14.sp,
                            lineHeight = if (compact) 14.sp else 16.sp,
                            letterSpacing = if (compact) 1.2.sp else 2.2.sp
                        ),
                        maxLines = if (compact) 3 else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            color = colors.textPrimary,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Black,
                            fontSize = if (compact) 27.sp else 31.sp,
                            lineHeight = if (compact) 29.sp else 33.sp,
                            letterSpacing = 0.sp
                        ),
                        maxLines = if (compact) 3 else 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = order.reason,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = colors.textSecondary,
                            fontWeight = FontWeight.Medium,
                            fontSize = if (compact) 13.sp else 14.sp,
                            lineHeight = if (compact) 17.sp else 18.sp
                        ),
                        maxLines = if (compact) 5 else 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                QuestReadinessRing(
                    progress = order.readinessProgress,
                    accent = accent,
                    compact = compact,
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(if (compact) 96.dp else 110.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuestMetric(
                    value = order.durationText,
                    label = order.durationLabel,
                    accent = colors.textSecondary,
                    compact = compact,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = if (compact) 72.dp else 58.dp)
                ) {
                    QuestClockBadge(accent = colors.textSecondary)
                }
                QuestMetric(
                    value = order.outcomeText,
                    label = order.outcomeLabel,
                    accent = accent,
                    compact = compact,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = if (compact) 72.dp else 58.dp)
                ) {
                    QuestXpBadge(accent = accent)
                }
            }
            Spacer(modifier = Modifier.height(18.dp))

            QuestPrimaryButton(
                text = order.primaryActionLabel,
                enabled = order.actionEnabled,
                accent = accent,
                onClick = onStartWorkout,
                modifier = Modifier.fillMaxWidth()
            )
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
    val shape = RoundedCornerShape(13.dp)
    Row(
        modifier = modifier
            .techSurface(
                shape = shape,
                active = false,
                accent = accent,
                role = TechSurfaceRole.Plate
            )
            .padding(horizontal = if (compact) 8.dp else 14.dp),
        horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 25.dp else 29.dp)
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
                    fontSize = if (compact) 14.sp else 17.sp,
                    lineHeight = if (compact) 17.sp else 19.sp
                ),
                maxLines = if (compact) 3 else 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Medium,
                    fontSize = if (compact) 8.sp else 9.sp,
                    lineHeight = if (compact) 10.sp else 11.sp,
                    letterSpacing = if (compact) 0.3.sp else 0.6.sp
                ),
                maxLines = 2,
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
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    val clamped = progress.coerceIn(0f, 1f)
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 10.dp.toPx()
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
                color = accent.copy(alpha = 0.16f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Butt)
            )
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        accent,
                        Color(0xFF19B8FF),
                        accent
                    )
                ),
                startAngle = -90f,
                sweepAngle = 360f * clamped,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Butt)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${(clamped * 100).toInt()}%",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = colors.textPrimary,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Black,
                    fontSize = if (compact) 29.sp else 34.sp,
                    lineHeight = if (compact) 31.sp else 35.sp,
                    textAlign = TextAlign.Center
                )
            )
            Text(
                text = "Готовність".uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (compact) 8.sp else 11.sp,
                    lineHeight = if (compact) 10.sp else 13.sp,
                    letterSpacing = if (compact) 0.2.sp else 0.7.sp,
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
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .heightIn(min = 58.dp)
            .testTag(SystemUiTestTags.TODAY_ORDER_CTA)
            .techSurface(
                shape = shape,
                active = enabled,
                accent = accent,
                role = TechSurfaceRole.Button,
                enabled = enabled
            )
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
            modifier = Modifier.padding(start = 24.dp, end = 54.dp),
            style = MaterialTheme.typography.titleMedium.copy(
                color = if (enabled) colors.textPrimary else colors.textMuted,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.6.sp,
                textAlign = TextAlign.Center
            ),
            maxLines = 2,
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
