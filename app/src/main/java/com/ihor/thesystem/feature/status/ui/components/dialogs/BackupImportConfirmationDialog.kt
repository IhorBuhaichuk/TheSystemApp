package com.ihor.thesystem.feature.status.ui.components.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import com.ihor.thesystem.core.theme.SystemItemSpacing
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemDialogContainer
import com.ihor.thesystem.core.ui.components.SystemGhostButton
import com.ihor.thesystem.feature.status.viewmodel.BackupImportPreviewUiState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
internal fun BackupImportConfirmationDialog(
    preview: BackupImportPreviewUiState,
    isBusy: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val colors = SystemTheme.colors

    Dialog(onDismissRequest = { if (!isBusy) onDismiss() }) {
        SystemDialogContainer(accent = colors.accentWarning) {
            Text(
                text = "Відновити дані з резервної копії",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = colors.accentWarning,
                    fontWeight = FontWeight.Black
                )
            )
            Text(
                text = "У копії ${preview.tableCount} розділів і ${preview.rowCount} записів. Створено ${preview.exportedAtMillis.previewTimeLabel()}.",
                style = MaterialTheme.typography.bodyMedium.copy(color = colors.textPrimary)
            )
            Text(
                text = "Після підтвердження наявні дані буде оновлено інформацією з файлу. Якщо відновлення не завершиться, нічого не зміниться.",
                style = MaterialTheme.typography.bodySmall.copy(color = colors.textSecondary)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SystemItemSpacing)
            ) {
                SystemGhostButton(
                    text = "Скасувати",
                    onClick = onDismiss,
                    enabled = !isBusy,
                    modifier = Modifier.weight(1f)
                )
                SystemButton(
                    text = if (isBusy) "Відновлення…" else "Відновити",
                    onClick = onConfirm,
                    enabled = !isBusy,
                    accent = colors.accentWarning,
                    glow = !isBusy,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private fun Long.previewTimeLabel(): String =
    Instant.ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .format(BACKUP_PREVIEW_TIME_FORMATTER)

private val BACKUP_PREVIEW_TIME_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
