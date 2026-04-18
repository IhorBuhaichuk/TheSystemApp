package com.ihor.thesystem.feature.status.ui.components.dialogs

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xFF020408))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
        ) {
            // Decorative background glow
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.Center)
                    .blur(60.dp)
                    .background(NeonGold.copy(alpha = 0.1f), CircleShape)
            )

            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Animated hex badge
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(scale),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val path = buildHexagonPath(size, 0f)
                        drawPath(
                            path,
                            Brush.radialGradient(listOf(NeonGold.copy(alpha = 0.3f), Color.Transparent))
                        )
                        drawPath(path, NeonGold, style = Stroke(4.dp.toPx()))
                    }
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = NeonGold,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "LEVEL UP",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonGold,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp
                        )
                    )
                    Text(
                        text = "СИСТЕМА ОНОВЛЕНА",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "ВАШ НОВИЙ КЛАС",
                        style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.3f), letterSpacing = 1.sp)
                    )
                    Text(
                        text = newClass.uppercase(),
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = NeonCyan,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                    )
                    Text(
                        text = "МІСЯЦЬ $newMonth",
                        style = MaterialTheme.typography.labelMedium.copy(color = NeonGold, fontWeight = FontWeight.Bold)
                    )
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonGold,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "ПРИЙНЯТИ СИЛУ",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    )
                }
            }
        }
    }
}
