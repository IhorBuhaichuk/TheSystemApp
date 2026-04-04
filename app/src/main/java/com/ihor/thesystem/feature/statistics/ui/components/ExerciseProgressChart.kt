package com.ihor.thesystem.feature.statistics.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ihor.thesystem.core.theme.NeonCyan
import com.ihor.thesystem.core.theme.PanelBorder
import com.ihor.thesystem.data.local.room.dao.ExerciseWeightHistory
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineSpec
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.shader.color
import com.patrykandpatrick.vico.compose.common.shader.verticalGradient
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.shader.DynamicShader
import com.patrykandpatrick.vico.core.common.shape.Shapes
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ExerciseProgressChart(
    history: List<ExerciseWeightHistory>,
    modifier: Modifier = Modifier,
    accentColor: Color = NeonCyan
) {
    if (history.size < 2) return

    val modelProducer = remember { CartesianChartModelProducer() }
    
    LaunchedEffect(history) {
        modelProducer.runTransaction {
            lineSeries {
                series(history.map { it.weight.toFloat() })
            }
        }
    }

    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM")
    val zoneId = ZoneId.systemDefault()

    Column(modifier = modifier.fillMaxWidth().height(100.dp)) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                layers = arrayOf(
                    rememberLineCartesianLayer(
                        lineProvider = LineCartesianLayer.LineProvider.series(
                            rememberLineSpec(
                                shader = DynamicShader.color(accentColor),
                                backgroundShader = DynamicShader.verticalGradient(
                                    arrayOf(accentColor, Color.Transparent)
                                ),
                                point = rememberShapeComponent(
                                    shape = Shapes.rect,
                                    color = accentColor,
                                    strokeColor = Color.White,
                                    strokeWidth = 2.dp
                                ),
                                pointSizeDp = 4f
                            )
                        )
                    )
                ),
                startAxis = rememberStartAxis(
                    label = null,
                    tick = null,
                    guideline = null,
                    line = null
                ),
                bottomAxis = rememberBottomAxis(
                    valueFormatter = { value, _, _ ->
                        history.getOrNull(value.toInt())?.let {
                            Instant.ofEpochMilli(it.timestamp).atZone(zoneId).format(dateFormatter)
                        } ?: ""
                    },
                    label = null,
                    tick = null,
                    guideline = null,
                    line = rememberLineComponent(color = PanelBorder.copy(alpha = 0.2f))
                )
            ),
            modelProducer = modelProducer,
            modifier = Modifier.fillMaxSize()
        )
    }
}
