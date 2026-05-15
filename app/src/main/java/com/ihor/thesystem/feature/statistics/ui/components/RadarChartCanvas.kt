package com.ihor.thesystem.feature.statistics.ui.components

import android.graphics.BlurMaskFilter
import android.graphics.Paint as AndroidPaint
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.asUiText
import com.ihor.thesystem.domain.model.MuscleGroup
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalTextApi::class)
@Composable
fun RadarChartCanvas(
    attributes: Map<MuscleGroup, Float>,
    modifier: Modifier = Modifier
) {
    val colors = SystemTheme.colors
    val textMeasurer = rememberTextMeasurer()
    val labels = MuscleGroup.entries
    val numPoints = labels.size
    val context = LocalContext.current
    val labelStyle = MaterialTheme.typography.labelSmall.copy(
        color = colors.textSecondary,
        fontWeight = FontWeight.Bold
    )

    // Pre-resolve names to use in drawing
    val labelNames = labels.map { it.asUiText().asString(context).uppercase() }

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2f * 0.75f

        // Helper to get coordinates for a point at a given index and distance from center
        fun getCoordinates(index: Int, currentRadius: Float): Offset {
            val angle = index * (2 * Math.PI / numPoints) - Math.PI / 2
            return Offset(
                x = center.x + currentRadius * cos(angle).toFloat(),
                y = center.y + currentRadius * sin(angle).toFloat()
            )
        }

        // 1. Draw Background Grid (4 hexagons)
        val gridLevels = 4
        for (i in 1..gridLevels) {
            val levelRadius = radius * (i.toFloat() / gridLevels)
            val gridPath = Path().apply {
                val start = getCoordinates(0, levelRadius)
                moveTo(start.x, start.y)
                for (j in 1 until numPoints) {
                    val next = getCoordinates(j, levelRadius)
                    lineTo(next.x, next.y)
                }
                close()
            }
            drawPath(
                path = gridPath,
                color = colors.borderSubtle.copy(alpha = 0.3f),
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // 2. Draw Axis Lines
        for (i in 0 until numPoints) {
            val end = getCoordinates(i, radius)
            drawLine(
                color = colors.borderSubtle.copy(alpha = 0.3f),
                start = center,
                end = end,
                strokeWidth = 1.dp.toPx()
            )
        }

        // 3. Draw Character Attributes Polygon
        if (attributes.isNotEmpty()) {
            val progressPath = Path().apply {
                val firstVal = attributes[labels[0]] ?: 0f
                val start = getCoordinates(0, radius * (firstVal / 100f).coerceIn(0f, 1f))
                moveTo(start.x, start.y)
                for (i in 1 until numPoints) {
                    val value = attributes[labels[i]] ?: 0f
                    val next = getCoordinates(i, radius * (value / 100f).coerceIn(0f, 1f))
                    lineTo(next.x, next.y)
                }
                close()
            }

            // Fill
            drawPath(
                path = progressPath,
                color = colors.accentPrimary.copy(alpha = 0.15f)
            )

            // Stroke with Glow effect using native canvas
            drawContext.canvas.nativeCanvas.apply {
                val paint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                    color = colors.accentPrimary.toArgb()
                    strokeWidth = 4f
                    style = android.graphics.Paint.Style.STROKE
                    maskFilter = BlurMaskFilter(15f, BlurMaskFilter.Blur.NORMAL)
                }
                drawPath(progressPath.asAndroidPath(), paint)
            }

            drawPath(
                path = progressPath,
                color = colors.accentPrimary,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // 4. Draw Labels
        for (i in 0 until numPoints) {
            val labelRadius = radius + 25.dp.toPx()
            val pos = getCoordinates(i, labelRadius)
            val labelText = labelNames[i]
            
            val textLayoutResult = textMeasurer.measure(
                text = AnnotatedString(labelText),
                style = labelStyle
            )
            
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(
                    pos.x - textLayoutResult.size.width / 2f,
                    pos.y - textLayoutResult.size.height / 2f
                )
            )
        }
    }
}
