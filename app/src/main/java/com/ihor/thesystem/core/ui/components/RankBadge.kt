package com.ihor.thesystem.core.ui.components

import android.graphics.BlurMaskFilter
import android.graphics.Paint as AndroidPaint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.RajdhaniFamily
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.theme.TekoFamily
import com.ihor.thesystem.domain.model.Rank

/**
 * Преміальний бейдж рангу.
 * Відображає слово "RANK" та літеру рангу в гексагональній рамці зі світінням.
 */
@Composable
fun RankBadge(
    rank: Rank,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val colors = SystemTheme.colors
    val rankColor = when (rank) {
        Rank.E -> colors.rankE
        Rank.D -> colors.rankD
        Rank.C -> colors.rankC
        Rank.B -> colors.rankB
        Rank.A -> colors.rankA
        Rank.S -> colors.rankS
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val s = size.toPx()
            val cut = s * 0.2f
            val path = Path().apply {
                moveTo(cut, 0f)
                lineTo(s - cut, 0f)
                lineTo(s, cut)
                lineTo(s, s - cut)
                lineTo(s - cut, s)
                lineTo(cut, s)
                lineTo(0f, s - cut)
                lineTo(0f, cut)
                close()
            }

            // Фонова заливка
            drawPath(path, rankColor.copy(alpha = 0.15f))

            // Ефект світіння (Glow)
            drawIntoCanvas { canvas ->
                val paint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
                    this.color = rankColor.toArgb()
                    this.maskFilter = BlurMaskFilter(12f, BlurMaskFilter.Blur.NORMAL)
                }
                canvas.nativeCanvas.drawPath(path.asAndroidPath(), paint)
            }

            // Обводка
            drawPath(
                path = path,
                color = rankColor.copy(alpha = 0.8f),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "RANK",
                style = TextStyle(
                    fontFamily = RajdhaniFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.16f).sp,
                    color = colors.textPrimary.copy(alpha = 0.6f),
                    letterSpacing = 0.sp
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text = rank.name,
                style = TextStyle(
                    fontFamily = TekoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.55f).sp,
                    color = colors.textPrimary,
                    shadow = Shadow(color = rankColor, blurRadius = 10f)
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.offset(y = (-4).dp)
            )
        }
    }
}
