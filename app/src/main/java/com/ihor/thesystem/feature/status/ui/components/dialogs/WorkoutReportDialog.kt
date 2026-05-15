package com.ihor.thesystem.feature.status.ui.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ihor.thesystem.core.theme.SystemScreenPadding
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.asString
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemGhostButton
import com.ihor.thesystem.core.ui.components.SystemIconButton
import com.ihor.thesystem.domain.model.AiArchitectReport

@Composable
fun WorkoutReportDialog(
    report: AiArchitectReport,
    onDismiss: () -> Unit,
    onOpenAnalysis: () -> Unit = {}
) {
    val context = LocalContext.current
    val colors = SystemTheme.colors

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(SystemScreenPadding)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Звіт архітектора",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = colors.accentAi,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )

                    SystemIconButton(
                        icon = Icons.Default.Close,
                        contentDescription = "Close",
                        onClick = onDismiss
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = report.architectFeedback.asString(context),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = colors.textPrimary.copy(alpha = 0.92f)
                        )
                    )

                    HorizontalDivider(color = colors.borderSubtle)

                    Text(
                        text = "Вікно відновлення: ${report.recoveryWindowHours.toInt()} годин",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = colors.accentPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    if (report.isFallback) {
                        Text(
                            text = "AI тимчасово недоступний. Тренування збережено, цілі виставлено обережно.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = colors.textSecondary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                SystemGhostButton(
                    text = "Відкрити аналіз",
                    onClick = onOpenAnalysis,
                    modifier = Modifier
                        .fillMaxWidth(),
                    accent = colors.accentAi
                )

                Spacer(modifier = Modifier.height(10.dp))

                SystemButton(
                    text = "Прийняти",
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth(),
                    accent = colors.accentPrimary,
                    glow = true
                )
            }
        }
    }
}
