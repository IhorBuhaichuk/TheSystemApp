package com.ihor.thesystem.feature.mode.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.*

@Composable
fun SyncAnchorDialog(
    dayNumber: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PanelSurface,
        shape = RoundedCornerShape(8.dp),
        title = {
            Text(
                text = "[ СИНХРОНІЗАЦІЯ ЦИКЛУ ]",
                color = NeonCyan,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Text(
                text = "Призначити День $dayNumber циклу поточним? Це оновить ваш розклад відповідно до сьогоднішньої дати.",
                color = TextPrimary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                textAlign = TextAlign.Start
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "ПІДТВЕРДИТИ",
                    color = NeonCyan,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "СКАСУВАТИ",
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    )
}
