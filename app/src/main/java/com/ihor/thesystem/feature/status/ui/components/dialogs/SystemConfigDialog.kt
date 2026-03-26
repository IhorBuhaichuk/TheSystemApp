package com.ihor.thesystem.feature.status.ui.components.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.domain.model.SystemConfig

@Composable
fun SystemConfigDialog(
    config: SystemConfig,
    onConfirm: (SystemConfig) -> Unit,
    onDismiss: () -> Unit
) {
    var penalty    by remember { mutableStateOf(config.defaultPenalty.toString()) }
    var sets       by remember { mutableStateOf(config.targetSets.toString()) }
    var reps       by remember { mutableStateOf(config.targetReps.toString()) }
    var weeks      by remember { mutableStateOf(config.matrixWeeks.toString()) }

    val isValid = listOf(penalty, sets, reps, weeks).all { it.toIntOrNull() != null }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = PanelSurface,
        shape            = RoundedCornerShape(4.dp),
        icon = {
            Icon(Icons.Filled.Settings, null, tint = NeonCyan)
        },
        title = {
            Text(
                text          = "[ СИСТЕМА: КОНФІГ ]",
                color         = NeonCyan,
                fontFamily    = FontFamily.Monospace,
                fontWeight    = FontWeight.Bold,
                fontSize      = 14.sp,
                letterSpacing = 1.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ConfigField(
                    label    = "ШТРАФ (%)",
                    value    = penalty,
                    onChange = { penalty = it }
                )
                ConfigField(
                    label    = "ЦІЛЬОВІ ПІДХОДИ",
                    value    = sets,
                    onChange = { sets = it }
                )
                ConfigField(
                    label    = "ЦІЛЬОВІ ПОВТОРИ",
                    value    = reps,
                    onChange = { reps = it }
                )
                ConfigField(
                    label    = "ТИЖНІВ У МАТРИЦІ",
                    value    = weeks,
                    onChange = { weeks = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        config.copy(
                            defaultPenalty = penalty.toIntOrNull() ?: config.defaultPenalty,
                            targetSets     = sets.toIntOrNull()    ?: config.targetSets,
                            targetReps     = reps.toIntOrNull()    ?: config.targetReps,
                            matrixWeeks    = weeks.toIntOrNull()   ?: config.matrixWeeks
                        )
                    )
                },
                enabled = isValid
            ) {
                Text(
                    "ЗБЕРЕГТИ",
                    color      = NeonCyan,
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 12.sp
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "СКАСУВАТИ",
                    color      = TextSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 12.sp
                )
            }
        }
    )
}

@Composable
private fun ConfigField(
    label: String,
    value: String,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onChange,
        singleLine    = true,
        label = {
            Text(label, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = NeonCyan,
            unfocusedBorderColor = PanelBorder,
            focusedLabelColor    = NeonCyan,
            cursorColor          = NeonCyan,
            focusedTextColor     = TextPrimary,
            unfocusedTextColor   = TextPrimary
        ),
        textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
        modifier  = Modifier.fillMaxWidth()
    )
}