package com.ihor.thesystem.feature.status.ui.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ihor.thesystem.R
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemDialogActions
import com.ihor.thesystem.core.ui.components.SystemDialogContainer
import com.ihor.thesystem.core.ui.components.SystemDialogHeader
import com.ihor.thesystem.core.ui.components.systemOutlinedTextFieldColors
import kotlinx.coroutines.delay

@Composable
fun AddTaskDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    titleText: String? = null,
    subtitleText: String = "Завдання",
    placeholderText: String? = null
) {
    var taskName by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val trimmedTaskName = taskName.trim()
    val dialogTitle = titleText ?: stringResource(R.string.text_add_task_title)
    val dialogPlaceholder = placeholderText ?: stringResource(R.string.text_add_task_placeholder)
    val colors = SystemTheme.colors

    fun dismiss() {
        keyboardController?.hide()
        onDismiss()
    }

    fun submit() {
        if (trimmedTaskName.isNotEmpty()) {
            keyboardController?.hide()
            onConfirm(trimmedTaskName)
        }
    }

    LaunchedEffect(Unit) {
        delay(120)
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Dialog(
        onDismissRequest = ::dismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            SystemDialogContainer(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth(),
                accent = colors.accentPrimary
            ) {
                SystemDialogHeader(
                    title = dialogTitle,
                    subtitle = subtitleText,
                    icon = Icons.Filled.Add,
                    accent = colors.accentPrimary
                )

                OutlinedTextField(
                    value = taskName,
                    onValueChange = { taskName = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    placeholder = {
                        Text(
                            text = dialogPlaceholder,
                            color = colors.textMuted
                        )
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = colors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    ),
                    colors = systemOutlinedTextFieldColors(colors.accentPrimary),
                    shape = RoundedCornerShape(SystemTheme.shapes.medium),
                    singleLine = false,
                    minLines = 3,
                    maxLines = 8,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default)
                )

                SystemDialogActions(
                    cancelText = stringResource(R.string.text_add_task_cancel),
                    confirmText = stringResource(R.string.text_add_task_confirm),
                    onCancel = ::dismiss,
                    onConfirm = ::submit,
                    confirmEnabled = trimmedTaskName.isNotEmpty(),
                    confirmIcon = Icons.Filled.Add,
                    confirmGlow = trimmedTaskName.isNotEmpty()
                )
            }
        }
    }
}
