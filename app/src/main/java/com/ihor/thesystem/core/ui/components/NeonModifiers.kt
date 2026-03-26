package com.ihor.thesystem.core.ui.components

import android.graphics.BlurMaskFilter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Малює sci-fi панель із зрізаними кутами (top-left & bottom-right).
 */
fun Modifier.sciPanel(
    borderColor: Color,
    backgroundColor: Color,
    cornerCut: Dp = 12.dp,
    borderWidth: Dp = 1.5.dp
): Modifier = this.drawBehind {
    val cut = cornerCut.toPx()
    val path = Path().apply {
        moveTo(cut, 0f)
        lineTo(size.width, 0f)
        lineTo(size.width, size.height - cut)
        lineTo(size.width - cut, size.height)
        lineTo(0f, size.height)
        lineTo(0f, cut)
        close()
    }
    drawPath(path, backgroundColor)
    drawPath(path, borderColor, style = Stroke(width = borderWidth.toPx()))
}

/**
 * Неонове свічення навколо елементу (Blur ефект).
 */
fun Modifier.neonGlow(color: Color, radius: Dp = 10.dp): Modifier = this.drawBehind {
    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            asFrameworkPaint().apply {
                isAntiAlias = true
                this.color = android.graphics.Color.TRANSPARENT
                maskFilter  = BlurMaskFilter(radius.toPx(), BlurMaskFilter.Blur.NORMAL)
            }
            this.color = color.copy(alpha = 0.45f)
        }
        canvas.drawRoundRect(
            left = 0f, top = 0f,
            right = size.width, bottom = size.height,
            radiusX = 4f, radiusY = 4f,
            paint = paint
        )
    }
}