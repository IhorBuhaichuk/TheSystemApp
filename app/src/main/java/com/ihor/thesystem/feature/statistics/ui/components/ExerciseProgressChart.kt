package com.ihor.thesystem.feature.statistics.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.components.buildHexagonPath
import com.ihor.thesystem.domain.model.WeightHistoryEntry

/**
 * ГРАФІК ПРОГРЕСУ (Custom Canvas Implementation)
 * Реалізовано через Canvas для повної стабільності та дотримання кіберпанк-стилю.
 */
@Composable
fun ExerciseProgressChart(
    history: List<WeightHistoryEntry>,
    modifier: Modifier = Modifier,
    accentColor: Color? = null
) {
    if (history.size < 2) return
    val colors = SystemTheme.colors
    val motion = SystemTheme.motion
    val accent = accentColor ?: colors.accentPrimary

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(history) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(motion.enterExitMillis, easing = EaseOutQuart)
        )
    }

    val normalizedWeights = remember(history) {
        val minWeight = history.minOf { it.weight }
        val maxWeight = history.maxOf { it.weight }
        val range = (maxWeight - minWeight).coerceAtLeast(1.0)
        history.map { ((it.weight - minWeight) / range).toFloat().coerceIn(0f, 1f) }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(vertical = 8.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val width = size.width
                    val height = size.height
                    val points = normalizedWeights.mapIndexed { index, normalized ->
                        val x = (index.toFloat() / normalizedWeights.lastIndex) * width
                        val y = height - (normalized * height * 0.8f) - (height * 0.1f)
                        Offset(x, y)
                    }
                    val strokePath = Path().apply {
                        points.forEachIndexed { index, offset ->
                            if (index == 0) moveTo(offset.x, offset.y) else lineTo(offset.x, offset.y)
                        }
                    }
                    val fillPath = Path().apply {
                        addPath(strokePath)
                        lineTo(points.last().x, height)
                        lineTo(points.first().x, height)
                        close()
                    }
                    val fillBrush = Brush.verticalGradient(
                        colors = listOf(accent.copy(alpha = 0.2f), Color.Transparent),
                        startY = points.minOf { it.y },
                        endY = height
                    )
                    val hexSize = 4.dp.toPx()
                    val hexPath = buildHexagonPath(Size(hexSize * 2, hexSize * 2))
                    val glowStroke = Stroke(
                        width = 5.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                    val lineStroke = Stroke(
                        width = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                    val pointStroke = Stroke(1.dp.toPx())

                    onDrawBehind {
                        val progress = animProgress.value
                        drawPath(path = fillPath, brush = fillBrush, alpha = progress)
                        drawPath(
                            path = strokePath,
                            color = accent.copy(alpha = 0.22f * progress),
                            style = glowStroke
                        )
                        drawPath(
                            path = strokePath,
                            color = accent.copy(alpha = 0.9f * progress),
                            style = lineStroke
                        )
                        points.forEach { offset ->
                            withTransform({
                                translate(offset.x - hexSize, offset.y - hexSize)
                            }) {
                                drawPath(hexPath, colors.textPrimary.copy(alpha = progress))
                                drawPath(
                                    path = hexPath,
                                    color = accent.copy(alpha = progress),
                                    style = pointStroke
                                )
                            }
                        }
                        drawLine(
                            color = colors.borderSubtle.copy(alpha = 0.2f),
                            start = Offset(0f, height),
                            end = Offset(width, height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }
        ) {}
    }
}
