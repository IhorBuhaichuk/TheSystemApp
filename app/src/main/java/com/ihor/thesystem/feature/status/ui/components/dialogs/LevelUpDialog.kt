package com.ihor.thesystem.feature.status.ui.components.dialogs

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ihor.thesystem.core.theme.*
import com.ihor.thesystem.core.ui.components.buildHexagonPath

@Composable
fun LevelUpDialog(
    newClass: String,
    newMonth: Int,
    onDismiss: () -> Unit
) {
    val scale by rememberInfiniteTransition(label = "pulse")
        .animateFloat(
            initialValue  = 0.95f,
            targetValue   = 1.05f,
            animationSpec = infiniteRepeatable(
                tween(800, easing = EaseInOutSine),
                RepeatMode.Reverse
            ),
            label = "scale"
        )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = PanelSurface,
        shape            = RoundedCornerShape(4.dp),
        title = {
            Column(
                modifier            = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Animated hex
                Box(
                    modifier         = Modifier.size(80.dp).scale(scale),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val path = buildHexagonPath(size, 0f)
                        drawPath(path, NeonGold.copy(0.15f))
                        drawPath(path, NeonGold, style = Stroke(3.dp.toPx()))
                    }
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint     = NeonGold,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text          = "ЛЕВЕЛ АП!",
                    color         = NeonGold,
                    fontFamily    = FontFamily.Monospace,
                    fontWeight    = FontWeight.Bold,
                    fontSize      = 22.sp,
                    letterSpacing = 4.sp,
                    textAlign     = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier            = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text       = "МІСЯЦЬ $newMonth РОЗПОЧАТО",
                    color      = NeonCyan,
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 12.sp,
                    textAlign  = TextAlign.Center
                )
                Divider(color = NeonGold.copy(0.2f))
                Text(
                    text       = "НОВИЙ КЛАС:",
                    color      = TextSecondary,
                    fontFamily = FontFamily.Monospace,
                    fontSize   = 11.sp,
                    textAlign  = TextAlign.Center
                )
                Text(
                    text          = "[ $newClass ]",
                    color         = NeonGold,
                    fontFamily    = FontFamily.Monospace,
                    fontWeight    = FontWeight.Bold,
                    fontSize      = 16.sp,
                    letterSpacing = 2.sp,
                    textAlign     = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text          = "[ ПРИЙНЯТИ СИЛУ ]",
                        color         = NeonGold,
                        fontFamily    = FontFamily.Monospace,
                        fontWeight    = FontWeight.Bold,
                        fontSize      = 13.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    )
}