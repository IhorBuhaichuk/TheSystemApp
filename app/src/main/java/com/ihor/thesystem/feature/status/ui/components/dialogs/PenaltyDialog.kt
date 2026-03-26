package com.ihor.thesystem.feature.status.ui.components.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.*

@Composable
fun PenaltyActivatedDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = PanelSurface,
        shape            = RoundedCornerShape(4.dp),
        icon = {
            Icon(Icons.Filled.Warning, null, tint = NeonRed, modifier = Modifier.size(36.dp))
        },
        title = {
            Text(
                text          = "ШТРАФНА ЗОНА",
                color         = NeonRed,
                fontFamily    = FontFamily.Monospace,
                fontWeight    = FontWeight.Bold,
                fontSize      = 18.sp,
                letterSpacing = 2.sp,
                textAlign     = TextAlign.Center,
                modifier      = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                modifier            = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text       = "2 провали поспіль зафіксовано.",
                    color      = TextPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 12.sp,
                    textAlign  = TextAlign.Center
                )
                Divider(color = NeonRed.copy(0.3f))
                Text(
                    text       = "Всі цільові ваги знижені на 20%.",
                    color      = NeonRed,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 12.sp,
                    textAlign  = TextAlign.Center
                )
                Text(
                    text       = "Виконай 2 Main Quest поспіль для відновлення.",
                    color      = TextSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 11.sp,
                    textAlign  = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TextButton(onClick = onDismiss) {
                    Text(
                        "[ ЗРОЗУМІЛО ]",
                        color      = NeonRed,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 13.sp
                    )
                }
            }
        }
    )
}

@Composable
fun PenaltyDeactivatedDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = PanelSurface,
        shape            = RoundedCornerShape(4.dp),
        icon = {
            Icon(Icons.Filled.Warning, null, tint = NeonGreen, modifier = Modifier.size(36.dp))
        },
        title = {
            Text(
                text       = "ШТРАФ ЗНЯТО",
                color      = NeonGreen,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize   = 18.sp,
                textAlign  = TextAlign.Center,
                modifier   = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text       = "2 успішних Main Quest поспіль!\nПродуктивність відновлено.",
                color      = TextPrimary,
                fontFamily = FontFamily.Monospace,
                fontSize   = 12.sp,
                textAlign  = TextAlign.Center,
                modifier   = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TextButton(onClick = onDismiss) {
                    Text(
                        "[ ПРОДОВЖИТИ ]",
                        color      = NeonGreen,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 13.sp
                    )
                }
            }
        }
    )
}