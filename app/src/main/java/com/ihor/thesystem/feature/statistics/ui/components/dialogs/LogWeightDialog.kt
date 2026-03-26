package com.ihor.thesystem.feature.statistics.ui.components.dialogs

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

@Composable
fun LogWeightDialog(
    currentWeight: Float,
    onConfirm: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    var input by remember { mutableStateOf(currentWeight.toString()) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = PanelSurface,
        shape            = RoundedCornerShape(4.dp),
        icon = {
            Icon(Icons.Filled.FitnessCenter, contentDescription = null, tint = NeonCyanDim)
        },
        title = {
            Text(
                text       = "[ ЛОГУВАТИ ВАГУ ]",
                color      = NeonCyan,
                fontFamily = FontFamily.Monospace,
                fontSize   = 14.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value         = input,
                    onValueChange = {
                        input   = it
                        isError = it.toFloatOrNull() == null && it.isNotEmpty()
                    },
                    singleLine    = true,
                    isError       = isError,
                    label         = {
                        Text("Вага (кг)", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = NeonCyanDim,
                        unfocusedBorderColor = PanelBorder,
                        focusedLabelColor    = NeonCyanDim,
                        cursorColor          = NeonCyanDim,
                        focusedTextColor     = TextPrimary,
                        unfocusedTextColor   = TextPrimary,
                        errorBorderColor     = NeonRed
                    ),
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
                )
                if (isError) {
                    Text(
                        text       = "Введіть коректне число",
                        color      = NeonRed,
                        fontSize   = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick  = {
                    val w = input.toFloatOrNull()
                    if (w != null && w > 0f) onConfirm(w)
                },
                enabled = !isError && input.isNotBlank()
            ) {
                Text("ЗБЕРЕГТИ", color = NeonCyanDim, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("СКАСУВАТИ", color = TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
        }
    )
}