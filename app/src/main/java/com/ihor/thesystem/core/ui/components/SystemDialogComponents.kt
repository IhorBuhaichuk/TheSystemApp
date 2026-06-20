package com.ihor.thesystem.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ihor.thesystem.core.theme.SystemCardPadding
import com.ihor.thesystem.core.theme.SystemControlHeight
import com.ihor.thesystem.core.theme.SystemScreenPadding
import com.ihor.thesystem.core.theme.SystemTheme

@Composable
fun SystemDialogContainer(
    modifier: Modifier = Modifier,
    accent: Color = SystemTheme.colors.accentPrimary,
    contentPadding: Dp = SystemScreenPadding,
    maxWidth: Dp = 560.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = SystemTheme.colors
    val shape = systemLargePanelShape()

    Box(
        modifier = modifier
            .widthIn(max = maxWidth)
            .fillMaxWidth()
            .shadow(
                elevation = SystemTheme.glow.activeElevation,
                shape = shape,
                ambientColor = accent.copy(alpha = 0.16f),
                spotColor = SystemTheme.glow.shadowSpot
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        colors.surfaceGlassStrong.copy(alpha = 0.94f),
                        colors.surfaceGlass.copy(alpha = 0.90f),
                        colors.surfaceGlassSoft.copy(alpha = 0.82f)
                    )
                )
            )
            .border(
                BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        listOf(
                            colors.overlayStrong,
                            accent.copy(alpha = 0.42f),
                            colors.borderSubtle
                        )
                    )
                ),
                shape
            )
            .padding(contentPadding)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content
        )
    }
}

@Composable
fun systemOutlinedTextFieldColors(
    accent: Color = SystemTheme.colors.accentPrimary
): TextFieldColors {
    val colors = SystemTheme.colors
    return OutlinedTextFieldDefaults.colors(
        focusedBorderColor = accent,
        unfocusedBorderColor = colors.borderSubtle,
        focusedContainerColor = colors.overlayLight,
        unfocusedContainerColor = colors.overlayLight.copy(alpha = 0.035f),
        cursorColor = accent,
        focusedTextColor = colors.textPrimary,
        unfocusedTextColor = colors.textPrimary,
        focusedPlaceholderColor = colors.textMuted,
        unfocusedPlaceholderColor = colors.textMuted,
        focusedLabelColor = accent,
        unfocusedLabelColor = colors.textSecondary,
        errorBorderColor = colors.accentError,
        errorCursorColor = colors.accentError,
        errorLabelColor = colors.accentError
    )
}

@Composable
fun SystemGhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accent: Color = SystemTheme.colors.textSecondary,
    icon: ImageVector? = null
) {
    val colors = SystemTheme.colors
    val shape = RoundedCornerShape(SystemTheme.shapes.medium)

    Box(
        modifier = modifier
            .height(SystemControlHeight)
            .clip(shape)
            .background(if (enabled) colors.overlayLight else colors.overlayLight.copy(alpha = 0.02f))
            .border(1.dp, if (enabled) colors.borderSubtle else colors.borderMuted, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = SystemCardPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) accent else colors.textMuted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    color = if (enabled) accent else colors.textMuted,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
