package com.ihor.thesystem.feature.statistics.ui.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.NeonCyan
import com.ihor.thesystem.core.theme.NeonCyanDim
import com.ihor.thesystem.core.theme.NeonRed
import com.ihor.thesystem.core.theme.PanelBorder
import com.ihor.thesystem.core.theme.PanelSurface
import com.ihor.thesystem.core.theme.TextPrimary
import com.ihor.thesystem.core.theme.TextSecondary

@Composable
fun LogAgeDialog(
    currentAge: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var input by remember { mutableStateOf(if (currentAge > 0) currentAge.toString() else "") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PanelSurface,
        shape = RoundedCornerShape(4.dp),
        icon = {
            Icon(Icons.Filled.Cake, contentDescription = null, tint = NeonCyanDim)
        },
        title = {
            Text(
                text = "[ ОНОВИТИ ВІК ]",
                color = NeonCyan,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = input,
                    onValueChange = {
                        input = it
                        val value = it.toIntOrNull()
                        isError = it.isNotEmpty() && (value == null || value !in 1..120)
                    },
                    singleLine = true,
                    isError = isError,
                    label = {
                        Text("Вік", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyanDim,
                        unfocusedBorderColor = PanelBorder,
                        focusedLabelColor = NeonCyanDim,
                        cursorColor = NeonCyanDim,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        errorBorderColor = NeonRed
                    ),
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
                )
                if (isError) {
                    Text(
                        text = "Введіть коректний вік",
                        color = NeonRed,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val age = input.toIntOrNull()
                    if (age != null && age in 1..120) onConfirm(age)
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
