package com.ihor.thesystem.feature.statistics.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.ihor.thesystem.domain.repository.DailyTonnageStats
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TonnageChartCanvas(
    entries: List<DailyTonnageStats>,
    modifier: Modifier = Modifier
) {
    // Якщо у вас у темі є ці кольори, можете імпортувати їх, тут використано хардкод для надійності
    val backgroundDeep = Color(0xFF0A0A0A)
    val neonGold = Color(0xFFFFD700)

    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(backgroundDeep)
            .padding(16.dp)
    ) {
        if (entries.isEmpty()) return@Canvas

        val maxTonnage = entries.maxOfOrNull { it.totalTonnage }?.toFloat() ?: 1f
        val paddingX = 100f // Простір зліва для підписів осі Y
        val paddingY = 60f  // Простір знизу для підписів осі X

        val chartWidth = size.width - paddingX
        val chartHeight = size.height - paddingY

        // Малюємо вісі X та Y
        drawLine(
            color = Color.DarkGray,
            start = Offset(paddingX, 0f),
            end = Offset(paddingX, chartHeight),
            strokeWidth = 2f
        )
        drawLine(
            color = Color.DarkGray,
            start = Offset(paddingX, chartHeight),
            end = Offset(size.width, chartHeight),
            strokeWidth = 2f
        )

        val path = Path()
        val stepX = if (entries.size > 1) chartWidth / (entries.size - 1) else chartWidth

        entries.forEachIndexed { index, entry ->
            val x = paddingX + (index * stepX)
            // Обчислюємо висоту точки відносно максимуму
            val y = chartHeight - ((entry.totalTonnage.toFloat() / maxTonnage) * chartHeight)

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }

            // Малюємо точку на графіку
            drawCircle(
                color = neonGold,
                radius = 6f,
                center = Offset(x, y)
            )

            // Підписи для вісі X (малюємо перший, останній або кожен 3-й день, щоб текст не зливався)
            if (index == 0 || index == entries.lastIndex || entries.size <= 5 || index % 3 == 0) {
                val date = Date(entry.dateUnixTimestamp * 1000)
                val dateString = SimpleDateFormat("dd.MM", Locale.getDefault()).format(date)
                drawText(
                    textMeasurer = textMeasurer,
                    text = dateString,
                    topLeft = Offset(x - 30f, chartHeight + 15f)
                )
            }
        }

        // Малюємо лінію самого графіка
        drawPath(
            path = path,
            color = neonGold,
            style = Stroke(width = 5f)
        )

        // Підписи для вісі Y (0, половина максимуму, максимум)
        val yLabels = listOf(maxTonnage, maxTonnage / 2, 0f)
        yLabels.forEach { value ->
            val y = chartHeight - ((value / maxTonnage) * chartHeight)
            drawText(
                textMeasurer = textMeasurer,
                text = "${value.toInt()} кг",
                topLeft = Offset(0f, y - 20f)
            )
        }
    }
}