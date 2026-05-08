package com.ihor.thesystem.feature.status.ui.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ihor.thesystem.core.theme.NeonCyan
import com.ihor.thesystem.core.theme.RajdhaniFamily
import com.ihor.thesystem.core.ui.asString
import com.ihor.thesystem.domain.model.AiArchitectReport

@Composable
fun WorkoutReportDialog(
    report: AiArchitectReport,
    onDismiss: () -> Unit,
    onOpenAnalysis: () -> Unit = {}
) {
    val context = LocalContext.current
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF020408))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ЗВІТ АРХІТЕКТОРА",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = NeonCyan,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = RajdhaniFamily,
                            letterSpacing = 2.sp
                        )
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
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
                            color = Color.White.copy(alpha = 0.9f),
                            lineHeight = 24.sp
                        )
                    )

                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                    Text(
                        text = "ВІКНО ВІДНОВЛЕННЯ: ${report.recoveryWindowHours.toInt()} ГОДИН",
                        style = MaterialTheme.typography.labelLarge.copy(
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )

                    if (report.isFallback) {
                        Text(
                            text = "AI тимчасово недоступний. Тренування збережено, цілі виставлено обережно.",
                            color = Color.White.copy(alpha = 0.72f),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                OutlinedButton(
                    onClick = onOpenAnalysis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = NeonCyan
                    )
                ) {
                    Text(
                        text = "ВІДКРИТИ АНАЛІЗ",
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                        fontFamily = RajdhaniFamily
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "ПРИЙНЯТИ",
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.5.sp,
                        fontFamily = RajdhaniFamily
                    )
                }
            }
        }
    }
}
