package com.ihor.thesystem.feature.statistics.ui.components.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import com.ihor.thesystem.R
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.components.SystemDialogActions
import com.ihor.thesystem.core.ui.components.SystemDialogContainer
import com.ihor.thesystem.core.ui.components.SystemDialogHeader
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
            SystemDialogHeader(
                title = stringResource(R.string.text_edit_name_title),
                subtitle = "Це ім’я показується у вашому профілі",
                icon = Icons.Filled.Person,
                accent = colors.accentPrimary
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
            SystemDialogActions(
                cancelText = stringResource(R.string.text_cancel),
                confirmText = stringResource(R.string.text_confirm),
                onCancel = onDismiss,
                onConfirm = { onConfirm(inputName) },
                accent = colors.accentPrimary
            )
        }
    }
}
