package com.ihor.thesystem.feature.mode.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.feature.mode.viewmodel.ActiveDayUiModel
import com.ihor.thesystem.feature.statistics.ui.components.MatrixEntryCard

@Composable
fun ActiveDayCard(
    data: ActiveDayUiModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(PanelSurface)
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(32.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (data.matrixEntries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "RECOVERY PROTOCOL ACTIVE",
                    color = TextSecondary.copy(alpha = 0.2f),
                    fontFamily = RajdhaniFamily,
                    fontSize = 14.sp,
                    letterSpacing = 4.sp
                )
            }
        } else {
            data.matrixEntries.forEach { entry ->
                MatrixEntryCard(
                    entry = entry,
                    onCardClick = { /* View details functionality could be added later */ },
                    onSetupClick = { /* Setup goals functionality could be added later */ }
                )
            }
        }
    }
}
