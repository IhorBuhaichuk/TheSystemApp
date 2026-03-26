package com.ihor.thesystem.feature.statistics.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.core.ui.components.buildHexagonPath
import androidx.compose.foundation.clickable

@Composable
fun StatRightPanel(
    month: String,
    weight: String,
    height: String,
    modifier: Modifier = Modifier,
    onWeightTap: () -> Unit = {}
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HexStatBadge(
            icon     = Icons.Filled.Favorite,
            value    = month,
            modifier = Modifier.fillMaxWidth()
        )
        HexStatBadge(
            icon     = Icons.Filled.Shield,
            value    = weight,
            onClick  = onWeightTap,
            modifier = Modifier.fillMaxWidth()
        )
        HexStatBadge(
            icon     = Icons.Filled.FlashOn,
            value    = height,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun HexStatBadge(
    icon: ImageVector,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    // Flat-top hex ratio: width/height = 2/√3 ≈ 1.155
    Box(
        modifier = modifier
            .aspectRatio(1.155f)
            .padding(4.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = buildHexagonPath(size, rotationDegrees = 0f)
            drawPath(path, PanelSurface)
            drawPath(path, NeonCyan, style = Stroke(width = 2.5.dp.toPx()))
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint     = TextSecondary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text       = value,
                color      = TextPrimary,
                fontSize   = 19.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}