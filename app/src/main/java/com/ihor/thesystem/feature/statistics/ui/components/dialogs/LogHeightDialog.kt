package com.ihor.thesystem.feature.statistics.ui.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Height
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.Dialog
import com.ihor.thesystem.R
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemDialogContainer
import com.ihor.thesystem.core.ui.components.SystemGhostButton
import com.ihor.thesystem.core.ui.components.systemOutlinedTextFieldColors

@Composable
fun LogHeightDialog(
    currentHeight: Float,
    onConfirm: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = SystemTheme.colors
    var input by remember { mutableStateOf(currentHeight.toInt().toString()) }
    var isError by remember { mutableStateOf(false) }
    val canConfirm = !isError && input.isNotBlank()

    Dialog(onDismissRequest = onDismiss) {
        SystemDialogContainer(accent = colors.accentPrimary) {
            Icon(Icons.Filled.Height, contentDescription = null, tint = colors.accentPrimary)
            Text(
                text = "[ Оновити зріст ]",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = colors.accentPrimary,
                    fontWeight = FontWeight.Bold
                )
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
                    label = { Text("Зріст (см)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SystemItemSpacing)
            ) {
                SystemGhostButton(
                    text = stringResource(R.string.text_cancel),
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                )
                SystemButton(
                    text = stringResource(R.string.text_confirm),
                    onClick = {
                        val height = input.toFloatOrNull()
                        if (height != null && height > 0f) onConfirm(height)
                    },
                    modifier = Modifier.weight(1f),
                    accent = colors.accentPrimary,
                    enabled = canConfirm,
                    glow = canConfirm
                )
            }
        }
    }
}
