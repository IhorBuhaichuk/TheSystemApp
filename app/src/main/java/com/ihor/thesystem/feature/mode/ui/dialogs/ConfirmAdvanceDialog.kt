package com.ihor.thesystem.feature.mode.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.core.ui.components.sciPanel

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
        containerColor   = Color(0xFF020408),
        shape            = RoundedCornerShape(16.dp),
        modifier = Modifier.border(1.dp, NeonCyan.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
        icon = {
            Icon(Icons.Filled.SwapHoriz, contentDescription = null, tint = NeonCyan)
        },
        title = {
            Text(
                text       = "ПЕРЕХІД ПРОТОКОЛУ",
                color      = NeonCyan,
                fontFamily = TekoFamily,
                fontWeight = FontWeight.Bold,
                fontSize   = 22.sp,
                letterSpacing = 2.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text       = "Завершити День $currentDay та розпочати День $nextDay?",
                    color      = Color.White,
                    fontFamily = RajdhaniFamily,
                    fontSize   = 16.sp
                )
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .sciPanel(NeonRed.copy(0.3f), NeonRed.copy(0.05f), 8.dp)
                        .padding(12.dp)
                ) {
                    Text(
                        text       = "УВАГА: Невиконані завдання будуть позначені як ПРОВАЛЕНО.",
                        color      = NeonRed.copy(alpha = 0.9f),
                        fontFamily = RajdhaniFamily,
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                Button(
                    onClick = onForceComplete,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, NeonGreen.copy(alpha = 0.3f))
                ) {
                    Text(
                        text       = "✓ ЗАВЕРШИТИ ВСЕ УСПІШНО",
                        color      = NeonGreen,
                        fontFamily = RajdhaniFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 13.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = NeonCyan.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text       = "ПЕРЕЙТИ",
                    color      = NeonCyan,
                    fontFamily = RajdhaniFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 14.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text       = "СКАСУВАТИ",
                    color      = Color.White.copy(alpha = 0.5f),
                    fontFamily = RajdhaniFamily,
                    fontSize   = 14.sp
                )
            }
        }
    )
}