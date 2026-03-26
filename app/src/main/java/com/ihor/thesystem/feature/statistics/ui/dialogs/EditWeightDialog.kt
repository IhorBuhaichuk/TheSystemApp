package com.ihor.thesystem.feature.statistics.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.feature.statistics.viewmodel.MatrixEntryUiModel
import androidx.compose.ui.Alignment

@Composable
fun EditWeightDialog(
    entry: MatrixEntryUiModel,
    onConfirm: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    var input   by remember { mutableStateOf(entry.currentWeight.toString()) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = PanelSurface,
        shape            = RoundedCornerShape(4.dp),
        icon = {
            Icon(Icons.Filled.FitnessCenter, null, tint = NeonGreen)
        },
        title = {
            Text(
                text       = "[ ПОТОЧНА ВАГА ]",
                color      = NeonGreen,
                fontFamily = FontFamily.Monospace,
                fontSize   = 14.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text       = entry.exerciseName,
                    color      = TextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    fontSize   = 13.sp
                )
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoChip("СТАРТ",  entry.displayStart)
                    InfoChip("ЦІЛЬ",   entry.displayTarget)
                    InfoChip("+/тиж", "+${String.format("%.2f", entry.weeklyStep)}кг")
                }
                OutlinedTextField(
                    value         = input,
                    onValueChange = {
                        input   = it
                        isError = it.toFloatOrNull() == null && it.isNotEmpty()
                    },
                    singleLine    = true,
                    isError       = isError,
                    label = {
                        Text("Поточна вага (кг)", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = NeonGreen,
                        unfocusedBorderColor = PanelBorder,
                        focusedLabelColor    = NeonGreen,
                        cursorColor          = NeonGreen,
                        focusedTextColor     = TextPrimary,
                        unfocusedTextColor   = TextPrimary,
                        errorBorderColor     = NeonRed
                    ),
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
                )
                if (isError) {
                    Text("Введіть коректне число", color = NeonRed,
                        fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { input.toFloatOrNull()?.let { onConfirm(it) } },
                enabled = !isError && input.isNotBlank()
            ) {
                Text("ЗБЕРЕГТИ", color = NeonGreen, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("СКАСУВАТИ", color = TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
        }
    )
}

@Composable
private fun InfoChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 9.sp)
        Text(value, color = NeonCyan, fontFamily = FontFamily.Monospace,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 12.sp)
    }
}