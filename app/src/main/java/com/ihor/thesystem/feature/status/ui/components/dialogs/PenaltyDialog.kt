package com.ihor.thesystem.feature.status.ui.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemDialogContainer

@Composable
fun PenaltyActivatedDialog(onDismiss: () -> Unit) {
    val colors = SystemTheme.colors
    PenaltyStateDialog(
        title = "Штрафна зона",
        message = "2 провали поспіль зафіксовано.",
        emphasis = "Цільові ваги знижені на 20%.",
        note = "Виконай 2 головні тренування поспіль, щоб відновити звичайні цілі.",
        buttonText = "Зрозуміло",
        accent = colors.accentError,
        onDismiss = onDismiss
    )
}

@Composable
fun PenaltyDeactivatedDialog(onDismiss: () -> Unit) {
    val colors = SystemTheme.colors
    PenaltyStateDialog(
        title = "Штраф знято",
        message = "2 головні тренування поспіль виконано успішно.",
        emphasis = "Продуктивність відновлено.",
        note = null,
        buttonText = "Продовжити",
        accent = colors.accentSuccess,
        onDismiss = onDismiss
    )
}

@Composable
private fun PenaltyStateDialog(
    title: String,
    message: String,
    emphasis: String,
    note: String?,
    buttonText: String,
    accent: Color,
    onDismiss: () -> Unit
) {
    val colors = SystemTheme.colors

    Dialog(onDismissRequest = onDismiss) {
        SystemDialogContainer(accent = accent) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(SystemItemSpacing),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(36.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = accent,
                        fontWeight = FontWeight.Black
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium.copy(color = colors.textPrimary),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                HorizontalDivider(color = accent.copy(alpha = 0.3f))
                Text(
                    text = emphasis,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = accent,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                if (note != null) {
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                SystemButton(
                    text = buttonText,
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    accent = accent,
                    glow = true
                )
            }
        }
    }
}
