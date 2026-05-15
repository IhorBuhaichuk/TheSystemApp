package com.ihor.thesystem.feature.status.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.components.sciPanel

@Composable
fun StatDataCard(
    label: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val colors = SystemTheme.colors
    val imageVector = when (icon) {
        "weight" -> Icons.Default.MonitorWeight
        "height" -> Icons.Default.Height
        "streak" -> Icons.Default.Whatshot
        else -> Icons.Default.MonitorWeight
    }

    Column(
        modifier = modifier
            .sciPanel(
                borderColor = colors.borderSubtle.copy(alpha = 0.3f),
                backgroundColor = colors.surfaceGlassStrong.copy(alpha = 0.5f),
                cornerCut = 8.dp
            )
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = colors.accentPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = colors.textSecondary)
        )
    }
}
