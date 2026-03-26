package com.ihor.thesystem.feature.mode.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.*

@Composable
fun ConfirmAdvanceDialog(
    currentDay: Int,
    onConfirm: () -> Unit,
    onForceComplete: () -> Unit,
    onDismiss: () -> Unit
) {
    val nextDay = if (currentDay >= 4) 1 else currentDay + 1

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = PanelSurface,
        shape            = RoundedCornerShape(4.dp),
        icon = {
            Icon(Icons.Filled.SwapHoriz, contentDescription = null, tint = NeonCyan)
        },
        title = {
            Text(
                text       = "[ ПЕРЕХІД ДО ДНЯ $nextDay ]",
                color      = NeonCyan,
                fontFamily = FontFamily.Monospace,
                fontSize   = 14.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text       = "Завершити День $currentDay та розпочати День $nextDay?",
                    color      = TextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 12.sp
                )
                Text(
                    text       = "Невиконані завдання будуть позначені як ПРОВАЛЕНО.",
                    color      = NeonRed.copy(alpha = 0.8f),
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 11.sp
                )
                Divider(color = PanelBorder.copy(alpha = 0.3f))
                Text(
                    text       = "Або примусово завершити всі квести як ВИКОНАНО:",
                    color      = TextSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 11.sp
                )
                TextButton(
                    onClick = onForceComplete,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text       = "✓ ЗАВЕРШИТИ ВСЕ ЯК ВИКОНАНО",
                        color      = NeonGreen,
                        fontFamily = FontFamily.Monospace,
                        fontSize   = 11.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text       = "ПЕРЕЙТИ →",
                    color      = NeonCyan,
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 12.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text       = "СКАСУВАТИ",
                    color      = TextSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 12.sp
                )
            }
        }
    )
}