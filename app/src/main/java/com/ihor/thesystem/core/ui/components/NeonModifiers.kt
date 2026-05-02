package com.ihor.thesystem.core.ui.components

import android.graphics.BlurMaskFilter
import android.graphics.Paint as AndroidPaint
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip

import com.ihor.thesystem.core.theme.CornerRadius

/**
 * Малює статичну sci-fi панель із зрізаними кутами.
 * Оптимізовано для високої продуктивності UI.
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
 * Неонове свічення навколо елементу.
 * Використовує апаратне розмиття для статичних об'єктів.
 */
fun Modifier.neonGlow(color: Color, radius: Dp = 10.dp): Modifier = this.drawBehind {
    drawIntoCanvas { canvas ->
        val paint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
            this.color = color.copy(alpha = 0.45f).toArgb()
            maskFilter = BlurMaskFilter(radius.toPx(), BlurMaskFilter.Blur.NORMAL)
        }
        canvas.nativeCanvas.drawRoundRect(
            0f,
            0f,
            size.width,
            size.height,
            4f,
            4f,
            paint
        )
    }
}

fun Modifier.glassCard(radius: Dp = CornerRadius): Modifier = this
    .clip(RoundedCornerShape(radius))
    .background(Color.White.copy(alpha = 0.08f))
    .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(radius))
