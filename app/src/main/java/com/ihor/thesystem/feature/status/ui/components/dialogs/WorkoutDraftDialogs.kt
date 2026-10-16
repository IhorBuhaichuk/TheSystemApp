package com.ihor.thesystem.feature.status.ui.components.dialogs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.components.SystemDialogActions
import com.ihor.thesystem.core.ui.components.SystemDialogContainer
import com.ihor.thesystem.core.ui.components.SystemDialogHeader
import com.ihor.thesystem.domain.model.WorkoutDraftCompatibility
import java.time.LocalDate

@Composable
fun WorkoutDraftConflictDialog(
    workoutName: String?,
    dateEpochDay: Long,
    reason: WorkoutDraftCompatibility,
    onKeep: () -> Unit,
    onDiscardAndContinue: () -> Unit
) {
    val colors = SystemTheme.colors
    val savedDate = LocalDate.ofEpochDay(dateEpochDay).toString()
    val explanation = when (reason) {
        WorkoutDraftCompatibility.PREVIOUS_DAY ->
            "Є незавершена чернетка за $savedDate. Вона не буде змішана із сьогоднішнім тренуванням."
        WorkoutDraftCompatibility.PLAN_CHANGED ->
            "План змінився після збереження чернетки. Старі підходи не будуть накладені на новий план."
        WorkoutDraftCompatibility.COMPATIBLE ->
            "Збережену чернетку можна продовжити."
    }

    Dialog(onDismissRequest = onKeep) {
        SystemDialogContainer(accent = colors.accentWarning) {
            SystemDialogHeader(
                title = "Збережена чернетка",
                subtitle = workoutName?.takeIf { it.isNotBlank() },
                icon = Icons.Default.WarningAmber,
                accent = colors.accentWarning
            )
            Text(
                text = explanation,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Medium
                )
            )
            SystemDialogActions(
                cancelText = "Залишити",
                confirmText = "Видалити й почати",
                onCancel = onKeep,
                onConfirm = onDiscardAndContinue,
                accent = colors.accentError,
                confirmIcon = Icons.Default.DeleteOutline
            )
        }
    }
}

@Composable
fun ConfirmDiscardWorkoutDraftDialog(
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    val colors = SystemTheme.colors
    Dialog(onDismissRequest = onCancel) {
        SystemDialogContainer(accent = colors.accentError) {
            SystemDialogHeader(
                title = "Видалити чернетку?",
                subtitle = "Введені підходи буде видалено. Завершені тренування та прогрес не зміняться.",
                icon = Icons.Default.DeleteOutline,
                accent = colors.accentError
            )
            SystemDialogActions(
                cancelText = "Скасувати",
                confirmText = "Видалити",
                onCancel = onCancel,
                onConfirm = onConfirm,
                accent = colors.accentError,
                confirmIcon = Icons.Default.DeleteOutline
            )
        }
    }
}

