package com.ihor.thesystem.feature.mode.ui.dialogs

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
        containerColor = Color(0xFF020408),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.border(1.dp, NeonCyan.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
        title = {
            Text(
                text = "СИНХРОНІЗАЦІЯ ЦИКЛУ",
                color = NeonCyan,
                fontFamily = TekoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                letterSpacing = 2.sp
            )
        },
        text = {
            Text(
                text = "Призначити День $dayNumber циклу як активний? Це синхронізує розклад тренувань із системним календарем.",
                color = Color.White.copy(alpha = 0.8f),
                fontFamily = RajdhaniFamily,
                fontSize   = 15.sp,
                textAlign  = TextAlign.Start
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "ПІДТВЕРДИТИ",
                    color = NeonCyan,
                    fontFamily = RajdhaniFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "СКАСУВАТИ",
                    color = Color.White.copy(alpha = 0.5f),
                    fontFamily = RajdhaniFamily,
                    fontSize = 14.sp
                )
            }
        }
    )
}
