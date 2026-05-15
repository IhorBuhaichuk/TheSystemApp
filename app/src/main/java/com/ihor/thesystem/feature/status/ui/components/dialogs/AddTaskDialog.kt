package com.ihor.thesystem.feature.status.ui.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
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
import com.ihor.thesystem.core.ui.components.SystemDialogContainer
import com.ihor.thesystem.core.ui.components.SystemGhostButton
import com.ihor.thesystem.core.ui.components.systemOutlinedTextFieldColors
import kotlinx.coroutines.delay

@Composable
fun AddTaskDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
    titleText: String? = null,
    subtitleText: String = "To-do",
    placeholderText: String? = null
) {
    var taskName by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val trimmedTaskName = taskName.trim()
    val dialogTitle = titleText ?: stringResource(R.string.text_add_task_title)
    val dialogPlaceholder = placeholderText ?: stringResource(R.string.text_add_task_placeholder)
    val colors = SystemTheme.colors
    val iconShape = RoundedCornerShape(SystemTheme.shapes.medium)

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(iconShape)
                            .background(colors.accentPrimarySoft)
                            .border(1.dp, colors.accentPrimary.copy(alpha = 0.34f), iconShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = colors.accentPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = dialogTitle,
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = colors.textPrimary,
                                fontWeight = FontWeight.Black
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = subtitleText,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = colors.textSecondary,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1
                        )
                    }
                }

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
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() })
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SystemGhostButton(
                        text = stringResource(R.string.text_add_task_cancel),
                        onClick = ::dismiss,
                        modifier = Modifier.weight(1f)
                    )

                    SystemButton(
                        text = stringResource(R.string.text_add_task_confirm),
                        icon = Icons.Filled.Add,
                        onClick = ::submit,
                        enabled = trimmedTaskName.isNotEmpty(),
                        glow = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
