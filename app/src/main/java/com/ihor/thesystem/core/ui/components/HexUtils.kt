package com.ihor.thesystem.core.ui.components

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import kotlin.math.cos
import kotlin.math.sin

/**
 * Будує Path шестикутника для Canvas.
 * @param rotationDegrees 0f = flat-top (широкий), 30f = pointy-top (вузький/вертикальний)
 */
fun buildHexagonPath(size: Size, rotationDegrees: Float = 0f): Path {
    return Path().apply {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r  = minOf(cx, cy) * 0.92f
        val rotRad = Math.toRadians(rotationDegrees.toDouble())
        for (i in 0..5) {
            val angle = rotRad + Math.PI / 3.0 * i
            val x = (cx + r * cos(angle)).toFloat()
            val y = (cy + r * sin(angle)).toFloat()
            if (i == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }
}