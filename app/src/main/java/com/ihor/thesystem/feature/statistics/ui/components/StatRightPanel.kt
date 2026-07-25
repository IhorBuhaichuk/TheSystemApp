package com.ihor.thesystem.feature.statistics.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ihor.thesystem.R
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.components.buildHexagonPath
import com.ihor.thesystem.core.ui.components.systemClickable

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
            label = stringResource(R.string.text_month),
            value = month,
            modifier = Modifier.fillMaxWidth()
        )
        HexStatBadge(
            icon = Icons.Filled.MonitorWeight,
            label = stringResource(R.string.text_weight),
            value = "$weight кг",
            onClick = onWeightTap,
            modifier = Modifier.fillMaxWidth()
        )
        HexStatBadge(
            icon = Icons.Filled.Height,
            label = stringResource(R.string.text_height),
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
    val colors = SystemTheme.colors
    Box(
        modifier = modifier
            .aspectRatio(1.155f)
            .padding(2.dp)
            .systemClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val path = buildHexagonPath(size, rotationDegrees = 0f)
            drawPath(path, colors.surfaceGlassStrong)
            drawPath(path, colors.accentPrimary, style = Stroke(width = 2.dp.toPx()))
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.accentPrimary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
