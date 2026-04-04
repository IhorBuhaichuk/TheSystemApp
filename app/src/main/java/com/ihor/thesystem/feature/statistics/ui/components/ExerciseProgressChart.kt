package com.ihor.thesystem.feature.statistics.ui.components

import android.graphics.BlurMaskFilter
import android.graphics.Paint
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import com.ihor.thesystem.core.theme.NeonCyan
import com.ihor.thesystem.core.theme.PanelBorder
import com.ihor.thesystem.core.ui.components.buildHexagonPath
import com.ihor.thesystem.data.local.room.dao.ExerciseWeightHistory

/**
 * ГРАФІК ПРОГРЕСУ (Custom Canvas Implementation)
 * Реалізовано через Canvas для повної стабільності та дотримання кіберпанк-стилю.
 */
@Composable
fun ExerciseProgressChart(
    history: List<ExerciseWeightHistory>,
    modifier: Modifier = Modifier,
    accentColor: Color = NeonCyan
) {
    if (history.size < 2) return

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(history) {
        animProgress.animateTo(1f, animationSpec = tween(1500, easing = EaseOutQuart))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(vertical = 8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            
            val weights = history.map { it.weight.toFloat() }
            val minWeight = weights.minOrNull() ?: 0f
            val maxWeight = weights.maxOrNull() ?: 100f
            val range = (maxWeight - minWeight).coerceAtLeast(1f)
            
            val points = history.mapIndexed { index, item ->
                val x = (index.toFloat() / (history.size - 1)) * width
                val y = height - ((item.weight.toFloat() - minWeight) / range * height * 0.8f) - (height * 0.1f)
                Offset(x, y)
            }

            val strokePath = Path().apply {
                points.forEachIndexed { index, offset ->
                    if (index == 0) moveTo(offset.x, offset.y)
                    else lineTo(offset.x, offset.y)
                }
            }

            val fillPath = Path().apply {
                addPath(strokePath)
                lineTo(points.last().x, height)
                lineTo(points.first().x, height)
                close()
            }
            
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(accentColor.copy(alpha = 0.2f * animProgress.value), Color.Transparent),
                    startY = points.minOf { it.y },
                    endY = height
                )
            )

            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    color = accentColor.toArgb()
                    strokeWidth = 4.dp.toPx()
                    style = Paint.Style.STROKE
                    strokeCap = Paint.Cap.ROUND
                    strokeJoin = Paint.Join.ROUND
                    isAntiAlias = true
                    maskFilter = BlurMaskFilter(10f, BlurMaskFilter.Blur.NORMAL)
                    alpha = (70 * animProgress.value).toInt()
                }
                canvas.nativeCanvas.drawPath(strokePath.asAndroidPath(), paint)
            }

            drawPath(
                path = strokePath,
                color = accentColor.copy(alpha = 0.9f * animProgress.value),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            points.forEach { offset ->
                val hexSize = 4.dp.toPx()
                val hexPath = buildHexagonPath(
                    size = Size(hexSize * 2, hexSize * 2)
                )
                
                withTransform({
                    translate(offset.x - hexSize, offset.y - hexSize)
                }) {
                    drawPath(hexPath, Color.White.copy(alpha = animProgress.value))
                    drawPath(hexPath, accentColor.copy(alpha = animProgress.value), style = Stroke(1.dp.toPx()))
                }
            }
            
            drawLine(
                color = PanelBorder.copy(alpha = 0.2f),
                start = Offset(0f, height),
                end = Offset(width, height),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}
