package com.ihor.thesystem.feature.statistics.ui.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.window.Dialog
import com.ihor.thesystem.R
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemDialogContainer
import com.ihor.thesystem.core.ui.components.SystemGhostButton
import com.ihor.thesystem.core.ui.components.systemOutlinedTextFieldColors

@Composable
fun EditNameDialog(
    currentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = SystemTheme.colors
    var inputName by remember { mutableStateOf(currentName) }

    Dialog(onDismissRequest = onDismiss) {
        SystemDialogContainer(accent = colors.accentPrimary) {
            Icon(Icons.Filled.Person, contentDescription = null, tint = colors.accentPrimary)
            Text(
                text = stringResource(R.string.text_edit_name_title),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = colors.accentPrimary,
                    fontWeight = FontWeight.Bold
                )
            )
            OutlinedTextField(
                value = inputName,
                onValueChange = { inputName = it },
                singleLine = true,
                label = { Text(stringResource(R.string.text_new_name)) },
                colors = systemOutlinedTextFieldColors(accent = colors.accentPrimary),
                textStyle = LocalTextStyle.current.copy(color = colors.textPrimary),
                modifier = Modifier.fillMaxWidth()
            )
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
                    onClick = { onConfirm(inputName) },
                    modifier = Modifier.weight(1f),
                    accent = colors.accentPrimary,
                    glow = true
                )
            }
        }
    }
}
