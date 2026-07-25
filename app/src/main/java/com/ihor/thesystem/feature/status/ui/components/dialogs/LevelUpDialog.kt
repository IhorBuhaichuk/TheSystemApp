package com.ihor.thesystem.feature.status.ui.components.dialogs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ihor.thesystem.R
import com.ihor.thesystem.core.theme.SystemScreenPadding
import com.ihor.thesystem.core.theme.SystemTheme
import com.ihor.thesystem.core.ui.asUiText
import com.ihor.thesystem.core.ui.toSystemSentenceCase
import com.ihor.thesystem.core.ui.components.SystemButton
import com.ihor.thesystem.core.ui.components.SystemDialogContainer
import com.ihor.thesystem.core.ui.components.buildHexagonPath
import com.ihor.thesystem.core.ui.components.systemCelebrationMotion
import com.ihor.thesystem.domain.model.PlayerRank

@Composable
fun LevelUpDialog(
    newClass: PlayerRank,
    newMonth: Int,
    onDismiss: () -> Unit
) {
    val colors = SystemTheme.colors
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        SystemDialogContainer(
            accent = colors.accentWarning,
            contentPadding = SystemScreenPadding + 8.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .systemCelebrationMotion(),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val path = buildHexagonPath(size, 0f)
                        drawPath(
                            path,
                            Brush.radialGradient(
                                listOf(
                                    colors.accentWarning.copy(alpha = 0.30f),
                                    colors.accentAiSoft,
                                    Color.Transparent
                                )
                            )
                        )
                        drawPath(path, colors.accentWarning, style = Stroke(3.dp.toPx()))
                    }
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = colors.accentWarning,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.text_level_up),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = colors.accentWarning,
                            fontWeight = FontWeight.Black
                        )
                    )
                    Text(
                        text = stringResource(R.string.text_system_updated),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = colors.textPrimary,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = newClass.asUiText().asString(context).toSystemSentenceCase(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = colors.accentPrimary,
                            fontWeight = FontWeight.Black
                        ),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.text_month_n, newMonth),
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = colors.textSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                SystemButton(
                    text = stringResource(R.string.text_accept_power),
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    icon = Icons.Filled.Star,
                    accent = colors.accentWarning,
                    glow = true
                )
            }
        }
    }
}
