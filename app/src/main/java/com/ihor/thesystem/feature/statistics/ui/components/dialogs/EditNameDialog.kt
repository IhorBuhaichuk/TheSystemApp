package com.ihor.thesystem.feature.statistics.ui.components.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.*

import androidx.compose.ui.res.stringResource
import com.ihor.thesystem.R

@Composable
fun EditNameDialog(
    currentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var inputName by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = PanelSurface,
        shape            = RoundedCornerShape(4.dp),
        icon = {
            Icon(Icons.Filled.Person, contentDescription = null, tint = NeonCyan)
        },
        title = {
            Text(
                text       = stringResource(R.string.text_edit_name_title),
                color      = NeonCyan,
                fontFamily = FontFamily.Monospace,
                fontSize   = 14.sp
            )
        },
        text = {
            OutlinedTextField(
                value          = inputName,
                onValueChange  = { inputName = it.uppercase() },
                singleLine     = true,
                label          = {
                    Text(stringResource(R.string.text_new_name), fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                },
                colors         = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = NeonCyan,
                    unfocusedBorderColor = PanelBorder,
                    focusedLabelColor    = NeonCyan,
                    cursorColor          = NeonCyan,
                    focusedTextColor     = TextPrimary,
                    unfocusedTextColor   = TextPrimary
                ),
                textStyle      = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace)
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(inputName) }) {
                Text(stringResource(R.string.text_confirm), color = NeonCyan, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.text_cancel), color = TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
            }
        }
    )
}
