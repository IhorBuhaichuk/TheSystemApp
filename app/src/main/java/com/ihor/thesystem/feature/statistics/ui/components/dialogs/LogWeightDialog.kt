package com.ihor.thesystem.feature.statistics.ui.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.Dialog
import com.ihor.thesystem.R
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.components.SystemDialogActions
import com.ihor.thesystem.core.ui.components.SystemDialogContainer
import com.ihor.thesystem.core.ui.components.SystemDialogHeader
import com.ihor.thesystem.core.ui.components.systemOutlinedTextFieldColors

@Composable
fun LogWeightDialog(
    currentWeight: Float,
    onConfirm: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = SystemTheme.colors
    var input by remember { mutableStateOf(currentWeight.toString()) }
    var isError by remember { mutableStateOf(false) }
    val canConfirm = !isError && input.isNotBlank()

    Dialog(onDismissRequest = onDismiss) {
        SystemDialogContainer(accent = colors.accentPrimary) {
            SystemDialogHeader(
                title = "Записати вагу",
                subtitle = "Вкажіть поточну вагу в кілограмах",
                icon = Icons.Filled.FitnessCenter,
                accent = colors.accentPrimary
            )
            Column(verticalArrangement = Arrangement.spacedBy(SystemItemSpacing)) {
                OutlinedTextField(
                    value = input,
                    onValueChange = {
                        input = it
                        isError = it.toFloatOrNull() == null && it.isNotEmpty()
                    },
                    singleLine = true,
                    isError = isError,
                    label = { Text("Вага (кг)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = systemOutlinedTextFieldColors(accent = colors.accentPrimary),
                    textStyle = LocalTextStyle.current.copy(color = colors.textPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
                if (isError) {
                    Text(
                        text = "Введіть коректне число",
                        style = MaterialTheme.typography.bodySmall.copy(color = colors.accentError)
                    )
                }
            }
            SystemDialogActions(
                cancelText = stringResource(R.string.text_cancel),
                confirmText = stringResource(R.string.text_confirm),
                onCancel = onDismiss,
                onConfirm = {
                    val weight = input.toFloatOrNull()
                    if (weight != null && weight > 0f) onConfirm(weight)
                },
                accent = colors.accentPrimary,
                confirmEnabled = canConfirm
            )
        }
    }
}
