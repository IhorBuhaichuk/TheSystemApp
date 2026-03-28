package com.ihor.thesystem.feature.statistics.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.core.ui.components.buildHexagonPath

@Composable
fun StatRightPanel(
    month: String,
    weight: String,
    height: String,
    modifier: Modifier = Modifier,
    onWeightTap: () -> Unit = {},
    onHeightTap: () -> Unit = {}
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HexStatBadge(
            icon = Icons.Filled.Flag,
            label = "ТРЕНУВАЛЬНИЙ МІСЯЦЬ",
            value = month,
            modifier = Modifier.fillMaxWidth()
        )
        HexStatBadge(
            icon = Icons.Filled.MonitorWeight,
            label = "ПОТОЧНА ВАГА",
            value = "$weight кг",
            onClick = onWeightTap,
            modifier = Modifier.fillMaxWidth()
        )
        HexStatBadge(
            icon = Icons.Filled.Height,
            label = "ЗРІСТ",
            value = "$height см",
            onClick = onHeightTap,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun HexStatBadge(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .aspectRatio(1.155f)
            .padding(2.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = buildHexagonPath(size, rotationDegrees = 0f)
            drawPath(path, PanelSurface)
            drawPath(path, NeonCyan, style = Stroke(width = 2.dp.toPx()))
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                color = TextSecondary,
                fontSize = 7.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 8.sp
            )
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}
