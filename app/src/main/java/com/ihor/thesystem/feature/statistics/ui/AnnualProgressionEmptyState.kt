package com.ihor.thesystem.feature.statistics.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemScreenPadding
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.components.DarkGlassCard
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemGhostButton

@Composable
internal fun AnnualProgressionEmptyState(
    onBack: () -> Unit,
    onCreateInAi: () -> Unit,
    onCreateManually: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = SystemScreenPadding)
            .padding(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)
    ) {
        AnnualDetailsHeader(onBack = onBack)
        Spacer(modifier = Modifier.height(8.dp))
        AnnualCreateOptionCard(
            icon = Icons.Filled.AutoAwesome,
            title = "Створити за допомогою ШІ",
            text = "ШІ запропонує місячні цілі для вибраних вправ і збереже їх у річний план.",
            buttonText = "Створити з ШІ",
            onClick = onCreateInAi,
            active = true
        )
        AnnualCreateOptionCard(
            icon = Icons.Filled.Edit,
            title = "Створити самостійно",
            text = "Відкриється таблиця з вправами з усіх днів циклу. Значення для кожного місяця можна ввести вручну.",
            buttonText = "Відкрити таблицю",
            onClick = onCreateManually,
            active = false
        )
    }
}

@Composable
private fun AnnualCreateOptionCard(
    icon: ImageVector,
    title: String,
    text: String,
    buttonText: String,
    onClick: () -> Unit,
    active: Boolean
) {
    val colors = SystemTheme.colors
    val iconShape = RoundedCornerShape(SystemTheme.shapes.medium)
    val iconBackground = if (active) colors.accentAiSoft else colors.overlayLight
    val iconBorder = if (active) colors.accentAi.copy(alpha = 0.24f) else colors.borderSubtle
    val iconTint = if (active) colors.accentAi else colors.textSecondary
    DarkGlassCard(active = active) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(iconShape)
                    .background(iconBackground)
                    .border(1.dp, iconBorder, iconShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            if (active) {
                SystemButton(
                    text = buttonText,
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = onClick,
                    accent = colors.accentAi,
                    glow = true,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                SystemGhostButton(
                    text = buttonText,
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = onClick,
                    accent = colors.textSecondary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
