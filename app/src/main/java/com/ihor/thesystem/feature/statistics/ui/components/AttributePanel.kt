package com.ihor.thesystem.feature.statistics.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ihor.thesystem.domain.model.MuscleGroup

@Composable
fun AttributePanel(
    characterAttributes: Map<MuscleGroup, Float>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // --- Spider Chart ---
        RadarChartCanvas(
            attributes = characterAttributes,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(horizontal = 24.dp, vertical = 8.dp)
        )
    }
}
